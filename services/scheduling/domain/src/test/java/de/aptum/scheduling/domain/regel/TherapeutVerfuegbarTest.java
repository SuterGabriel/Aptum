package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Abwesenheit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Zeitraum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.Duration;
import java.util.List;

import static de.aptum.scheduling.domain.regel.Kalender.ACHT_BIS_SIEBZEHN;
import static de.aptum.scheduling.domain.regel.Kalender.ALPHA;
import static de.aptum.scheduling.domain.regel.Kalender.BETA;
import static de.aptum.scheduling.domain.regel.Kalender.DIENSTAG;
import static de.aptum.scheduling.domain.regel.Kalender.EINSTELLUNG;
import static de.aptum.scheduling.domain.regel.Kalender.RAUM_1;
import static de.aptum.scheduling.domain.regel.Kalender.halbeStundeAb;
import static de.aptum.scheduling.domain.regel.Kalender.termin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Arbeitszeit minus Abwesenheiten minus gebuchte Termine. */
class TherapeutVerfuegbarTest {

    private final TherapeutVerfuegbar regel = new TherapeutVerfuegbar();

    private Pruefergebnis pruefe(Dienstplan plan, List<Termin> bestehende, Zeitraum behandlung) {
        return regel.pruefe(ALPHA, plan, bestehende, behandlung, EINSTELLUNG);
    }

    @DisplayName("Die Arbeitszeit gilt einschliesslich der Ruestzeit auf beiden Seiten")
    @ParameterizedTest(name = "{2}")
    @CsvSource({
            "8,  10, ERFUELLT, Behandlung ab 08:10, Ruestzeit ab 08:00 - genau auf den Dienstbeginn",
            "8,  0,  VERLETZT, Behandlung ab 08:00, Ruestzeit begaenne vor dem Dienst",
            "16, 20, ERFUELLT, Behandlung bis 16:50, Ruestzeit bis 17:00 - genau auf das Dienstende",
            "16, 30, VERLETZT, Behandlung bis 17:00, Ruestzeit reichte ueber das Dienstende",
    })
    void arbeitszeitMitRuestzeit(int stunde, int minute, Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(erwartet,
                pruefe(ACHT_BIS_SIEBZEHN, List.of(), halbeStundeAb(stunde, minute)).ausgang(),
                situation);
    }

    @Test
    @DisplayName("Ein freier Tag ist keine Arbeitszeit")
    void freierTag() {
        Zeitraum samstag = Zeitraum.ab(
                DIENSTAG.plusDays(4).atTime(10, 0).atZone(Zeitraum.PRAXIS), Duration.ofMinutes(30));
        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                pruefe(ACHT_BIS_SIEBZEHN, List.of(), samstag).ausgang());
    }

    @Test
    @DisplayName("Urlaub schlaegt die Arbeitszeit")
    void abwesenheit() {
        Dienstplan imUrlaub = ACHT_BIS_SIEBZEHN.abwesend(
                new Abwesenheit(DIENSTAG.minusDays(1), DIENSTAG.plusDays(1), "Urlaub"));

        Pruefergebnis ergebnis = pruefe(imUrlaub, List.of(), halbeStundeAb(10, 0));
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("Urlaub"), "die Begruendung nennt den Grund");
    }

    @Test
    @DisplayName("Zwei Termine derselben Person brauchen die Ruestzeit dazwischen")
    void ruestzeitZwischenTerminen() {
        // Bestehend: KG von 10:00 bis 10:25, mit Rüstzeit bis 10:35.
        List<Termin> bestehend = List.of(termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0));

        // Neu ab 10:35: Rüstzeit ab 10:25 - kollidiert mit der Nachbereitung.
        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                pruefe(ACHT_BIS_SIEBZEHN, bestehend, halbeStundeAb(10, 35)).ausgang(),
                "zehn Minuten Abstand reichen nicht, es braucht zweimal Ruestzeit");

        // Neu ab 10:45: Rüstzeit ab 10:35 - schließt genau an.
        assertTrue(pruefe(ACHT_BIS_SIEBZEHN, bestehend, halbeStundeAb(10, 45)).istErfuellt(),
                "zwanzig Minuten Abstand: Nachbereitung und Vorbereitung passen hintereinander");
    }

    @Test
    @DisplayName("Der Termin einer anderen Person blockiert nicht")
    void anderePerson() {
        List<Termin> betasTermin = List.of(termin(BETA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0));
        assertTrue(pruefe(ACHT_BIS_SIEBZEHN, betasTermin, halbeStundeAb(10, 0)).istErfuellt(),
                "ob der Raum frei ist, ist eine andere Frage - hier geht es um die Person");
    }

    @Test
    @DisplayName("Ein Termin ueber Mitternacht ist keine Arbeitszeit")
    void ueberMitternacht() {
        Zeitraum spaet = Zeitraum.ab(
                DIENSTAG.atTime(23, 50).atZone(Zeitraum.PRAXIS), Duration.ofMinutes(30));
        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                pruefe(ACHT_BIS_SIEBZEHN, List.of(), spaet).ausgang());
    }
}
