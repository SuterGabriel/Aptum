package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;

/**
 * Ist dieser Raum für dieses Heilmittel ausgestattet?
 *
 * <p>Die Regel in zwei Sätzen: Einzelne Leistungen verlangen einen eigenen
 * Bereich mit Mindestfläche und Ausstattung. Ein Termin für eine solche
 * Leistung passt nicht in jeden freien Raum, auch wenn der Raum zu der Zeit
 * leer ist.
 *
 * <p>Das ist die dritte Dimension der Slot-Berechnung und der Fall, der im
 * README steht: Krankengymnastik am Gerät braucht einen Bereich von mindestens
 * 30 m² mit vier Pflichtgeräten ({@code @fundstelle HM-RAUM-02}).
 *
 * <p><strong>Die Fläche allein reicht nicht.</strong> Die Anforderung an den
 * Gerätebereich ist zweiteilig: eine Grundfläche und ein Zuschlag je weiterem
 * Gerät über den vier Pflichtgeräten hinaus. Ein Raum mit 30 m² und zehn
 * Geräten erfüllt sie nicht, obwohl er die Grundfläche hat — deshalb prüft
 * diese Regel beide Hälften.
 *
 * <p><strong>Was hier nicht geprüft wird.</strong> Die Grundausstattung einer
 * Praxis — Mindestfläche, zwei höhenverstellbare Liegen, Notrufanlage
 * ({@code @fundstelle HM-RAUM-01}) — ist eine Zulassungsvoraussetzung der
 * Praxis und keine Eigenschaft der einzelnen Buchung. Sie gehört nicht in die
 * Slot-Suche. Dasselbe gilt für die Ergotherapie
 * ({@code @fundstelle HM-RAUM-07}), die keine leistungsbezogenen
 * Sonderbereiche kennt.
 *
 * <p>Ebenfalls nicht geprüft: die Anforderungen an Bewegungsbad
 * ({@code @fundstelle HM-RAUM-03}) über die Eignung hinaus. Wassertiefe und
 * Temperatur sind Eigenschaften der Anlage, die sich zwischen zwei Terminen
 * nicht ändern. Ob ein Raum ein zugelassenes Bewegungsbad ist, wird als
 * Tatsache geführt, nicht jedes Mal nachgerechnet.
 */
public final class RaumausstattungsGrenze {

    private static final String NAME = "Raumausstattung";

    /** @fundstelle HM-RAUM-02 */
    private static final int GERAETEBEREICH_GRUNDFLAECHE_QM = 30;

    /** @fundstelle HM-RAUM-02 */
    private static final int GERAETEBEREICH_PFLICHTGERAETE = 4;

    /** @fundstelle HM-RAUM-02 */
    private static final int GERAETEBEREICH_ZUSCHLAG_QM_JE_GERAET = 4;

    public Pruefergebnis pruefe(Heilmittel heilmittel, Raum raum) {
        Raumanforderung anforderung = heilmittel.raumanforderung();

        if (!raum.istGeeignetFuer(anforderung)) {
            return Pruefergebnis.verletzt(
                    NAME,
                    "%s verlangt %s; %s ist dafür nicht ausgestattet."
                            .formatted(heilmittel.bezeichnung(), anforderung.bezeichnung(), raum.bezeichnung()));
        }

        if (anforderung == Raumanforderung.GERAETEBEREICH) {
            return pruefeGeraetebereich(heilmittel, raum);
        }

        return Pruefergebnis.erfuellt(
                NAME, "%s ist als %s ausgewiesen.".formatted(raum.bezeichnung(), anforderung.bezeichnung()));
    }

    private Pruefergebnis pruefeGeraetebereich(Heilmittel heilmittel, Raum raum) {
        int ueberzaehligeGeraete = Math.max(0, raum.geraeteAnzahl() - GERAETEBEREICH_PFLICHTGERAETE);
        int gefordert = GERAETEBEREICH_GRUNDFLAECHE_QM + ueberzaehligeGeraete * GERAETEBEREICH_ZUSCHLAG_QM_JE_GERAET;

        if (raum.geraeteAnzahl() < GERAETEBEREICH_PFLICHTGERAETE) {
            return Pruefergebnis.verletzt(
                    NAME,
                    ("%s verlangt %d Pflichtgeräte; %s hat %d.")
                            .formatted(
                                    heilmittel.bezeichnung(),
                                    GERAETEBEREICH_PFLICHTGERAETE,
                                    raum.bezeichnung(),
                                    raum.geraeteAnzahl()));
        }

        String lage = "%s hat %d m² bei %d Geräten, gefordert sind %d m²"
                .formatted(raum.bezeichnung(), raum.flaecheQm(), raum.geraeteAnzahl(), gefordert);

        if (raum.flaecheQm() < gefordert) {
            return Pruefergebnis.verletzt(NAME, lage + ".");
        }
        return Pruefergebnis.erfuellt(NAME, lage + ".");
    }
}
