package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import de.aptum.scheduling.domain.model.Verordnung;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Testdaten für die Unterbrechungsregeln.
 *
 * <p>Ein Verlauf wird über die Abstände zwischen den Behandlungen beschrieben,
 * nicht über Datumsangaben. Ein Test, der von "25, 25, 25" spricht, ist beim
 * Lesen sofort verständlich; einer, der fünf Kalenderdaten aufzählt, zwingt
 * zum Nachrechnen.
 *
 * <p>Alle Daten sind erkennbar synthetisch. Patientennamen kommen hier nicht
 * vor, weil die Regeln sie nicht brauchen.
 */
final class Verlauf {

    /** Ein fester Startpunkt, damit Tests nicht am Kalender des Tages hängen. */
    static final LocalDate ERSTER_TAG = LocalDate.of(2026, 1, 5);

    private Verlauf() {}

    /** Behandlungen im Abstand der angegebenen Tage, ohne Kennzeichen. */
    static Behandlungsverlauf mitAbstaenden(long... abstaende) {
        return mitAbstaenden(Unterbrechungskennzeichen.KEINES, abstaende);
    }

    /** Behandlungen im Abstand der angegebenen Tage, jede Pause so begründet. */
    static Behandlungsverlauf mitAbstaenden(Unterbrechungskennzeichen kennzeichen, long... abstaende) {
        List<Behandlungstermin> termine = new ArrayList<>();
        termine.add(Behandlungstermin.an(ERSTER_TAG));
        LocalDate laufend = ERSTER_TAG;
        for (long abstand : abstaende) {
            laufend = laufend.plusDays(abstand);
            termine.add(new Behandlungstermin(laufend, kennzeichen));
        }
        return Behandlungsverlauf.aus(termine);
    }

    /** Die Katalogfrequenz der meisten Diagnosegruppen, wenn der Test sie nicht braucht. */
    static final Frequenz REGELFREQUENZ = Frequenz.spanne(1, 3);

    static Verordnung verordnung(Therapieform therapieform, int einheiten) {
        return verordnung(therapieform, einheiten, REGELFREQUENZ);
    }

    static Verordnung verordnung(Therapieform therapieform, int einheiten, Frequenz frequenz) {
        return verordnung(vertreterFuer(therapieform), einheiten, frequenz);
    }

    static Verordnung verordnung(Diagnosegruppe gruppe, int einheiten, Frequenz frequenz) {
        return new Verordnung(ERSTER_TAG.minusDays(3), false, gruppe, einheiten, frequenz);
    }

    /**
     * Eine Diagnosegruppe je Therapieform, für Tests, die nur die Form
     * brauchen. Beide lassen zehn Einheiten je Verordnung zu, damit die
     * Mengenregel den anderen Tests nicht dazwischenfunkt.
     */
    static Diagnosegruppe vertreterFuer(Therapieform therapieform) {
        return therapieform.istErgotherapie() ? Diagnosegruppe.SB1 : Diagnosegruppe.ZN;
    }
}
