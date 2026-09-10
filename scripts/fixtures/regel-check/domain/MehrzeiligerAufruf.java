package fixture;

/**
 * Erwartung: kein Befund.
 *
 * So sieht eine Enum-Konstante aus, nachdem der Formatter sie umgebrochen
 * hat: jede Argumentzeile endet mit einem Komma. Die Fundstelle steht über
 * dem Aufruf und gilt für alle Zahlen darin. Ein Komma innerhalb einer
 * Klammer ist kein Anweisungsende.
 */
public enum MehrzeiligerAufruf {
    /** @fundstelle HM-FRIST-01 */
    REGELFALL(
            "Behandlungsbeginn",
            28,
            28),

    /** @fundstelle HM-FRIST-02 */
    DRINGLICH(
            "dringlicher Bedarf",
            14,
            14);

    MehrzeiligerAufruf(String name, int a, int b) {}
}
