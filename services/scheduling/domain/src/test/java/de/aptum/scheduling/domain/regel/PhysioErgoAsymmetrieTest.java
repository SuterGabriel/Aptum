package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import de.aptum.scheduling.domain.model.Verordnung;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Derselbe Behandlungsverlauf, zwei Therapieformen, gegenläufiges Ergebnis.
 *
 * <p>Dieser Test ist der Grund, warum die Unterbrechungsregeln getrennt
 * modelliert sind. Die Ergotherapie begrenzt die <em>Summe</em> der
 * begründeten Unterbrechungen auf 70 Kalendertage. Die Physiotherapie kennt
 * diese Summe nicht, begrenzt dafür aber die <em>Gesamtlaufzeit</em> auf drei
 * oder sechs Monate ab dem ersten Behandlungstag.
 *
 * <p>Beide Fälle unten sind identische Terminreihen mit identischen
 * Begründungen. Nur die Therapieform unterscheidet sich — und damit das
 * Ergebnis, in beide Richtungen. Eine gemeinsame Regel für beide Formen müsste
 * diese Fälle gleich beantworten und wäre in einem von beiden falsch.
 */
class PhysioErgoAsymmetrieTest {

    private final UnterbrechungsSumme summenregel = new UnterbrechungsSumme();
    private final VerordnungsGueltigkeit laufzeitregel = new VerordnungsGueltigkeit();

    /** Das Gesamturteil aus beiden Regeln: verletzt, sobald eine verletzt ist. */
    private boolean verfallen(Therapieform form, int einheiten, Behandlungsverlauf verlauf) {
        Verordnung verordnung = Verlauf.verordnung(form, einheiten);
        LocalDate stichtag = verlauf.letzterBehandlungstag().orElse(Verlauf.ERSTER_TAG);

        Pruefergebnis summe = summenregel.pruefe(verordnung, verlauf);
        Pruefergebnis laufzeit = laufzeitregel.pruefe(verordnung, verlauf, stichtag);
        return !summe.istErfuellt() || !laufzeit.istErfuellt();
    }

    @Test
    @DisplayName("Lange Pausen, kurze Gesamtdauer: Ergo verfaellt, Physio nicht")
    void langePausenTreffenNurDieErgotherapie() {
        // Vier Behandlungen mit drei begründeten Pausen von je 25 Tagen.
        // Summe der Unterbrechungen: 75 Tage. Gesamtdauer: 75 Tage, also gut
        // unter drei Monaten.
        Behandlungsverlauf verlauf =
                Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 25, 25, 25);

        assertTrue(verfallen(Therapieform.ERGOTHERAPIE, 10, verlauf),
                "75 Tage Unterbrechung reissen die 70-Tage-Summe der Ergotherapie");
        assertTrue(!verfallen(Therapieform.PHYSIOTHERAPIE, 6, verlauf),
                "in der Physiotherapie gibt es diese Summengrenze nicht, "
                        + "und die Laufzeit ist nicht ausgeschoepft");
    }

    @Test
    @DisplayName("Kurze Pausen, lange Gesamtdauer: Physio verfaellt, Ergo nicht")
    void langeGesamtdauerTrifftNurDiePhysiotherapie() {
        // Sechs Behandlungen: vier Pausen von je 14 Tagen, die die Summe der
        // Ergotherapie gar nicht berühren, und eine von 50 Tagen. Zählbare
        // Summe also 50 Tage, unter der Grenze von 70. Gesamtdauer dagegen
        // 106 Tage, damit über den drei Monaten der Physio-Verordnung.
        Behandlungsverlauf verlauf =
                Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 14, 14, 14, 14, 50);

        assertTrue(verfallen(Therapieform.PHYSIOTHERAPIE, 6, verlauf),
                "die Physio-Verordnung ueber sechs Einheiten laeuft nach drei Monaten ab");
        assertTrue(!verfallen(Therapieform.ERGOTHERAPIE, 10, verlauf),
                "in der Ergotherapie zaehlen nur Pausen ueber 14 Tagen, also 50 von 70");
    }

    @Test
    @DisplayName("Die 14-Tage-Regel dagegen trifft beide gleich")
    void dieGrundregelGiltFuerBeide() {
        Behandlungsverlauf ohneBegruendung = Verlauf.mitAbstaenden(20);
        UnterbrechungsFrist grundregel = new UnterbrechungsFrist();

        for (Therapieform form : Therapieform.values()) {
            assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                    grundregel.pruefe(Verlauf.verordnung(form, 6), ohneBegruendung).ausgang(),
                    "eine unbegruendete Pause von 20 Tagen verfaellt in beiden Formen: " + form);
        }
    }
}
