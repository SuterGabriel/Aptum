package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Zeitraum;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;

import static de.aptum.scheduling.domain.regel.Kalender.ACHT_BIS_SIEBZEHN;
import static de.aptum.scheduling.domain.regel.Kalender.ALPHA;
import static de.aptum.scheduling.domain.regel.Kalender.BAD;
import static de.aptum.scheduling.domain.regel.Kalender.EINSTELLUNG;
import static de.aptum.scheduling.domain.regel.Kalender.RAUM_1;
import static de.aptum.scheduling.domain.regel.Kalender.termin;
import static de.aptum.scheduling.domain.regel.Kalender.um;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Derselbe Termin, zwei Ressourcen, verschiedene Belegung.
 *
 * <p>Nach dem Bewegungsbad ruht der Patient zwanzig Minuten im Bad. Die
 * Therapeutin ist in dieser Zeit frei und behandelt nebenan weiter. Wer beide
 * Ressourcen gleich behandelt, verschenkt entweder Behandlungszeit oder legt
 * zwei Patienten ins selbe Bad.
 *
 * <p>Das ist der Satz aus dem README: Nachruhezeiten, die den Raum blockieren,
 * aber nicht die Therapeutin.
 */
class NachruheAsymmetrieTest {

    private final TherapeutVerfuegbar therapeutRegel = new TherapeutVerfuegbar();
    private final RaumFrei raumRegel = new RaumFrei();

    /** Bewegungsbad 10:00 bis 10:30. Rüstzeit bis 10:40, Nachruhe bis 11:00. */
    private final List<Termin> bad = List.of(termin(ALPHA, BAD, Heilmittel.KG_BEWEGUNGSBAD, 10, 0));

    @Test
    @DisplayName("Waehrend der Nachruhe: Therapeutin frei, Bad belegt")
    void waehrendDerNachruhe() {
        // Neue Behandlung 10:50 bis 11:20, Rüstzeit ab 10:40 - direkt nach
        // der Nachbereitung des Bads, mitten in der Nachruhe des Patienten.
        Zeitraum naechste = Zeitraum.ab(um(10, 50), Duration.ofMinutes(30));

        Pruefergebnis therapeutin = therapeutRegel.pruefe(ALPHA, ACHT_BIS_SIEBZEHN, bad, naechste, EINSTELLUNG);
        Pruefergebnis nebenan = raumRegel.pruefe(RAUM_1, Heilmittel.KG_EINZEL, bad, naechste, EINSTELLUNG);
        Pruefergebnis imBad = raumRegel.pruefe(BAD, Heilmittel.KG_BEWEGUNGSBAD, bad, naechste, EINSTELLUNG);

        assertTrue(therapeutin.istErfuellt(), "die Therapeutin ist nach der Ruestzeit frei");
        assertTrue(nebenan.istErfuellt(), "Raum 1 war nie belegt");
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, imBad.ausgang(),
                "das Bad ist bis 11:00 belegt, weil noch jemand darin ruht");
    }

    @Test
    @DisplayName("Nach der Nachruhe ist auch das Bad wieder frei")
    void nachDerNachruhe() {
        // 11:10 bis 11:40, Rüstzeit ab 11:00 - genau wenn die Nachruhe endet.
        Zeitraum spaeter = Zeitraum.ab(um(11, 10), Duration.ofMinutes(30));

        assertTrue(raumRegel.pruefe(BAD, Heilmittel.KG_BEWEGUNGSBAD, bad, spaeter, EINSTELLUNG).istErfuellt());
    }

    @Test
    @DisplayName("Der Termin weiss selbst, was er je Ressource belegt")
    void terminKenntBeideBelegungen() {
        Termin t = bad.get(0);
        assertEquals(Duration.ofMinutes(50), t.belegtTherapeutin(EINSTELLUNG).dauer(),
                "30 Behandlung + 2 x 10 Ruestzeit");
        assertEquals(Duration.ofMinutes(70), t.belegtRaum(EINSTELLUNG).dauer(),
                "dasselbe plus 20 Nachruhe");

        Termin ohneBad = termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0);
        assertEquals(ohneBad.belegtTherapeutin(EINSTELLUNG), ohneBad.belegtRaum(EINSTELLUNG),
                "ohne Nachruhe belegt ein Termin beides gleich lang");
    }
}
