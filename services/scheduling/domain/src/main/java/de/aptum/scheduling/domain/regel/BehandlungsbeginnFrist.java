package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Beginnt die Behandlung rechtzeitig nach der Ausstellung?
 *
 * <p>Die Regel in zwei Sätzen: Eine Verordnung verfällt, wenn die Behandlung
 * nicht innerhalb von 28 Kalendertagen nach dem Ausstellungsdatum beginnt. Ist
 * dringlicher Behandlungsbedarf auf dem Vordruck gekennzeichnet, verkürzt sich
 * die Frist auf 14 Kalendertage.
 *
 * <p>Die Zahlen stehen nicht hier, weil sie plausibel sind, sondern weil sie
 * belegt sind. Die Fundstellen darunter zeigen auf die Zeilen in
 * {@code regeln.md}; {@code scripts/regel-check.mjs} prüft, dass es sie gibt
 * und dass ihr Status BELEGT lautet. Grundlage ist ADR-007.
 *
 * <p><strong>Eine Auslegung, die die Quelle offen lässt:</strong> Der
 * Verordnungstext sagt "innerhalb von 28 Kalendertagen", ohne zu klären, ob
 * der 28. Tag noch dazugehört. Diese Regel legt ihn als zulässig aus, den 29.
 * nicht. Die Annahme ist hier benannt und hat einen eigenen Testfall, damit
 * sie beim nächsten Rechtsstand nicht als Selbstverständlichkeit durchgeht.
 */
public final class BehandlungsbeginnFrist {

    private static final String NAME = "Verordnungsfrist";

    /** @fundstelle HM-FRIST-01 */
    private static final int FRIST_TAGE_REGELFALL = 28;

    /** @fundstelle HM-FRIST-02 */
    private static final int FRIST_TAGE_DRINGLICH = 14;

    /**
     * Prüft, ob der geplante Behandlungsbeginn die Frist einhält.
     *
     * @param verordnung         die Verordnung, deren Frist gilt
     * @param behandlungsbeginn  der geplante erste Behandlungstag
     * @return das Ergebnis mit Begründung im Klartext, nie nur ein Ja oder Nein
     */
    public Pruefergebnis pruefe(Verordnung verordnung, LocalDate behandlungsbeginn) {
        int frist = verordnung.dringlicherBedarf()
                ? FRIST_TAGE_DRINGLICH
                : FRIST_TAGE_REGELFALL;

        long tage = ChronoUnit.DAYS.between(verordnung.ausstellungsdatum(), behandlungsbeginn);

        // Ein Beginn vor der Ausstellung ist keine Fristverletzung, sondern ein
        // unmöglicher Fall. Er bekommt trotzdem eine eigene Begründung, weil
        // "Frist verletzt" die Rezeption in die Irre schicken würde.
        if (tage < 0) {
            return Pruefergebnis.verletzt(NAME,
                    "Der Behandlungsbeginn liegt vor dem Ausstellungsdatum der Verordnung.");
        }

        String zusatz = verordnung.dringlicherBedarf()
                ? " (dringlicher Behandlungsbedarf ist gekennzeichnet)"
                : "";

        if (tage <= frist) {
            return Pruefergebnis.erfuellt(NAME,
                    "Behandlungsbeginn %d Tage nach Ausstellung, zulässig sind %d%s."
                            .formatted(tage, frist, zusatz));
        }

        return Pruefergebnis.verletzt(NAME,
                "Behandlungsbeginn %d Tage nach Ausstellung, zulässig sind %d%s. "
                        .formatted(tage, frist, zusatz)
                        + "Die Verordnung hat ihre Gültigkeit verloren.");
    }
}
