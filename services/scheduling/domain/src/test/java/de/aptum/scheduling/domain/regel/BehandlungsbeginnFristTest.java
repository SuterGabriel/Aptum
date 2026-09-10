package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Verordnung;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Die Grenzfälle der Frist für den Behandlungsbeginn.
 *
 * <p>Die Fallnamen beschreiben die fachliche Situation, nicht die Zahlen. Wer
 * einen roten Test liest, soll wissen, was in der Praxis passiert ist - nicht,
 * welcher Vergleich fehlgeschlagen ist.
 */
class BehandlungsbeginnFristTest {

    private static final LocalDate AUSSTELLUNG = LocalDate.of(2026, 3, 2);
    private static final int EINHEITEN = 6;

    private final BehandlungsbeginnFrist regel = new BehandlungsbeginnFrist();

    private Pruefergebnis pruefeNach(long tage, boolean dringlich) {
        return regel.pruefe(
                new Verordnung(
                        AUSSTELLUNG,
                        dringlich,
                        Verlauf.vertreterFuer(Therapieform.PHYSIOTHERAPIE),
                        EINHEITEN,
                        Verlauf.REGELFREQUENZ),
                AUSSTELLUNG.plusDays(tage));
    }

    @DisplayName("Regelfall: 28 Kalendertage")
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "0,  ERFUELLT, Behandlung am Ausstellungstag",
        "27, ERFUELLT, Behandlung einen Tag vor Fristende",
        "28, ERFUELLT, Behandlung genau am letzten zulaessigen Tag",
        "29, VERLETZT, Behandlung einen Tag nach Fristende",
        "90, VERLETZT, Verordnung liegt lange liegen geblieben",
    })
    void regelfall(long tage, Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(erwartet, pruefeNach(tage, false).ausgang(), situation);
    }

    @DisplayName("Dringlicher Bedarf: verkuerzt auf 14 Kalendertage")
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "13, ERFUELLT, Behandlung einen Tag vor Fristende",
        "14, ERFUELLT, Behandlung genau am letzten zulaessigen Tag",
        "15, VERLETZT, Behandlung einen Tag nach Fristende",
        "20, VERLETZT, im Regelfall zulaessig, mit Dringlichkeitskennzeichen nicht",
    })
    void dringlicherBedarf(long tage, Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(erwartet, pruefeNach(tage, true).ausgang(), situation);
    }

    @Test
    @DisplayName("Der Tag, an dem sich die beiden Fristen unterscheiden")
    void dieselbeVerordnungMitUndOhneKennzeichen() {
        long tage = 20;
        assertTrue(pruefeNach(tage, false).istErfuellt(), "ohne Kennzeichen liegt Tag 20 innerhalb der Regelfrist");
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                pruefeNach(tage, true).ausgang(),
                "mit Kennzeichen ist dieselbe Verordnung verfallen");
    }

    @Test
    @DisplayName("Beginn vor der Ausstellung ist kein Fristfall")
    void beginnVorAusstellung() {
        Pruefergebnis ergebnis = pruefeNach(-1, false);
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(
                ergebnis.begruendung().contains("vor dem Ausstellungsdatum"),
                "die Begruendung muss den Grund nennen, nicht nur die Frist");
    }

    @Test
    @DisplayName("Die Begruendung nennt Zahlen, die die Rezeption vorlesen kann")
    void begruendungIstKlartext() {
        Pruefergebnis ergebnis = pruefeNach(12, false);
        assertTrue(ergebnis.begruendung().contains("12"), "der tatsaechliche Abstand");
        assertTrue(ergebnis.begruendung().contains("28"), "die zulaessige Frist");
    }
}
