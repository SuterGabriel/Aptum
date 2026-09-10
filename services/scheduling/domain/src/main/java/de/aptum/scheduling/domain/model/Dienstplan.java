package de.aptum.scheduling.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Wann eine Therapeutin grundsätzlich da ist: regelmäßige Arbeitszeit minus
 * Abwesenheiten.
 *
 * <p>Getrennt vom {@link Therapeut}, weil die Person und ihr Plan verschieden
 * schnell wechseln. Die Abrechnungserlaubnis hat sie für Jahre, den Urlaub
 * für zwei Wochen. Ein Typ, der beides trägt, müsste bei jedem Urlaubsantrag
 * neu gebaut werden.
 *
 * @param arbeitszeit   die regelmäßige Arbeitszeit je Wochentag
 * @param abwesenheiten Urlaub, Fortbildung, Krankheit
 */
public record Dienstplan(Arbeitszeit arbeitszeit, List<Abwesenheit> abwesenheiten) {

    public Dienstplan {
        Objects.requireNonNull(arbeitszeit, "arbeitszeit");
        Objects.requireNonNull(abwesenheiten, "abwesenheiten");
        abwesenheiten = List.copyOf(abwesenheiten);
    }

    public static Dienstplan mit(Arbeitszeit arbeitszeit) {
        return new Dienstplan(arbeitszeit, List.of());
    }

    public Dienstplan abwesend(Abwesenheit abwesenheit) {
        List<Abwesenheit> neu = new ArrayList<>(abwesenheiten);
        neu.add(abwesenheit);
        return new Dienstplan(arbeitszeit, neu);
    }

    /** Die erste Abwesenheit, die den Zeitraum berührt — falls es eine gibt. */
    public Optional<Abwesenheit> abwesenheitWaehrend(Zeitraum zeitraum) {
        return abwesenheiten.stream().filter((a) -> a.beruehrt(zeitraum)).findFirst();
    }
}
