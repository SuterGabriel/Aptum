package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;
import java.time.LocalDate;

/**
 * Ist die Verordnung insgesamt noch gültig, gerechnet ab dem ersten
 * Behandlungstag?
 *
 * <p>Die Regel in zwei Sätzen: In der Physiotherapie verfällt eine Verordnung
 * über bis zu sechs Behandlungseinheiten drei Monate nach dem ersten
 * Behandlungstag, eine über mehr als sechs Einheiten nach sechs Monaten.
 * Bezugspunkt ist der erste Behandlungstag, nicht das Ausstellungsdatum.
 *
 * <p><strong>Die andere Hälfte der Asymmetrie.</strong> Die Ergotherapie
 * begrenzt nicht die Laufzeit, sondern die Summe der Unterbrechungen — siehe
 * {@link UnterbrechungsSumme}. Zu einer absoluten Gültigkeitsdauer für die
 * Ergotherapie wurde in den Verträgen <em>keine</em> Regel gefunden. Der
 * Regelkatalog führt sie unter {@code HM-UNTBR-06} als UNSICHER, weil ein
 * Nichtfund kein Beweis der Abwesenheit ist.
 *
 * <p>Nach ADR-007 darf aus einer unsicheren Zeile keine Konstante werden.
 * Deshalb prüft diese Regel für die Ergotherapie nichts und sagt das auch. Der
 * Regel-Check würde eine erfundene Frist an dieser Stelle zurückweisen.
 *
 * <p><strong>Eine Auslegung, die die Quelle offen lässt:</strong> Ob der
 * Stichtag selbst noch dazugehört, sagt der Vertragstext nicht. Diese Regel
 * legt ihn als zulässig aus — dieselbe Auslegung wie bei
 * {@link BehandlungsbeginnFrist}, damit die beiden Fristen sich nicht
 * unterschiedlich verhalten.
 */
public final class VerordnungsGueltigkeit {

    private static final String NAME = "Verordnungsgültigkeit";

    /** @fundstelle HM-UNTBR-05 */
    private static final int EINHEITEN_GRENZE = 6;

    /** @fundstelle HM-UNTBR-05 */
    private static final int MONATE_BIS_GRENZE = 3;

    /** @fundstelle HM-UNTBR-05 */
    private static final int MONATE_UEBER_GRENZE = 6;

    public Pruefergebnis pruefe(Verordnung verordnung, Behandlungsverlauf verlauf, LocalDate stichtag) {
        if (!verordnung.therapieform().istPhysiotherapie()) {
            return Pruefergebnis.erfuellt(
                    NAME,
                    "Für die Ergotherapie ist keine absolute Gültigkeitsdauer belegt. "
                            + "Dort begrenzt stattdessen die Summe der Unterbrechungen.");
        }

        if (verlauf.istLeer()) {
            return Pruefergebnis.erfuellt(NAME, "Die Laufzeit beginnt mit der ersten Behandlung; es gab noch keine.");
        }

        LocalDate ersterTag = verlauf.ersterBehandlungstag().orElseThrow();
        int monate = verordnung.verordneteEinheiten() <= EINHEITEN_GRENZE ? MONATE_BIS_GRENZE : MONATE_UEBER_GRENZE;
        LocalDate verfallstag = ersterTag.plusMonths(monate);

        String lage = "%d Einheiten verordnet, Laufzeit %d Monate ab %s, also bis %s"
                .formatted(verordnung.verordneteEinheiten(), monate, ersterTag, verfallstag);

        if (stichtag.isAfter(verfallstag)) {
            return Pruefergebnis.verletzt(NAME, lage + ". Am " + stichtag + " ist die Verordnung verfallen.");
        }
        return Pruefergebnis.erfuellt(NAME, lage + ".");
    }
}
