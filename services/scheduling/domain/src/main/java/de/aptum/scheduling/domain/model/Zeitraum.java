package de.aptum.scheduling.domain.model;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Objects;

/**
 * Ein Abschnitt auf der Uhr, mit Zeitzone.
 *
 * <p>Fristen der Verordnung rechnen auf Kalendertagen und brauchen keine
 * Uhrzeit. Ein Termin dagegen hat eine, und sie gilt in Europe/Berlin — nicht
 * in UTC. Der Unterschied ist kein Detail: Eine Serie „jeden Dienstag 14:00"
 * bleibt bei 14:00 Ortszeit, auch über die Umstellung im Oktober hinweg. Wer
 * in UTC rechnet und stur 168 Stunden addiert, verschiebt den Termin um eine
 * Stunde. Genau dafür trägt jeder Zeitraum seine Zone mit.
 *
 * <p>Das Ende ist ausgeschlossen: Ein Termin von 14:00 bis 14:30 und einer
 * von 14:30 bis 15:00 überschneiden sich nicht. So denkt auch der Kalender.
 *
 * @param von Beginn, einschließlich
 * @param bis Ende, ausschließlich
 */
public record Zeitraum(ZonedDateTime von, ZonedDateTime bis) {

    /** Die Zeitzone der Praxis. Es gibt genau eine. */
    public static final ZoneId PRAXIS = ZoneId.of("Europe/Berlin");

    public Zeitraum {
        Objects.requireNonNull(von, "von");
        Objects.requireNonNull(bis, "bis");
        if (!bis.isAfter(von)) {
            throw new IllegalArgumentException("Ein Zeitraum endet nach seinem Beginn: " + von + " bis " + bis);
        }
    }

    public static Zeitraum ab(ZonedDateTime von, Duration dauer) {
        return new Zeitraum(von, von.plus(dauer));
    }

    public Duration dauer() {
        return Duration.between(von, bis);
    }

    public boolean ueberschneidet(Zeitraum anderer) {
        return von.isBefore(anderer.bis) && anderer.von.isBefore(bis);
    }

    /** Verlängert nach vorn und hinten, etwa um Rüstzeit. */
    public Zeitraum erweitertUm(Duration davor, Duration danach) {
        return new Zeitraum(von.minus(davor), bis.plus(danach));
    }
}
