package de.aptum.scheduling.domain.model;

/**
 * Der Buchstabe, mit dem eine Behandlungsunterbrechung auf dem
 * Verordnungsblatt begründet wird.
 *
 * <p>Die Konstanten heißen wie die Buchstaben auf dem Papier. Das ist
 * unüblich für Java, aber hier richtig: Wer eine Verordnung in der Hand hält,
 * liest dort ein T oder ein K, und jede Übersetzung erzeugt eine zweite
 * Sprache, in der die Regel nicht mehr wörtlich nachschlagbar ist.
 *
 * <p>{@link #KEINES} ist der Regelfall und ersetzt ein fehlendes Kennzeichen.
 * So kommt kein {@code null} über eine Modulgrenze und kein {@code Optional}
 * in ein Feld.
 *
 * <p>Fundstelle der Buchstaben: {@code HM-UNTBR-02}, § 7 Abs. 3a der Verträge
 * für Physio- und Ergotherapie.
 */
public enum Unterbrechungskennzeichen {

    /** Keine Begründung vermerkt. Der Regelfall. */
    KEINES("keine Begründung vermerkt", false),

    /** Therapeutisch indiziert. */
    T("therapeutisch indiziert", false),

    /** Krankheit auf einer der beiden Seiten. */
    K("Krankheit", false),

    /** Ferien oder Urlaub. */
    F("Ferien oder Urlaub", false),

    /**
     * Nur in der Ergotherapie zulässig.
     *
     * <p>Wofür das U steht, weisen die vorliegenden Quellen nicht aus. Der
     * Vertrag nennt den Buchstaben, nicht seine Bedeutung. Deshalb steht hier
     * kein erfundener Klartext — die Bezeichnung bleibt der Buchstabe, bis die
     * Bedeutung belegt ist.
     */
    U("nur Ergotherapie, Bedeutung nicht belegt", true);

    private final String beschreibung;
    private final boolean nurErgotherapie;

    Unterbrechungskennzeichen(String beschreibung, boolean nurErgotherapie) {
        this.beschreibung = beschreibung;
        this.nurErgotherapie = nurErgotherapie;
    }

    public String beschreibung() {
        return beschreibung;
    }

    /**
     * Begründet dieses Kennzeichen eine Unterbrechung bei der gegebenen
     * Therapieform?
     *
     * <p>Das U zählt nur in der Ergotherapie. Auf einer Physio-Verordnung ist
     * es kein gültiger Grund, sondern gar keiner.
     */
    public boolean begruendetBei(Therapieform therapieform) {
        if (this == KEINES) {
            return false;
        }
        return !nurErgotherapie || therapieform.istErgotherapie();
    }
}
