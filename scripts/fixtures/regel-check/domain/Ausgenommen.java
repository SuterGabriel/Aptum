package fixture;

/** Erwartung: kein Befund. 0, 1 und 2 sind strukturell, nicht fachlich. */
public final class Ausgenommen {
    public int summe(int[] werte) {
        int s = 0;
        for (int i = 0; i < werte.length; i++) {
            s += werte[i] * 2 + 1;
        }
        return s;
    }
}
