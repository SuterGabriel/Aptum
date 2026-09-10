package de.aptum.scheduling.domain.model;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Die dritte Dimension: wann der Patient kann.
 *
 * <p>Ein Zeitraum in Tagen, eine Tageszeit und die Wochentage, die in Frage
 * kommen. Mehr braucht die Suche nicht — „vormittags, dienstags oder
 * donnerstags, in den nächsten zwei Wochen" ist genau das, was am Telefon
 * gesagt wird.
 *
 * @param von         erster Tag, einschließlich
 * @param bis         letzter Tag, einschließlich
 * @param fruehestens früheste Uhrzeit für den Behandlungsbeginn
 * @param spaetestens späteste Uhrzeit für das Behandlungsende
 * @param wochentage  welche Tage überhaupt
 */
public record Wunschfenster(
        LocalDate von, LocalDate bis, LocalTime fruehestens, LocalTime spaetestens, Set<DayOfWeek> wochentage) {

    public Wunschfenster {
        Objects.requireNonNull(von, "von");
        Objects.requireNonNull(bis, "bis");
        Objects.requireNonNull(fruehestens, "fruehestens");
        Objects.requireNonNull(spaetestens, "spaetestens");
        Objects.requireNonNull(wochentage, "wochentage");
        if (bis.isBefore(von)) {
            throw new IllegalArgumentException("Wunschfenster endet vor seinem Beginn: " + von + " bis " + bis);
        }
        if (!spaetestens.isAfter(fruehestens)) {
            throw new IllegalArgumentException(
                    "Tageszeit endet vor ihrem Beginn: " + fruehestens + " bis " + spaetestens);
        }
        wochentage = Set.copyOf(wochentage);
    }

    /** Jeder Werktag zwischen den beiden Daten, zu jeder Tageszeit. */
    public static Wunschfenster werktags(LocalDate von, LocalDate bis) {
        return new Wunschfenster(
                von, bis, LocalTime.MIN, LocalTime.MAX, EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY));
    }

    public Wunschfenster zwischen(LocalTime fruehestens, LocalTime spaetestens) {
        return new Wunschfenster(von, bis, fruehestens, spaetestens, wochentage);
    }

    public Wunschfenster an(DayOfWeek... tage) {
        return new Wunschfenster(von, bis, fruehestens, spaetestens, Set.of(tage));
    }

    public boolean erlaubt(LocalDate tag) {
        return !tag.isBefore(von) && !tag.isAfter(bis) && wochentage.contains(tag.getDayOfWeek());
    }
}
