package de.aptum.scheduling.domain.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Was die Praxis selbst festlegt, weil die Quellen es offenlassen.
 *
 * <p>Zwei Zeiten, die gleich aussehen und Verschiedenes binden:
 *
 * <ul>
 *   <li>Die <strong>Rüstzeit</strong> ist Vor- und Nachbereitung um eine
 *       Behandlung herum. Sie bindet die Therapeutin <em>und</em> den Raum —
 *       wer den Raum vorbereitet, ist in dieser Zeit nicht frei.
 *   <li>Die <strong>Nachruhe</strong> ist die Ruhephase nach Bädern und
 *       Bewegungsbad. Sie bindet den Raum, aber nicht die Therapeutin: Der
 *       Patient ruht, die Therapeutin behandelt nebenan weiter.
 * </ul>
 *
 * <p>Beides sind Parameter und keine Konstanten, und das ist eine Entscheidung
 * aus der Quellenlage. In der Ergotherapie ist die Rüstzeit Teil der
 * Regelleistungszeit und steckt schon in der Dauer des Heilmittels; in der
 * Physiotherapie lässt der Vertragstext offen, ob sie zusätzlich einzuplanen
 * ist. Eine fest verdrahtete Rüstzeit wäre für eine der beiden Formen falsch.
 * Für die Nachruhe gibt es einen belegten Richtwert von 20 bis 25 Minuten
 * ({@code @fundstelle HM-ZEIT-16}); welchen Wert die Praxis darin wählt, ist
 * ihre Sache.
 *
 * @param ruestzeit Vor- und Nachbereitung je Behandlung, bindet Therapeutin und Raum
 * @param nachruhe  Ruhephase nach Bädern, bindet nur den Raum
 */
public record Praxiseinstellung(Duration ruestzeit, Duration nachruhe) {

    public Praxiseinstellung {
        Objects.requireNonNull(ruestzeit, "ruestzeit");
        Objects.requireNonNull(nachruhe, "nachruhe");
        if (ruestzeit.isNegative() || nachruhe.isNegative()) {
            throw new IllegalArgumentException("Zeiten sind nicht negativ: " + ruestzeit + ", " + nachruhe);
        }
    }

    /** Die untere Grenze des belegten Richtwerts für die Nachruhe. */
    public static Praxiseinstellung mitRuestzeit(Duration ruestzeit) {
        return new Praxiseinstellung(ruestzeit, NACHRUHE_RICHTWERT);
    }

    /** @fundstelle HM-ZEIT-16 */
    private static final Duration NACHRUHE_RICHTWERT = Duration.ofMinutes(20);
}
