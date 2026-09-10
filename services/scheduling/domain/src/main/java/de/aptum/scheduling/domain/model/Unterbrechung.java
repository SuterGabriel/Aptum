package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Die Pause zwischen zwei Behandlungsterminen.
 *
 * <p>Gezählt wird in Kalendertagen zwischen den beiden Terminen. Liegt der
 * eine am 1. und der andere am 16., ist die Unterbrechung fünfzehn Tage lang.
 *
 * @param von         der Tag der letzten Behandlung vor der Pause
 * @param bis         der Tag der Behandlung, mit der es weitergeht
 * @param kennzeichen die Begründung, die für diese Pause vermerkt ist
 */
public record Unterbrechung(LocalDate von, LocalDate bis, Unterbrechungskennzeichen kennzeichen) {

    public Unterbrechung {
        Objects.requireNonNull(von, "von");
        Objects.requireNonNull(bis, "bis");
        Objects.requireNonNull(kennzeichen, "kennzeichen");
    }

    public long tage() {
        return ChronoUnit.DAYS.between(von, bis);
    }

    public boolean istBegruendetBei(Therapieform therapieform) {
        return kennzeichen.begruendetBei(therapieform);
    }
}
