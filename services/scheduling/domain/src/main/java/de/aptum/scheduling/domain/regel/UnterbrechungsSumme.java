package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Unterbrechung;
import de.aptum.scheduling.domain.model.Verordnung;

/**
 * Summieren sich die begründeten Unterbrechungen über die Grenze der
 * Ergotherapie?
 *
 * <p>Die Regel in zwei Sätzen: In der Ergotherapie dürfen sich begründete
 * Unterbrechungen auf höchstens 70 Kalendertage summieren. Mitgezählt werden
 * dabei nur Pausen, die für sich genommen länger als 14 Tage sind.
 *
 * <p><strong>Die Hälfte der Asymmetrie.</strong> Für die Physiotherapie
 * enthält der Vertragstext keine Summenobergrenze. Das ist kein Versehen und
 * keine Lücke in der Recherche, sondern ein belegter Nichtfund
 * ({@code HM-UNTBR-04}). Diese Regel meldet für die Physiotherapie deshalb
 * ausdrücklich, dass sie nicht greift, statt still nichts zu tun — sonst sieht
 * ein Nichtfund im Ergebnis genauso aus wie eine vergessene Prüfung.
 *
 * <p>Die Gegenseite ist {@link VerordnungsGueltigkeit}: Was die Ergotherapie
 * über die Summe der Pausen begrenzt, begrenzt die Physiotherapie über die
 * Gesamtlaufzeit.
 */
public final class UnterbrechungsSumme {

    private static final String NAME = "Unterbrechungssumme";

    /** @fundstelle HM-UNTBR-03 */
    private static final int MAX_SUMME_TAGE = 70;

    /** @fundstelle HM-UNTBR-03 */
    private static final int MINDESTLAENGE_ZUM_ZAEHLEN_TAGE = 14;

    public Pruefergebnis pruefe(Verordnung verordnung, Behandlungsverlauf verlauf) {
        if (!verordnung.therapieform().istErgotherapie()) {
            return Pruefergebnis.erfuellt(
                    NAME,
                    "Für die Physiotherapie ist keine Summenobergrenze für "
                            + "Unterbrechungen vertraglich geregelt. Begrenzt wird dort "
                            + "stattdessen die Gesamtlaufzeit der Verordnung.");
        }

        long summe = 0;
        int gezaehlt = 0;
        for (Unterbrechung unterbrechung : verlauf.unterbrechungen()) {
            if (unterbrechung.tage() <= MINDESTLAENGE_ZUM_ZAEHLEN_TAGE) {
                continue;
            }
            summe += unterbrechung.tage();
            gezaehlt++;
        }

        String zaehlweise = "%d Unterbrechungen über %d Tage zählen mit, Summe %d von %d"
                .formatted(gezaehlt, MINDESTLAENGE_ZUM_ZAEHLEN_TAGE, summe, MAX_SUMME_TAGE);

        if (summe > MAX_SUMME_TAGE) {
            return Pruefergebnis.verletzt(NAME, zaehlweise + " Kalendertagen. Die Verordnung ist verfallen.");
        }
        return Pruefergebnis.erfuellt(NAME, zaehlweise + " Kalendertagen.");
    }
}
