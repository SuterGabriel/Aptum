package de.aptum.scheduling.domain.model;

/**
 * Die auf der Verordnung angegebene Behandlungshäufigkeit.
 *
 * <p>Die Richtlinie lässt ausdrücklich eine Spanne zu — „1–3x wöchentlich" ist
 * der Regelwert der meisten Diagnosegruppen. Eine feste Angabe ist deshalb
 * kein eigener Typ, sondern eine Spanne, bei der beide Enden gleich sind.
 *
 * <p>Gezählt wird ausschließlich je Woche. Die vorliegenden Quellen kennen
 * keine andere Bezugsgröße, und eine erfundene wäre schlimmer als eine
 * fehlende: Der Feldname sagt deshalb, worauf er sich bezieht, statt eine
 * Einheit offenzulassen, die niemand belegt hat.
 *
 * @param minProWoche die untere Grenze der verordneten Häufigkeit
 * @param maxProWoche die obere Grenze, bei fester Angabe gleich der unteren
 */
public record Frequenz(int minProWoche, int maxProWoche) {

    public Frequenz {
        if (minProWoche < 1) {
            throw new IllegalArgumentException(
                    "Eine Frequenz unter einer Behandlung je Woche ist keine Frequenz: "
                            + minProWoche);
        }
        if (maxProWoche < minProWoche) {
            throw new IllegalArgumentException(
                    "Die obere Grenze liegt unter der unteren: "
                            + minProWoche + " bis " + maxProWoche);
        }
    }

    /** Eine feste Angabe, etwa „2x wöchentlich". */
    public static Frequenz genau(int proWoche) {
        return new Frequenz(proWoche, proWoche);
    }

    /** Eine Spanne, etwa „1–3x wöchentlich". */
    public static Frequenz spanne(int minProWoche, int maxProWoche) {
        return new Frequenz(minProWoche, maxProWoche);
    }

    public boolean istSpanne() {
        return maxProWoche > minProWoche;
    }

    /** Die Angabe so, wie sie auf dem Vordruck steht. */
    public String beschreibung() {
        return istSpanne()
                ? "%d-%dx wöchentlich".formatted(minProWoche, maxProWoche)
                : "%dx wöchentlich".formatted(minProWoche);
    }
}
