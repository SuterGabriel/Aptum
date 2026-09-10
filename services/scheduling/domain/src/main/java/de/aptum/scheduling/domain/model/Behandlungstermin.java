package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Ein durchgeführter Behandlungstermin auf einer Verordnung.
 *
 * <p>Das Kennzeichen begründet die Unterbrechung, die <em>vor</em> diesem
 * Termin lag. So steht es auch auf dem Verordnungsblatt: Die Therapeutin
 * vermerkt den Grund neben der Behandlung, mit der es weitergeht — nicht
 * neben der Lücke, denn eine Lücke hat keine Zeile.
 *
 * <p>Nur das Datum, keine Uhrzeit. Die Fristen der Verordnung rechnen auf
 * Kalendertagen. Die Uhrzeit gehört zum Kalendertermin, und der ist ein
 * anderes Ding als der Behandlungsnachweis auf dem Rezept.
 *
 * @param datum       der Tag der Behandlung
 * @param kennzeichen die Begründung für die vorangegangene Unterbrechung
 */
public record Behandlungstermin(LocalDate datum, Unterbrechungskennzeichen kennzeichen) {

    public Behandlungstermin {
        Objects.requireNonNull(datum, "datum");
        Objects.requireNonNull(kennzeichen, "kennzeichen");
    }

    /** Ein Termin ohne vermerkte Begründung. Der Regelfall. */
    public static Behandlungstermin an(LocalDate datum) {
        return new Behandlungstermin(datum, Unterbrechungskennzeichen.KEINES);
    }

    public boolean begruendetUnterbrechungBei(Therapieform therapieform) {
        return kennzeichen.begruendetBei(therapieform);
    }
}
