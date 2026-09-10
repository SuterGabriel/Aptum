package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapeut;

/**
 * Darf diese Person diese Leistung erbringen?
 *
 * <p>Die Regel in zwei Sätzen: Bestimmte Heilmittel sind Zertifikatsleistungen
 * und dürfen nur mit personengebundener Abrechnungserlaubnis erbracht werden.
 * Fehlt sie, ist der Termin nicht abrechenbar, und die Behandlung wäre
 * geleistete Arbeit ohne Vergütung.
 *
 * <p>Dies ist die erste Regel, die nicht die Verordnung prüft, sondern die
 * Dimension <em>Therapeut</em>. Sie beantwortet den Teil der Slot-Suche, der im
 * README als „Manuelle Lymphdrainage darf nicht jeder abrechnen" steht.
 *
 * <p><strong>Warum hier keine Stundenzahl auftaucht.</strong> Der Katalog nennt
 * Weiterbildungsumfänge, für die Lymphdrainage aber widersprüchlich — 170
 * Stunden in Anlage 1, 140 Unterrichtseinheiten in der ab Juni 2026 wirksamen
 * Anlage 7 ({@code @fundstelle HM-QUAL-02}, Status UNSICHER). Nach ADR-007 darf
 * daraus keine Konstante werden.
 *
 * <p>Der Verzicht führt zum besseren Modell. Geprüft wird die erteilte
 * Abrechnungserlaubnis ({@code @fundstelle HM-QUAL-06}), nicht die Stundenzahl
 * dahinter — und genau das tut eine Praxis auch. Die unsichere Quelle hat hier
 * nichts offen gelassen, sondern eine falsche Modellierung verhindert.
 *
 * <p><strong>Für die Ergotherapie greift die Regel nie.</strong> Dort gibt es
 * keine Zertifikatspositionen, nur eine allgemeine Fortbildungspflicht
 * ({@code @fundstelle HM-QUAL-07}). Das Ergebnis sagt das ausdrücklich, statt
 * still durchzuwinken.
 */
public final class QualifikationsGrenze {

    private static final String NAME = "Qualifikation";

    public Pruefergebnis pruefe(Heilmittel heilmittel, Therapeut therapeut) {
        if (!heilmittel.brauchtAbrechnungserlaubnis()) {
            return Pruefergebnis.erfuellt(NAME,
                    ("%s ist keine Zertifikatsleistung und darf ohne Zusatzqualifikation "
                            + "erbracht werden.").formatted(heilmittel.bezeichnung()));
        }

        String leistung = heilmittel.zertifikatsleistung().bezeichnung();

        if (therapeut.darfAbrechnen(heilmittel.zertifikatsleistung())) {
            return Pruefergebnis.erfuellt(NAME,
                    "Abrechnungserlaubnis für %s liegt bei %s vor."
                            .formatted(leistung, therapeut.kuerzel()));
        }

        return Pruefergebnis.verletzt(NAME,
                ("%s ist eine Zertifikatsleistung; %s hat dafür keine Abrechnungserlaubnis. "
                        + "Die Behandlung wäre nicht abrechenbar.")
                        .formatted(heilmittel.bezeichnung(), therapeut.kuerzel()));
    }
}
