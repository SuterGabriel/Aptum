package de.aptum.scheduling.domain.suche;

import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.util.Objects;

/**
 * Ein Termin, der alle Regeln besteht — mit dem Bericht, der das belegt.
 *
 * <p>Der Bericht bleibt am Vorschlag. Die Trefferliste im Wireframe zeigt je
 * Vorschlag kleine Marker, welche Regeln geprüft wurden; ein Vorschlag ohne
 * seinen Bericht könnte das nicht.
 *
 * @param behandlung wann, die Zeit am Patienten
 * @param therapeut  wer
 * @param raum       wo
 * @param bericht    alle Regelergebnisse, keines verletzt, Warnungen möglich
 */
public record Vorschlag(Zeitraum behandlung, Therapeut therapeut, Raum raum, Pruefbericht bericht) {

    public Vorschlag {
        Objects.requireNonNull(behandlung, "behandlung");
        Objects.requireNonNull(therapeut, "therapeut");
        Objects.requireNonNull(raum, "raum");
        Objects.requireNonNull(bericht, "bericht");
        if (bericht.blockiert()) {
            throw new IllegalArgumentException("Ein Vorschlag hat keine verletzte Regel");
        }
    }

    public boolean hatWarnung() {
        return !bericht.warnungen().isEmpty();
    }
}
