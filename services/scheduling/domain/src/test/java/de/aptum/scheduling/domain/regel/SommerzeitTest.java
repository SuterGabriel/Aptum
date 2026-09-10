package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Zeitraum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Der Grenzfall aus dem Skill heilmittel-domain: Eine Serie „jeden Dienstag
 * 14:00" bleibt bei 14:00 Ortszeit, auch über die Umstellung hinweg. Wer in
 * UTC rechnet und stur 168 Stunden addiert, verschiebt den Termin im März um
 * eine Stunde.
 *
 * <p>Serientermine gibt es noch nicht. Der Test hält fest, dass das Zeitmodell
 * sie tragen kann, wenn sie kommen — und dass niemand die Zone unterwegs
 * verliert.
 */
class SommerzeitTest {

    /** Dienstag vor und Dienstag nach der Umstellung am 29. März 2026. */
    private static final LocalDate DAVOR = LocalDate.of(2026, 3, 24);
    private static final LocalDate DANACH = LocalDate.of(2026, 3, 31);

    @Test
    @DisplayName("Eine Woche spaeter ist wieder 14:00 Ortszeit, nicht 15:00")
    void wocheSpaeterBleibtOrtszeit() {
        ZonedDateTime davor = DAVOR.atTime(14, 0).atZone(Zeitraum.PRAXIS);
        ZonedDateTime danach = davor.plusWeeks(1);

        assertEquals(LocalTime.of(14, 0), danach.toLocalTime(), "plusWeeks rechnet in Ortszeit");
        assertEquals(DANACH, danach.toLocalDate());
        assertEquals(Duration.ofHours(167), Duration.between(davor, danach),
                "die Woche der Umstellung hat 167 Stunden - und genau das ist richtig");
    }

    @Test
    @DisplayName("Wer 168 Stunden addiert, landet um 15:00")
    void stundenAddierenIstDerFehler() {
        ZonedDateTime davor = DAVOR.atTime(14, 0).atZone(Zeitraum.PRAXIS);
        ZonedDateTime falsch = davor.plusHours(168);

        assertEquals(LocalTime.of(15, 0), falsch.toLocalTime(),
                "das ist der Fehler, den der Skill beschreibt - hier absichtlich festgehalten");
    }

    @Test
    @DisplayName("Ein Termin ueber die Umstellung dauert so lange, wie er dauert")
    void terminUeberDieUmstellung() {
        // 29. März 2026, 01:30 bis 03:30 Ortszeit - dazwischen fehlt die Stunde
        // von 02:00 bis 03:00. Ein Nachtdienst, den es in der Praxis nicht gibt;
        // hier nur, damit die Dauer stimmt, falls es ihn je gibt.
        ZonedDateTime von = LocalDate.of(2026, 3, 29).atTime(1, 30).atZone(Zeitraum.PRAXIS);
        Zeitraum termin = new Zeitraum(von, von.plusHours(1));

        assertEquals(LocalTime.of(3, 30), termin.bis().toLocalTime(), "die Uhr springt");
        assertEquals(Duration.ofHours(1), termin.dauer(), "die Dauer nicht");
    }
}
