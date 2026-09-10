package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.time.ZonedDateTime;
import java.util.List;

/**
 * Ist der Raum zu dieser Zeit frei?
 *
 * <p>Die Regel in zwei Sätzen: Ein Raum ist belegt, solange darin behandelt,
 * vorbereitet, aufgeräumt — oder geruht wird. Die Nachruhe nach dem
 * Bewegungsbad zählt hier mit, obwohl die Therapeutin währenddessen längst
 * frei ist.
 *
 * <p>Das ist die andere Hälfte der Asymmetrie zu {@link TherapeutVerfuegbar}.
 * Derselbe Termin belegt Raum und Therapeutin verschieden lang, und die beiden
 * Regeln fragen deshalb verschiedene Zeiträume ab. Was der Termin je Ressource
 * belegt, weiß der {@link Termin} selbst — die Regel rechnet es nicht nach.
 *
 * <p>Ob der Raum für das Heilmittel überhaupt <em>ausgestattet</em> ist,
 * beantwortet {@link RaumausstattungsGrenze}. Hier geht es nur um die Zeit.
 */
public final class RaumFrei {

    private static final String NAME = "Raum frei";

    public Pruefergebnis pruefe(
            Raum raum,
            Heilmittel heilmittel,
            List<Termin> bestehende,
            Zeitraum behandlung,
            Praxiseinstellung einstellung) {
        // Der neue Termin belegt den Raum genauso, wie er es als gebuchter
        // Termin täte - ein Platzhalter mit Raum und Heilmittel reicht dafür.
        Zeitraum belegt = new Termin(PLATZHALTER, raum, heilmittel, behandlung).belegtRaum(einstellung);

        for (Termin bestehend : bestehende) {
            if (!bestehend.raum().equals(raum)) {
                continue;
            }
            Zeitraum andere = bestehend.belegtRaum(einstellung);
            if (andere.ueberschneidet(belegt)) {
                String wodurch = bestehend.heilmittel().brauchtNachruhe()
                        ? bestehend.heilmittel().bezeichnung() + " einschließlich Nachruhe"
                        : bestehend.heilmittel().bezeichnung();
                return Pruefergebnis.verletzt(
                        NAME,
                        "%s ist von %s bis %s belegt: %s."
                                .formatted(raum.bezeichnung(), uhr(andere.von()), uhr(andere.bis()), wodurch));
            }
        }

        return Pruefergebnis.erfuellt(
                NAME, "%s ist von %s bis %s frei.".formatted(raum.bezeichnung(), uhr(belegt.von()), uhr(belegt.bis())));
    }

    /** Nur für die Belegungsrechnung; welche Person behandelt, spielt für den Raum keine Rolle. */
    private static final Therapeut PLATZHALTER = Therapeut.ohneZertifikate("–");

    private static String uhr(ZonedDateTime zeit) {
        return zeit.withZoneSameInstant(Zeitraum.PRAXIS).toLocalTime().toString();
    }
}
