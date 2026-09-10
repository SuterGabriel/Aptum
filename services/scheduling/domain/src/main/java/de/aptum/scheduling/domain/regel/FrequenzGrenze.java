package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

/**
 * Würde dieser Termin die verordnete Häufigkeit überschreiten?
 *
 * <p>Die Regel in zwei Sätzen: Die Frequenzangabe der Verordnung ist für die
 * Praxis bindend. Wer davon abweichen will, braucht die vorherige Absprache mit
 * der verordnenden Person, und die Änderung gehört auf den Vordruck.
 *
 * <p><strong>Die einzige Regel im Modell, die warnt statt zu blockieren.</strong>
 * Über eine Frequenzabweichung entscheidet ein Mensch nach Rücksprache, nicht
 * die Software. Ein {@code VERLETZT} wäre hier fachlich falsch: Es würde eine
 * Buchung verhindern, die nach einem Telefonat mit der Praxis völlig zulässig
 * ist. Diese Regel liefert deshalb nie mehr als eine
 * {@link Pruefergebnis.Ausgang#WARNUNG}.
 *
 * <p><strong>Warum hier keine einzige Zahl steht.</strong> Eine prozentuale
 * Toleranz gibt es in der Richtlinie nicht — das ist ein belegter Nichtfund
 * ({@code @fundstelle HM-FREQ-05}). Es gibt also keine Karenz, die man
 * implementieren könnte. Verbindlich ist allein die Angabe auf dem Vordruck,
 * und die kommt aus der Verordnung. Die Regel hat deshalb keine Konstante.
 *
 * <p><strong>Was eine Woche ist, sagt die Quelle nicht.</strong> Diese Regel
 * legt sie als Kalenderwoche von Montag bis Sonntag aus, weil eine Praxis ihre
 * Frequenz am Wochenplan abliest und nicht in rollenden Sieben-Tage-Fenstern.
 * Ein rollendes Fenster wäre strenger. Die Auslegung ist hier benannt und hat
 * einen eigenen Testfall.
 *
 * <p><strong>Was diese Regel bewusst nicht prüft:</strong> die Unterschreitung.
 * Ob zu selten behandelt wurde, lässt sich am Buchungszeitpunkt nicht sagen —
 * die Woche ist noch nicht vorbei. Zudem hält der Vertragstext ausdrücklich
 * fest, dass eine Unterbrechung keine Frequenzabweichung ist
 * ({@code @fundstelle HM-UNTBR-07}). Eine Lücke im Verlauf darf hier also gar
 * keine Meldung erzeugen.
 *
 * <p>Fundstellen der Bindungswirkung und des Abweichungswegs:
 * {@code @fundstelle HM-FREQ-01}, {@code @fundstelle HM-FREQ-02}.
 */
public final class FrequenzGrenze {

    private static final String NAME = "Frequenz";

    public Pruefergebnis pruefe(Verordnung verordnung, Behandlungsverlauf verlauf, LocalDate geplanterTermin) {
        LocalDate wochenbeginn = geplanterTermin.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate naechsteWoche = wochenbeginn.plusWeeks(1);

        long bereitsInDieserWoche = verlauf.termine().stream()
                .map(Behandlungstermin::datum)
                .filter(datum -> !datum.isBefore(wochenbeginn) && datum.isBefore(naechsteWoche))
                .count();

        long waereDer = bereitsInDieserWoche + 1;
        String lage = "Verordnet %s, dies wäre die %d. Behandlung in der Woche ab %s"
                .formatted(verordnung.frequenz().beschreibung(), waereDer, wochenbeginn);

        if (waereDer <= verordnung.frequenz().maxProWoche()) {
            return Pruefergebnis.erfuellt(NAME, lage + ".");
        }

        return Pruefergebnis.warnung(
                NAME,
                lage
                        + ". Eine Abweichung ist nur nach vorheriger Absprache mit der "
                        + "verordnenden Person zulässig und auf dem Vordruck zu dokumentieren.");
    }
}
