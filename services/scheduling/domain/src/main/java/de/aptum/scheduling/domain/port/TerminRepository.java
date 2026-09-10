package de.aptum.scheduling.domain.port;

import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.TerminId;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.util.List;

/**
 * Was die Domäne vom Kalender braucht: die Termine in einem Zeitraum, und die
 * Möglichkeit, einen dazuzulegen.
 *
 * <p>Die Suche lädt den Zeitraum des Wunschfensters einmal und prüft dann im
 * Speicher. Für eine Praxis mit einer Handvoll Räumen ist das die richtige
 * Größenordnung — ADR-001 nennt die Grenze, ab der die Vorauswahl in die
 * Datenbank gehörte.
 */
public interface TerminRepository {

    /** Alle Termine, deren Behandlung den Zeitraum berührt. */
    List<Termin> imZeitraum(Zeitraum zeitraum);

    void speichere(TerminId id, Termin termin);
}
