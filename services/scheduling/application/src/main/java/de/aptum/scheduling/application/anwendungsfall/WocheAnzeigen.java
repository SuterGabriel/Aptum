package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.kalender.Wochenansicht;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.port.TerminRepository;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

/**
 * Die Wochenansicht als Anwendungsfall: Termine der Woche laden, Stammdaten
 * dazu, und die Domäne fragen, was wo liegt.
 *
 * <p>Der Ausschnitt des Tages ist ein Praxisparameter wie das Raster der
 * Suche — keine Fachzahl, deshalb hier und nicht in der Domäne.
 */
public final class WocheAnzeigen {

    /** Erste Uhrzeit im Gitter. */
    private static final LocalTime TAGESBEGINN = LocalTime.of(7, 0);

    /** Letzte Uhrzeit im Gitter. */
    private static final LocalTime TAGESENDE = LocalTime.of(19, 0);

    private final TerminRepository termine;
    private final Stammdaten stammdaten;

    public WocheAnzeigen(TerminRepository termine, Stammdaten stammdaten) {
        this.termine = termine;
        this.stammdaten = stammdaten;
    }

    /**
     * @param montag      der Montag der Woche
     * @param tagesbeginn erste Uhrzeit im Gitter
     * @param tagesende   letzte Uhrzeit im Gitter
     * @param einstellung Rüstzeit und Nachruhe, damit das Gitter die Legende beschriften kann
     * @param spalten     je Therapeutin ihre Belegungen
     */
    public record Woche(
            LocalDate montag,
            LocalTime tagesbeginn,
            LocalTime tagesende,
            Praxiseinstellung einstellung,
            List<Wochenansicht.Spalte> spalten) {}

    /** Die Woche, in der der Tag liegt. Ein Dienstag ergibt dieselbe Woche wie ihr Montag. */
    public Woche ausfuehren(LocalDate tagInDerWoche) {
        LocalDate montag = tagInDerWoche.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        Zeitraum woche = new Zeitraum(
                montag.atStartOfDay(Zeitraum.PRAXIS),
                montag.with(TemporalAdjusters.next(DayOfWeek.MONDAY)).atStartOfDay(Zeitraum.PRAXIS));
        List<Termin> gebucht = termine.imZeitraum(woche);

        List<Wochenansicht.Spalte> spalten = Wochenansicht.berechne(new Wochenansicht.Anfrage(
                montag, stammdaten.therapeuten(), gebucht, stammdaten.einstellung(), TAGESBEGINN, TAGESENDE));
        return new Woche(montag, TAGESBEGINN, TAGESENDE, stammdaten.einstellung(), spalten);
    }
}
