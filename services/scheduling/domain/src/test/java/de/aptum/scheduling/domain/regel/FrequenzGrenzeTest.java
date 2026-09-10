package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Die Frequenzregel. Sie warnt, sie blockiert nie. */
class FrequenzGrenzeTest {

    /** Ein Montag, damit die Kalenderwoche im Test sichtbar bleibt. */
    private static final LocalDate MONTAG = LocalDate.of(2026, 3, 2);

    private static final int EINHEITEN = 10;

    private final FrequenzGrenze regel = new FrequenzGrenze();

    private Behandlungsverlauf behandlungenAb(LocalDate erster, int anzahl) {
        List<Behandlungstermin> termine = new ArrayList<>();
        for (int i = 0; i < anzahl; i++) {
            termine.add(Behandlungstermin.an(erster.plusDays(i)));
        }
        return Behandlungsverlauf.aus(termine);
    }

    private Pruefergebnis pruefe(Frequenz frequenz, Behandlungsverlauf verlauf, LocalDate geplant) {
        return regel.pruefe(
                Verlauf.verordnung(Therapieform.PHYSIOTHERAPIE, EINHEITEN, frequenz),
                verlauf,
                geplant);
    }

    @DisplayName("Bis zur oberen Grenze der Verordnung ist der Termin unauffaellig")
    @ParameterizedTest(name = "{3}")
    @CsvSource({
            "2, 0, ERFUELLT, Erster Termin bei zweimal woechentlich",
            "2, 1, ERFUELLT, Zweiter Termin bei zweimal woechentlich",
            "2, 2, WARNUNG,  Dritter Termin bei zweimal woechentlich",
            "3, 2, ERFUELLT, Dritter Termin bei bis zu dreimal woechentlich",
            "3, 3, WARNUNG,  Vierter Termin bei bis zu dreimal woechentlich",
    })
    void obereGrenze(int maxProWoche, int schonInDerWoche,
                     Pruefergebnis.Ausgang erwartet, String situation) {
        Pruefergebnis ergebnis = pruefe(
                Frequenz.spanne(1, maxProWoche),
                behandlungenAb(MONTAG, schonInDerWoche),
                MONTAG.plusDays(schonInDerWoche));

        assertEquals(erwartet, ergebnis.ausgang(), situation);
    }

    @Test
    @DisplayName("Die Woche endet am Sonntag: der Montag darauf faengt neu an")
    void wochengrenze() {
        LocalDate sonntag = MONTAG.plusDays(6);
        LocalDate naechsterMontag = MONTAG.plusDays(7);
        assertEquals(DayOfWeek.SUNDAY, sonntag.getDayOfWeek());
        assertEquals(DayOfWeek.MONDAY, naechsterMontag.getDayOfWeek());

        // Drei Behandlungen von Montag bis Mittwoch, Verordnung zweimal
        // wöchentlich. In derselben Woche wäre der nächste Termin die
        // vierte Behandlung und damit auffällig.
        Behandlungsverlauf verlauf = behandlungenAb(MONTAG, 3);

        assertEquals(Pruefergebnis.Ausgang.WARNUNG,
                pruefe(Frequenz.genau(2), verlauf, sonntag).ausgang(),
                "noch dieselbe Kalenderwoche");
        assertEquals(Pruefergebnis.Ausgang.ERFUELLT,
                pruefe(Frequenz.genau(2), verlauf, naechsterMontag).ausgang(),
                "die neue Woche zaehlt bei null wieder los");
    }

    @Test
    @DisplayName("Termine aus anderen Wochen zaehlen nicht mit")
    void andereWochenBleibenAussen() {
        Behandlungsverlauf vorwoche = behandlungenAb(MONTAG.minusDays(7), 5);
        assertTrue(pruefe(Frequenz.genau(1), vorwoche, MONTAG).istErfuellt(),
                "fuenf Behandlungen in der Vorwoche beruehren diese Woche nicht");
    }

    @Test
    @DisplayName("Eine lange Luecke erzeugt keine Frequenzmeldung")
    void unterbrechungIstKeineFrequenzabweichung() {
        // Der Vertragstext hält ausdrücklich fest, dass eine Unterbrechung
        // keine Frequenzabweichung ist. Diese Regel darf deshalb bei einer
        // Lücke im Verlauf gar nichts melden - HM-UNTBR-07.
        Behandlungsverlauf langePause = Verlauf.mitAbstaenden(90);

        assertTrue(pruefe(Frequenz.genau(3), langePause, Verlauf.ERSTER_TAG.plusDays(180))
                .istErfuellt(), "zu wenig Behandlung ist hier keine Meldung wert");
    }

    @Test
    @DisplayName("Die Regel blockiert nie, sie warnt hoechstens")
    void niemalsVerletzt() {
        // Sechs Behandlungen in einer Woche bei einer Verordnung über eine.
        // Selbst dieser Fall darf die Buchung nicht verhindern: Über eine
        // Abweichung entscheidet ein Mensch nach Rücksprache, nicht das System.
        Behandlungsverlauf viel = behandlungenAb(MONTAG, 5);
        Pruefergebnis ergebnis = pruefe(Frequenz.genau(1), viel, MONTAG.plusDays(5));

        assertNotEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertEquals(Pruefergebnis.Ausgang.WARNUNG, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("Absprache"),
                "die Begruendung nennt den Weg, auf dem die Abweichung zulaessig wird");
    }

    @Test
    @DisplayName("Die Begruendung nennt die Angabe vom Vordruck")
    void begruendungNenntDieVerordneteAngabe() {
        Pruefergebnis ergebnis = pruefe(
                Frequenz.spanne(1, 3), Behandlungsverlauf.leer(), MONTAG);
        assertTrue(ergebnis.begruendung().contains("1-3x wöchentlich"),
                "die Rezeption soll die Angabe wiedererkennen, die auf dem Rezept steht");
    }
}
