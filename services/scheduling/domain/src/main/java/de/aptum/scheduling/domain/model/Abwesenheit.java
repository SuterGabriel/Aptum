package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Ein Zeitraum, in dem eine Therapeutin nicht behandelt: Urlaub,
 * Fortbildung, Krankheit.
 *
 * <p>Ganze Kalendertage, beide Grenzen einschließlich. Eine halbtägige
 * Abwesenheit wäre eine Änderung der Arbeitszeit an diesem Tag, keine
 * Abwesenheit — das Modell hält die beiden Dinge getrennt, weil sie sich
 * verschieden verhalten: Abwesenheiten überlagern sich, Arbeitszeiten nicht.
 *
 * <p>Der Grund ist Freitext für die Oberfläche. Er entscheidet nichts.
 *
 * @param von   erster Tag der Abwesenheit
 * @param bis   letzter Tag der Abwesenheit, einschließlich
 * @param grund wofür, in Klartext
 */
public record Abwesenheit(LocalDate von, LocalDate bis, String grund) {

    public Abwesenheit {
        Objects.requireNonNull(von, "von");
        Objects.requireNonNull(bis, "bis");
        Objects.requireNonNull(grund, "grund");
        if (bis.isBefore(von)) {
            throw new IllegalArgumentException("Abwesenheit endet vor ihrem Beginn: " + von + " bis " + bis);
        }
    }

    public static Abwesenheit am(LocalDate tag, String grund) {
        return new Abwesenheit(tag, tag, grund);
    }

    public boolean umfasst(LocalDate tag) {
        return !tag.isBefore(von) && !tag.isAfter(bis);
    }

    /** Berührt der Zeitraum einen Tag der Abwesenheit? Gerechnet in der Zone der Praxis. */
    public boolean beruehrt(Zeitraum zeitraum) {
        LocalDate erster = zeitraum.von().withZoneSameInstant(Zeitraum.PRAXIS).toLocalDate();
        LocalDate letzter = zeitraum.bis().withZoneSameInstant(Zeitraum.PRAXIS).minusNanos(1).toLocalDate();
        return !letzter.isBefore(von) && !erster.isAfter(bis);
    }
}
