package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Abwesenheit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Ist die Therapeutin zu dieser Zeit frei?
 *
 * <p>Die Regel in zwei Sätzen: Ein Termin liegt in der Arbeitszeit, an keinem
 * Abwesenheitstag, und er überschneidet sich mit keinem bestehenden Termin
 * derselben Person — einschließlich der Rüstzeit auf beiden Seiten. Das ist
 * die Dimension <em>Therapeut</em> aus dem README: Arbeitszeit minus
 * Abwesenheiten minus gebuchte Termine.
 *
 * <p>Was sie bewusst <em>nicht</em> bindet: die Nachruhe. Wenn ein Patient
 * nach dem Bewegungsbad ruht, behandelt die Therapeutin nebenan weiter. Wer
 * die Nachruhe hier mitrechnet, verschenkt Behandlungszeit — die Gegenseite
 * steht in {@link RaumFrei}.
 *
 * <p>Die Rüstzeit wird auf beiden Seiten des neuen Termins gerechnet: Er
 * belegt die Therapeutin von der Vorbereitung bis zum Aufräumen. Das ergibt
 * den Abstand, den zwei Termine derselben Person brauchen.
 */
public final class TherapeutVerfuegbar {

    private static final String NAME = "Therapeut verfügbar";

    public Pruefergebnis pruefe(
            Therapeut therapeut,
            Dienstplan dienstplan,
            List<Termin> bestehende,
            Zeitraum behandlung,
            Praxiseinstellung einstellung) {
        Zeitraum belegt = behandlung.erweitertUm(einstellung.ruestzeit(), einstellung.ruestzeit());

        if (!dienstplan.arbeitszeit().deckt(belegt)) {
            return Pruefergebnis.verletzt(
                    NAME,
                    "%s arbeitet am %s nicht von %s bis %s (einschließlich Rüstzeit)."
                            .formatted(
                                    therapeut.kuerzel(),
                                    belegt.von()
                                            .withZoneSameInstant(Zeitraum.PRAXIS)
                                            .getDayOfWeek(),
                                    uhr(belegt.von()),
                                    uhr(belegt.bis())));
        }

        Optional<Abwesenheit> abwesend = dienstplan.abwesenheitWaehrend(behandlung);
        if (abwesend.isPresent()) {
            return Pruefergebnis.verletzt(
                    NAME,
                    "%s ist abwesend: %s (%s bis %s)."
                            .formatted(
                                    therapeut.kuerzel(),
                                    abwesend.get().grund(),
                                    abwesend.get().von(),
                                    abwesend.get().bis()));
        }

        for (Termin bestehend : bestehende) {
            if (!bestehend.therapeut().equals(therapeut)) {
                continue;
            }
            Zeitraum andere = bestehend.belegtTherapeutin(einstellung);
            if (andere.ueberschneidet(belegt)) {
                return Pruefergebnis.verletzt(
                        NAME,
                        "%s hat von %s bis %s bereits %s (einschließlich Rüstzeit)."
                                .formatted(
                                        therapeut.kuerzel(),
                                        uhr(andere.von()),
                                        uhr(andere.bis()),
                                        bestehend.heilmittel().bezeichnung()));
            }
        }

        return Pruefergebnis.erfuellt(
                NAME,
                "%s ist von %s bis %s frei.".formatted(therapeut.kuerzel(), uhr(belegt.von()), uhr(belegt.bis())));
    }

    private static String uhr(ZonedDateTime zeit) {
        return zeit.withZoneSameInstant(Zeitraum.PRAXIS).toLocalTime().toString();
    }
}
