package de.aptum.scheduling.domain.suche;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.Wunschfenster;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Die vier Dimensionen, so wie die Terminsuche sie abfragt.
 *
 * <p>Verordnung und Verlauf, das Heilmittel, das Wunschfenster des Patienten,
 * die Personen mit ihren Dienstplänen, die Räume, der bestehende Kalender.
 * Dazu das Raster, in dem Beginnzeiten vorgeschlagen werden — ein Parameter
 * der Praxis, keine Fachzahl.
 *
 * @param verordnung   welche Verordnung gebucht wird
 * @param verlauf      was darauf schon erbracht ist
 * @param heilmittel   was behandelt wird
 * @param wunsch       wann der Patient kann
 * @param therapeuten  wer in Frage kommt, je mit Dienstplan
 * @param raeume       welche Räume in Frage kommen
 * @param bestehende   der Kalender, gegen den geprüft wird
 * @param einstellung  Rüstzeit und Nachruhe der Praxis
 * @param raster       Abstand zwischen zwei vorgeschlagenen Beginnzeiten
 */
public record Suchanfrage(
        Verordnung verordnung,
        Behandlungsverlauf verlauf,
        Heilmittel heilmittel,
        Wunschfenster wunsch,
        Map<Therapeut, Dienstplan> therapeuten,
        List<Raum> raeume,
        List<Termin> bestehende,
        Praxiseinstellung einstellung,
        Duration raster) {

    public Suchanfrage {
        Objects.requireNonNull(verordnung, "verordnung");
        Objects.requireNonNull(verlauf, "verlauf");
        Objects.requireNonNull(heilmittel, "heilmittel");
        Objects.requireNonNull(wunsch, "wunsch");
        Objects.requireNonNull(therapeuten, "therapeuten");
        Objects.requireNonNull(raeume, "raeume");
        Objects.requireNonNull(bestehende, "bestehende");
        Objects.requireNonNull(einstellung, "einstellung");
        Objects.requireNonNull(raster, "raster");
        if (raster.isZero() || raster.isNegative()) {
            throw new IllegalArgumentException("Das Raster ist positiv: " + raster);
        }
        therapeuten = Map.copyOf(therapeuten);
        raeume = List.copyOf(raeume);
        bestehende = List.copyOf(bestehende);
    }
}
