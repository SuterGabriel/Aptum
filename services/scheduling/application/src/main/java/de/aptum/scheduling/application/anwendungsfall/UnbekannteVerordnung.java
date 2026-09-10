package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.domain.model.VerordnungId;

/**
 * Die Kennung gibt es nicht — oder nicht für diesen Mandanten. Der Unterschied
 * ist von außen nicht sichtbar, und das ist Absicht: Eine Antwort „gibt es,
 * gehört aber jemand anderem" wäre eine Auskunft über fremde Daten.
 */
public class UnbekannteVerordnung extends RuntimeException {

    public UnbekannteVerordnung(VerordnungId id) {
        super("Keine Verordnung mit der Kennung " + id.wert());
    }
}
