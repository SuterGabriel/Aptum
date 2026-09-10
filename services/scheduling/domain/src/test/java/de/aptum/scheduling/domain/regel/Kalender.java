package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Arbeitszeit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.EnumSet;

/**
 * Testdaten für das Zeitmodell.
 *
 * <p>Ein fester Dienstag im März, damit kein Test am Kalender des Tages hängt
 * und die Sommerzeit nicht unbemerkt hineinspielt — für die gibt es einen
 * eigenen Fall.
 */
final class Kalender {

    /** Dienstag, 3. März 2026. Winterzeit. */
    static final LocalDate DIENSTAG = LocalDate.of(2026, 3, 3);

    static final Therapeut ALPHA = Therapeut.ohneZertifikate("T. Alpha");
    static final Therapeut BETA = Therapeut.ohneZertifikate("T. Beta");

    static final Raum RAUM_1 = Raum.behandlungsraum("Raum 1", 24);
    static final Raum BAD = new Raum("Bewegungsbad", 40, 0, EnumSet.of(Raumanforderung.BEWEGUNGSBAD));

    /** Zehn Minuten Rüstzeit, zwanzig Minuten Nachruhe. */
    static final Praxiseinstellung EINSTELLUNG =
            new Praxiseinstellung(Duration.ofMinutes(10), Duration.ofMinutes(20));

    static final Dienstplan ACHT_BIS_SIEBZEHN =
            Dienstplan.mit(Arbeitszeit.werktags(LocalTime.of(8, 0), LocalTime.of(17, 0)));

    private Kalender() {
    }

    static ZonedDateTime um(int stunde, int minute) {
        return DIENSTAG.atTime(stunde, minute).atZone(Zeitraum.PRAXIS);
    }

    static Zeitraum von(int stunde, int minute, Duration dauer) {
        return Zeitraum.ab(um(stunde, minute), dauer);
    }

    /** Eine halbe Stunde ab dieser Uhrzeit. */
    static Zeitraum halbeStundeAb(int stunde, int minute) {
        return von(stunde, minute, Duration.ofMinutes(30));
    }

    static Termin termin(Therapeut wer, Raum wo, Heilmittel was, int stunde, int minute) {
        return new Termin(wer, wo, was, Zeitraum.ab(um(stunde, minute), was.regeldauer()));
    }
}
