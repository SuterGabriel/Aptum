package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;

/**
 * Ist auf der Verordnung noch eine Behandlungseinheit offen?
 *
 * <p>Die Regel in zwei Sätzen: Eine Verordnung trägt eine feste Anzahl
 * Behandlungseinheiten. Sind alle erbracht, kann auf diese Verordnung nicht
 * weiter gebucht werden.
 *
 * <p>Das ist die einfachste Regel im Modell und trotzdem die, die im
 * Praxisalltag am häufigsten greift. Sie liefert die Zahl, die in der
 * Terminsuche als „4 von 10" steht — und die Begründung nennt beide Werte,
 * damit die Rezeption am Telefon sagen kann, wie viele Termine noch zu
 * vergeben sind.
 *
 * <p><strong>Warum keine Fundstelle:</strong> Hier steht keine Zahl aus der
 * Richtlinie. Die Menge kommt von der Verordnung selbst, gezählt wird der
 * Behandlungsverlauf. Ob die verordnete Menge überhaupt zulässig war, prüft
 * {@link HoechstmengeGrenze} — das ist eine andere Frage zu einem anderen
 * Zeitpunkt.
 */
public final class RestkontingentGrenze {

    private static final String NAME = "Restkontingent";

    public Pruefergebnis pruefe(Verordnung verordnung, Behandlungsverlauf verlauf) {
        int verordnet = verordnung.verordneteEinheiten();
        int erbracht = verlauf.anzahlBehandlungen();
        int rest = verordnet - erbracht;

        if (rest <= 0) {
            return Pruefergebnis.verletzt(
                    NAME,
                    ("Alle %d verordneten Einheiten sind erbracht, das Restkontingent ist "
                                    + "aufgebraucht. Für weitere Behandlungen braucht es eine neue "
                                    + "Verordnung.")
                            .formatted(verordnet));
        }

        return Pruefergebnis.erfuellt(NAME, "Noch %d von %d Einheiten offen.".formatted(rest, verordnet));
    }
}
