package de.aptum.scheduling.domain.model;

/**
 * Der Kurzschlüssel des Heilmittelkatalogs, der eine Verordnung einordnet.
 *
 * <p>An der Diagnosegruppe hängen zwei Mengen, die gleich aussehen und
 * verschieden binden:
 *
 * <ul>
 *   <li>Die <strong>Höchstmenge je Verordnung</strong> ist eine harte Grenze.
 *       Mehr Einheiten dürfen nicht auf einem Rezept stehen.
 *   <li>Die <strong>orientierende Behandlungsmenge</strong> ist ein Richtwert.
 *       Sie zu überschreiten ist ausdrücklich zulässig; weitere Verordnungen
 *       im selben Verordnungsfall sind möglich.
 * </ul>
 *
 * <p>Wer die zweite als Grenze behandelt, baut eine Regel, die es nicht gibt.
 * Deshalb prüft {@link de.aptum.scheduling.domain.regel.HoechstmengeGrenze}
 * nur die erste.
 *
 * <p>Die Gruppe bestimmt außerdem die Therapieform. Es gibt keine
 * Physio-Verordnung mit ergotherapeutischer Diagnosegruppe, und deshalb trägt
 * die Verordnung die Therapieform nicht als eigenes Feld — ein Feld, das man
 * nicht hat, kann nicht widersprüchlich gefüllt werden.
 *
 * <p><strong>Vollständigkeit:</strong> Hier stehen die Gruppen, die im
 * Regelkatalog namentlich belegt sind. Der Heilmittelkatalog kennt weitere.
 * Die Physio-Höchstmenge ist dort mit „in der Regel" angegeben, also mit einem
 * Vorbehalt — deshalb liegen die Werte je Gruppe und nicht als Formel vor. Eine
 * Korrektur ist damit eine Zeile.
 */
public enum Diagnosegruppe {

    // Physiotherapie
    WS(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    EX(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    CS(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    AT(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    GE(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    SO(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO),
    LY(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO, Menge.ORIENTIEREND_PHYSIO_HOCH),
    ZN(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO_ZNS, Menge.ORIENTIEREND_PHYSIO_HOCH),
    PN(Therapieform.PHYSIOTHERAPIE, Menge.HOECHST_PHYSIO_ZNS, Menge.ORIENTIEREND_PHYSIO_HOCH),

    // Ergotherapie
    SB1(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO, Menge.ORIENTIEREND_ERGO_SB1),
    SB2(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO, Menge.ORIENTIEREND_ERGO_SB2),
    EN1(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO, Menge.ORIENTIEREND_ERGO_HOCH),
    EN2(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO, Menge.ORIENTIEREND_ERGO_HOCH),
    PS2(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO_PSYCH, Menge.ORIENTIEREND_ERGO_HOCH),
    PS3(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO_PSYCH, Menge.ORIENTIEREND_ERGO_HOCH),
    PS4(Therapieform.ERGOTHERAPIE, Menge.HOECHST_ERGO, Menge.ORIENTIEREND_ERGO_HOCH);

    /**
     * Die Zahlen aus dem Heilmittelkatalog, jede mit ihrer Fundstelle.
     *
     * <p>Sie stehen in einem verschachtelten Interface, weil eine
     * Enum-Konstante keine statischen Felder ihrer eigenen Enum verwenden darf.
     * Ohne diesen Umweg müssten die Werte roh in der Konstantenliste stehen,
     * und dann trüge keine von ihnen eine Fundstelle.
     */
    private interface Menge {

        /** @fundstelle HM-MENGE-01 */
        int HOECHST_PHYSIO = 6;

        /** @fundstelle HM-MENGE-01 */
        int HOECHST_PHYSIO_ZNS = 10;

        /** @fundstelle HM-MENGE-02 */
        int HOECHST_ERGO = 10;

        /** @fundstelle HM-MENGE-02 */
        int HOECHST_ERGO_PSYCH = 20;

        /** @fundstelle HM-MENGE-03 */
        int ORIENTIEREND_PHYSIO = 18;

        /** @fundstelle HM-MENGE-03 */
        int ORIENTIEREND_PHYSIO_HOCH = 30;

        /** @fundstelle HM-MENGE-04 */
        int ORIENTIEREND_ERGO_SB1 = 20;

        /** @fundstelle HM-MENGE-04 */
        int ORIENTIEREND_ERGO_SB2 = 30;

        /** @fundstelle HM-MENGE-04 */
        int ORIENTIEREND_ERGO_HOCH = 40;
    }

    private final Therapieform therapieform;
    private final int hoechstmengeJeVerordnung;
    private final int orientierendeBehandlungsmenge;

    Diagnosegruppe(Therapieform therapieform, int hoechstmenge, int orientierendeMenge) {
        this.therapieform = therapieform;
        this.hoechstmengeJeVerordnung = hoechstmenge;
        this.orientierendeBehandlungsmenge = orientierendeMenge;
    }

    public Therapieform therapieform() {
        return therapieform;
    }

    /** Harte Grenze: mehr Einheiten dürfen nicht auf einem Rezept stehen. */
    public int hoechstmengeJeVerordnung() {
        return hoechstmengeJeVerordnung;
    }

    /**
     * Richtwert, keine Grenze. Wird bewusst von keiner Regel als Obergrenze
     * ausgewertet.
     */
    public int orientierendeBehandlungsmenge() {
        return orientierendeBehandlungsmenge;
    }
}
