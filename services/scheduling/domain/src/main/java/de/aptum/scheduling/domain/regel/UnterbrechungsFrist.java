package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Unterbrechung;
import de.aptum.scheduling.domain.model.Verordnung;

/**
 * Wurde die Behandlung zu lange unterbrochen, ohne dass ein Grund vermerkt ist?
 *
 * <p>Die Regel in zwei Sätzen: Eine Pause von mehr als 14 Kalendertagen
 * zwischen zwei Behandlungen lässt die Verordnung für die Restmenge verfallen.
 * Ist auf dem Verordnungsblatt ein zulässiges Kennzeichen vermerkt, verfällt
 * sie nicht.
 *
 * <p>Diese Regel gilt für beide Therapieformen gleich. Die Unterschiede
 * zwischen Physio- und Ergotherapie liegen in {@link UnterbrechungsSumme} und
 * {@link VerordnungsGueltigkeit} — dort trennen sich die Wege.
 *
 * <p>Eine Feinheit aus dem Vertragstext: Das Kennzeichen U ist nur in der
 * Ergotherapie zulässig. Auf einer Physio-Verordnung ist es kein schlechter
 * Grund, sondern gar keiner, und die Unterbrechung gilt als unbegründet.
 */
public final class UnterbrechungsFrist {

    private static final String NAME = "Unterbrechung";

    /** @fundstelle HM-UNTBR-01 */
    private static final int MAX_UNTERBRECHUNG_TAGE = 14;

    public Pruefergebnis pruefe(Verordnung verordnung, Behandlungsverlauf verlauf) {
        for (Unterbrechung unterbrechung : verlauf.unterbrechungen()) {
            if (unterbrechung.tage() <= MAX_UNTERBRECHUNG_TAGE) {
                continue;
            }
            if (unterbrechung.istBegruendetBei(verordnung.therapieform())) {
                continue;
            }
            return Pruefergebnis.verletzt(
                    NAME,
                    ("Unterbrechung von %d Tagen zwischen %s und %s, "
                                    + "zulässig sind %d ohne Begründung (%s). "
                                    + "Die Verordnung ist für die Restmenge verfallen.")
                            .formatted(
                                    unterbrechung.tage(),
                                    unterbrechung.von(),
                                    unterbrechung.bis(),
                                    MAX_UNTERBRECHUNG_TAGE,
                                    unterbrechung.kennzeichen().beschreibung()));
        }

        return Pruefergebnis.erfuellt(NAME, begruendungFuerErfuellt(verlauf));
    }

    private String begruendungFuerErfuellt(Behandlungsverlauf verlauf) {
        if (verlauf.unterbrechungen().isEmpty()) {
            return "Noch keine Pause zwischen zwei Behandlungen.";
        }
        long laengste = verlauf.unterbrechungen().stream()
                .mapToLong(Unterbrechung::tage)
                .max()
                .orElse(0L);
        return "Längste Unterbrechung %d Tage, Grenze %d ohne Begründung.".formatted(laengste, MAX_UNTERBRECHUNG_TAGE);
    }
}
