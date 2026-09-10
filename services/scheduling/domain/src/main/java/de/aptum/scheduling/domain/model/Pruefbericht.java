package de.aptum.scheduling.domain.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Das Ergebnis aller Regeln für einen Kandidaten, in der Reihenfolge der
 * Prüfung.
 *
 * <p>Ein Bericht blockiert, sobald eine Regel verletzt ist. Warnungen
 * blockieren nicht — sie gehören zum Vorschlag dazu, damit die Oberfläche sie
 * neben den Termin schreiben kann. Genau die drei Zustände, die der
 * Buchungsdialog zeigt.
 *
 * @param ergebnisse ein Ergebnis je Regel, keine ausgelassen
 */
public record Pruefbericht(List<Pruefergebnis> ergebnisse) {

    public Pruefbericht {
        Objects.requireNonNull(ergebnisse, "ergebnisse");
        ergebnisse = List.copyOf(ergebnisse);
    }

    public boolean blockiert() {
        return ergebnisse.stream().anyMatch((e) -> e.ausgang() == Pruefergebnis.Ausgang.VERLETZT);
    }

    public List<Pruefergebnis> warnungen() {
        return ergebnisse.stream().filter((e) -> e.ausgang() == Pruefergebnis.Ausgang.WARNUNG).toList();
    }

    /** Die erste verletzte Regel — der Grund, den die Oberfläche nennt. */
    public Optional<Pruefergebnis> ersterVerstoss() {
        return ergebnisse.stream().filter((e) -> e.ausgang() == Pruefergebnis.Ausgang.VERLETZT).findFirst();
    }
}
