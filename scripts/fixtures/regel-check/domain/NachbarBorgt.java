package fixture;

/**
 * Erwartung: Befund für 77, nicht für 28.
 *
 * Die zweite Konstante hat keine Fundstelle. Vor der Korrektur borgte sie
 * sich die der ersten, weil sie im Drei-Zeilen-Fenster lag. Stumme Lücke
 * Nummer eins.
 */
public final class NachbarBorgt {
    /** @fundstelle HM-FRIST-01 */
    private static final int A = 28;

    private static final int B = 77;
}
