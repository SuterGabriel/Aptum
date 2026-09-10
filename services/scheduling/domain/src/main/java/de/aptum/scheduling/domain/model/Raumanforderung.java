package de.aptum.scheduling.domain.model;

/**
 * Was ein Heilmittel vom Raum verlangt.
 *
 * <p>Die Zulassungsvoraussetzungen kennen zwei Arten von Anforderung, die
 * getrennt gehören: die <em>Grundausstattung</em>, die jede Praxis für den
 * Regelbetrieb vorhalten muss, und die <em>besonderen Bereiche</em> für
 * einzelne Leistungen. Nur die zweite Art entscheidet, ob ein bestimmter
 * Termin in einen bestimmten Raum passt — die erste ist eine Eigenschaft der
 * Praxis und keine der Buchung.
 *
 * <p>Die Flächen stehen nicht hier, sondern an der Anforderung, die sie
 * verlangt. So steht neben jeder Zahl ihre Fundstelle, und ein geänderter
 * Rechtsstand ist eine Zeile.
 */
public enum Raumanforderung {

    /**
     * Der Regelfall: ein Behandlungsraum der Grundausstattung.
     *
     * <p>Die Praxis muss insgesamt mindestens 23 m² Therapiefläche vorhalten,
     * je zusätzlich gleichzeitig tätiger Person 8 m² mehr, dazu zwei
     * höhenverstellbare Liegen, Kurzzeituhren und eine Notrufanlage
     * ({@code @fundstelle HM-RAUM-01}). Für die Ergotherapie gelten eigene
     * Werte ({@code @fundstelle HM-RAUM-07}).
     *
     * <p>Diese Anforderungen betreffen die Zulassung der Praxis, nicht den
     * einzelnen Termin. Sie sind hier benannt, damit sichtbar bleibt, dass sie
     * existieren — geprüft werden sie an anderer Stelle, wenn es sie gibt.
     */
    GRUNDAUSSTATTUNG("Behandlungsraum der Grundausstattung"),

    /**
     * Der eigene Bereich für Krankengymnastik am Gerät.
     *
     * <p>Mindestens 30 m², je weiterem Gerät 4 m² mehr, dazu vier
     * Pflichtgeräte ({@code @fundstelle HM-RAUM-02}). Das ist die Anforderung
     * aus dem Beispiel im README: Ein Termin für Gerätetraining passt nicht in
     * jeden freien Raum.
     */
    GERAETEBEREICH("Bereich für Krankengymnastik am Gerät"),

    /**
     * Das Bewegungsbad.
     *
     * <p>Wasseroberfläche mindestens 12 m², kleinste Seitenlänge mindestens
     * 3 m, Wassertiefe höchstens 1,35 m, Wassertemperatur zwischen 28 und
     * 36 °C, dazu eine Dusche ({@code @fundstelle HM-RAUM-03}).
     *
     * <p>Nach der Behandlung im Bewegungsbad ist eine Nachruhe vorgesehen. Sie
     * blockiert den Raum, aber nicht die Therapeutin — das ist eine Frage der
     * Zeitplanung und gehört nicht hierher.
     */
    BEWEGUNGSBAD("Bewegungsbad");

    private final String bezeichnung;

    Raumanforderung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String bezeichnung() {
        return bezeichnung;
    }
}
