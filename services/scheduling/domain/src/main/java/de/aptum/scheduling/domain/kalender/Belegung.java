package de.aptum.scheduling.domain.kalender;

import de.aptum.scheduling.domain.model.Zeitraum;
import java.util.Objects;
import java.util.Optional;

/**
 * Ein Block im Wochengitter: Art, Zeitraum, ein Text für Menschen und —
 * wo es einen gibt — der Raum.
 *
 * <p>Der Raum steht bei Behandlung und Nachruhe. Bei der Nachruhe ist er die
 * eigentliche Information: Die Spalte gehört der Therapeutin, aber gebunden
 * ist der Raum.
 *
 * @param art      was der Block ist
 * @param zeitraum von wann bis wann
 * @param text     Heilmittel, Grund der Abwesenheit, „außerhalb der Arbeitszeit"
 * @param raum     Bezeichnung des Raums, wenn der Block einen bindet
 */
public record Belegung(Belegungsart art, Zeitraum zeitraum, String text, Optional<String> raum) {

    public Belegung {
        Objects.requireNonNull(art, "art");
        Objects.requireNonNull(zeitraum, "zeitraum");
        Objects.requireNonNull(text, "text");
        Objects.requireNonNull(raum, "raum");
    }

    static Belegung ohneRaum(Belegungsart art, Zeitraum zeitraum, String text) {
        return new Belegung(art, zeitraum, text, Optional.empty());
    }

    static Belegung imRaum(Belegungsart art, Zeitraum zeitraum, String text, String raum) {
        return new Belegung(art, zeitraum, text, Optional.of(raum));
    }
}
