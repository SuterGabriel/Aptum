package de.aptum.scheduling.domain.model;

import java.time.Duration;

/**
 * Die verordnete Therapieleistung.
 *
 * <p>Das Heilmittel ist das Bindeglied der Slot-Berechnung. An ihm hängt, wie
 * lange ein Termin dauert, wer ihn erbringen darf und welchen Raum er braucht.
 * Ohne das Heilmittel lassen sich weder Therapeut noch Raum prüfen.
 *
 * <p>Die Behandlungszeiten sind Spannen, keine festen Werte. Der Katalog gibt
 * einen Zeitrichtwert je Leistung; die Mindestdauer darf nur aus medizinischen
 * Gründen unterschritten werden. Für die Slot-Länge ist die untere Grenze der
 * knappste zulässige Ansatz und die obere der realistische.
 *
 * <p><strong>Ein Unterschied zwischen den Therapieformen, der leicht
 * durchrutscht:</strong> In der Ergotherapie enthält die Regelleistungszeit
 * ausdrücklich 15 Minuten Vor- und Nachbereitung. In der Physiotherapie
 * beziehen sich die Zeitrichtwerte auf die Durchführung am Patienten, und ob
 * zusätzlich Rüstzeit einzuplanen ist, lässt der Vertragstext offen. Die
 * Rüstzeit ist deshalb ein Parameter je Praxis und keine Konstante — sie steht
 * bewusst nicht hier.
 */
public enum Heilmittel {

    // --- Physiotherapie ---------------------------------------------------

    /** @fundstelle HM-ZEIT-01 */
    KG_EINZEL("Krankengymnastik", Therapieform.PHYSIOTHERAPIE, 15, 25, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-02 */
    KG_GRUPPE("Krankengymnastik in der Gruppe", Therapieform.PHYSIOTHERAPIE, 20, 30, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-03 */
    MANUELLE_THERAPIE("Manuelle Therapie", Therapieform.PHYSIOTHERAPIE, 15, 25, Zertifikatsleistung.MANUELLE_THERAPIE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-04 */
    KLASSISCHE_MASSAGE("Klassische Massagetherapie", Therapieform.PHYSIOTHERAPIE, 15, 20, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-05 */
    MLD_TEILBEHANDLUNG("Manuelle Lymphdrainage, Teilbehandlung", Therapieform.PHYSIOTHERAPIE, 30, 30, Zertifikatsleistung.LYMPHDRAINAGE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-05 */
    MLD_GROSSBEHANDLUNG("Manuelle Lymphdrainage, Grossbehandlung", Therapieform.PHYSIOTHERAPIE, 45, 45, Zertifikatsleistung.LYMPHDRAINAGE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-05 */
    MLD_GANZBEHANDLUNG("Manuelle Lymphdrainage, Ganzbehandlung", Therapieform.PHYSIOTHERAPIE, 60, 60, Zertifikatsleistung.LYMPHDRAINAGE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-06 */
    KG_ZNS_ERWACHSENE("KG-ZNS, Erwachsene", Therapieform.PHYSIOTHERAPIE, 25, 35, Zertifikatsleistung.KG_ZNS_ERWACHSENE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-07 */
    KG_ZNS_KINDER("KG-ZNS, Kinder", Therapieform.PHYSIOTHERAPIE, 30, 45, Zertifikatsleistung.KG_ZNS_KINDER, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-08 */
    KG_GERAET("Krankengymnastik am Gerät", Therapieform.PHYSIOTHERAPIE, 60, 60, Zertifikatsleistung.GERAET, Raumanforderung.GERAETEBEREICH),

    /** @fundstelle HM-ZEIT-09 */
    KG_BEWEGUNGSBAD("Krankengymnastik im Bewegungsbad", Therapieform.PHYSIOTHERAPIE, 20, 30, Zertifikatsleistung.KEINE, Raumanforderung.BEWEGUNGSBAD),

    /** @fundstelle HM-ZEIT-10 */
    WARMPACKUNG("Warmpackung", Therapieform.PHYSIOTHERAPIE, 20, 30, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    // --- Ergotherapie -----------------------------------------------------
    // Die Zeiten sind Regelleistungszeiten und enthalten 15 Minuten Vor- und
    // Nachbereitung. Anders als bei der Physiotherapie ist das ausdrücklich
    // Teil der Angabe.

    /** @fundstelle HM-ZEIT-11 */
    ERGO_MOTORISCH_FUNKTIONELL("Motorisch-funktionelle Behandlung", Therapieform.ERGOTHERAPIE, 45, 45, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-12 */
    ERGO_SENSOMOTORISCH("Sensomotorisch-perzeptive Behandlung", Therapieform.ERGOTHERAPIE, 60, 60, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-13 */
    ERGO_HIRNLEISTUNGSTRAINING("Hirnleistungstraining", Therapieform.ERGOTHERAPIE, 45, 45, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-14 */
    ERGO_PSYCHISCH_FUNKTIONELL("Psychisch-funktionelle Behandlung", Therapieform.ERGOTHERAPIE, 75, 75, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG),

    /** @fundstelle HM-ZEIT-15 */
    ERGO_BERATUNG_UMFELD("Beratung des Umfelds", Therapieform.ERGOTHERAPIE, 120, 120, Zertifikatsleistung.KEINE, Raumanforderung.GRUNDAUSSTATTUNG);

    private final String bezeichnung;
    private final Therapieform therapieform;
    private final Duration mindestdauer;
    private final Duration regeldauer;
    private final Zertifikatsleistung zertifikatsleistung;
    private final Raumanforderung raumanforderung;

    Heilmittel(String bezeichnung, Therapieform therapieform,
               int mindestminuten, int regelminuten,
               Zertifikatsleistung zertifikatsleistung,
               Raumanforderung raumanforderung) {
        this.bezeichnung = bezeichnung;
        this.therapieform = therapieform;
        this.mindestdauer = Duration.ofMinutes(mindestminuten);
        this.regeldauer = Duration.ofMinutes(regelminuten);
        this.zertifikatsleistung = zertifikatsleistung;
        this.raumanforderung = raumanforderung;
    }

    public String bezeichnung() {
        return bezeichnung;
    }

    public Therapieform therapieform() {
        return therapieform;
    }

    /** Die untere Grenze des Zeitrichtwerts. */
    public Duration mindestdauer() {
        return mindestdauer;
    }

    /** Die obere Grenze des Zeitrichtwerts, der realistische Ansatz für einen Slot. */
    public Duration regeldauer() {
        return regeldauer;
    }

    public Zertifikatsleistung zertifikatsleistung() {
        return zertifikatsleistung;
    }

    /** Was dieses Heilmittel vom Raum verlangt. */
    public Raumanforderung raumanforderung() {
        return raumanforderung;
    }

    public boolean brauchtAbrechnungserlaubnis() {
        return zertifikatsleistung.istZertifikatspflichtig();
    }
}
