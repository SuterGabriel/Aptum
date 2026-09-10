package de.aptum.scheduling.domain.model;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Die regelmäßige Arbeitszeit einer Therapeutin je Wochentag.
 *
 * <p>Ein Tag ohne Eintrag ist ein freier Tag. Mehr als ein Block je Tag —
 * eine Mittagspause — kennt das Modell noch nicht; das kommt, wenn eine
 * Regel es braucht, nicht vorher.
 *
 * <p>Die Zeiten sind Ortszeiten ohne Datum. Ob ein konkreter Zeitraum in die
 * Arbeitszeit fällt, wird gegen den Wochentag seines Beginns geprüft, in der
 * Zone der Praxis.
 */
public final class Arbeitszeit {

    /** Ein Block von Beginn bis Ende, Ende ausgeschlossen. */
    public record Block(LocalTime von, LocalTime bis) {
        public Block {
            Objects.requireNonNull(von, "von");
            Objects.requireNonNull(bis, "bis");
            if (!bis.isAfter(von)) {
                throw new IllegalArgumentException("Arbeitszeit endet nach ihrem Beginn: " + von + " bis " + bis);
            }
        }
    }

    private final Map<DayOfWeek, Block> bloecke;

    private Arbeitszeit(Map<DayOfWeek, Block> bloecke) {
        this.bloecke = bloecke;
    }

    public static Arbeitszeit keine() {
        return new Arbeitszeit(new EnumMap<>(DayOfWeek.class));
    }

    /** Montag bis Freitag dieselben Zeiten. Der Regelfall. */
    public static Arbeitszeit werktags(LocalTime von, LocalTime bis) {
        Arbeitszeit a = keine();
        for (DayOfWeek tag : DayOfWeek.values()) {
            if (tag != DayOfWeek.SATURDAY && tag != DayOfWeek.SUNDAY) {
                a = a.mit(tag, von, bis);
            }
        }
        return a;
    }

    public Arbeitszeit mit(DayOfWeek tag, LocalTime von, LocalTime bis) {
        Map<DayOfWeek, Block> neu = new EnumMap<>(bloecke);
        neu.put(tag, new Block(von, bis));
        return new Arbeitszeit(neu);
    }

    public Optional<Block> am(DayOfWeek tag) {
        return Optional.ofNullable(bloecke.get(tag));
    }

    /** Liegt der ganze Zeitraum innerhalb der Arbeitszeit seines Tages? */
    public boolean deckt(Zeitraum zeitraum) {
        var lokalVon = zeitraum.von().withZoneSameInstant(Zeitraum.PRAXIS);
        var lokalBis = zeitraum.bis().withZoneSameInstant(Zeitraum.PRAXIS);
        if (!lokalVon.toLocalDate().equals(lokalBis.toLocalDate())) {
            return false; // über Mitternacht hinaus ist keine Arbeitszeit
        }
        return am(lokalVon.getDayOfWeek())
                .map((block) -> !lokalVon.toLocalTime().isBefore(block.von())
                        && !lokalBis.toLocalTime().isAfter(block.bis()))
                .orElse(false);
    }
}
