package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static de.aptum.scheduling.domain.regel.Kalender.ALPHA;
import static de.aptum.scheduling.domain.regel.Kalender.BETA;
import static de.aptum.scheduling.domain.regel.Kalender.EINSTELLUNG;
import static de.aptum.scheduling.domain.regel.Kalender.RAUM_1;
import static de.aptum.scheduling.domain.regel.Kalender.halbeStundeAb;
import static de.aptum.scheduling.domain.regel.Kalender.termin;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Ein Raum ist belegt, solange darin behandelt, vorbereitet oder geruht wird. */
class RaumFreiTest {

    private final RaumFrei regel = new RaumFrei();

    @Test
    @DisplayName("Zwei Termine im selben Raum brauchen die Ruestzeit dazwischen")
    void ruestzeitImRaum() {
        List<Termin> bestehend = List.of(termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0));

        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(RAUM_1, Heilmittel.KG_EINZEL, bestehend, halbeStundeAb(10, 35), EINSTELLUNG).ausgang());
        assertTrue(regel.pruefe(RAUM_1, Heilmittel.KG_EINZEL, bestehend, halbeStundeAb(10, 45), EINSTELLUNG).istErfuellt());
    }

    @Test
    @DisplayName("Ein anderer Raum ist ein anderer Raum")
    void andererRaum() {
        Raum raum2 = Raum.behandlungsraum("Raum 2", 24);
        List<Termin> bestehend = List.of(termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0));

        assertTrue(regel.pruefe(raum2, Heilmittel.KG_EINZEL, bestehend, halbeStundeAb(10, 0), EINSTELLUNG).istErfuellt());
    }

    @Test
    @DisplayName("Wer im Raum behandelt, spielt fuer die Belegung keine Rolle")
    void personIstEgal() {
        // Beta hat den Raum. Dass Alpha frei wäre, hilft nicht.
        List<Termin> bestehend = List.of(termin(BETA, RAUM_1, Heilmittel.KG_EINZEL, 10, 0));

        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(RAUM_1, Heilmittel.KG_EINZEL, bestehend, halbeStundeAb(10, 0), EINSTELLUNG).ausgang());
    }

    @Test
    @DisplayName("Die Begruendung nennt die Nachruhe, wenn sie der Grund ist")
    void begruendungNenntNachruhe() {
        List<Termin> bad = List.of(termin(ALPHA, Kalender.BAD, Heilmittel.KG_BEWEGUNGSBAD, 10, 0));
        // Bewegungsbad 10:00 bis 10:30, Rüstzeit bis 10:40, Nachruhe bis 11:00.
        Pruefergebnis ergebnis = regel.pruefe(
                Kalender.BAD, Heilmittel.KG_BEWEGUNGSBAD, bad, halbeStundeAb(10, 55), EINSTELLUNG);

        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("Nachruhe"),
                "die Rezeption soll sehen, dass der Raum wegen der Ruhephase belegt ist");
    }
}
