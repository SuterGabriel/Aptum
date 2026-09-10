package de.aptum.scheduling.domain.kalender;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Abwesenheit;
import de.aptum.scheduling.domain.model.Arbeitszeit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Was das Gitter zeigt, an einer Person und einer Woche durchgespielt.
 *
 * <p>Jeder Test bucht oder plant genau eine Sache und schaut, welche Blöcke
 * daraus werden — und dass Rüstzeit und Nachruhe dieselben Grenzen haben,
 * die {@link Termin} der Suche nennt.
 */
class WochenansichtTest {

    /** Montag, 2. März 2026. */
    private static final LocalDate MONTAG = LocalDate.of(2026, 3, 2);

    private static final Therapeut ALPHA = Therapeut.ohneZertifikate("T. Alpha");
    private static final Raum RAUM_1 = Raum.behandlungsraum("Raum 1", 24);
    private static final Praxiseinstellung EINSTELLUNG =
            new Praxiseinstellung(Duration.ofMinutes(5), Duration.ofMinutes(20));
    private static final Dienstplan VOLLZEIT =
            Dienstplan.mit(Arbeitszeit.werktags(LocalTime.of(8, 0), LocalTime.of(17, 0)));
    private static final LocalTime SIEBEN = LocalTime.of(7, 0);
    private static final LocalTime NEUNZEHN = LocalTime.of(19, 0);

    private static ZonedDateTime um(LocalDate tag, int stunde, int minute) {
        return tag.atTime(stunde, minute).atZone(Zeitraum.PRAXIS);
    }

    private static List<Belegung> alpha(Dienstplan dienstplan, Termin... termine) {
        List<Wochenansicht.Spalte> spalten = Wochenansicht.berechne(new Wochenansicht.Anfrage(
                MONTAG, Map.of(ALPHA, dienstplan), List.of(termine), EINSTELLUNG, SIEBEN, NEUNZEHN));
        assertEquals(1, spalten.size());
        return spalten.get(0).belegungen();
    }

    private static List<Belegung> am(List<Belegung> alle, LocalDate tag, Belegungsart art) {
        return alle.stream()
                .filter(b -> b.art() == art)
                .filter(b -> b.zeitraum().von().toLocalDate().equals(tag))
                .toList();
    }

    @Test
    @DisplayName("Vollzeit ohne Termine: je Werktag zwei Ränder, Samstag kein Dienst")
    void leereWoche() {
        List<Belegung> alle = alpha(VOLLZEIT);

        List<Belegung> montag = am(alle, MONTAG, Belegungsart.GESPERRT);
        assertEquals(2, montag.size());
        assertEquals(um(MONTAG, 7, 0), montag.get(0).zeitraum().von());
        assertEquals(um(MONTAG, 8, 0), montag.get(0).zeitraum().bis());
        assertEquals(um(MONTAG, 17, 0), montag.get(1).zeitraum().von());
        assertEquals(um(MONTAG, 19, 0), montag.get(1).zeitraum().bis());
        assertEquals(Wochenansicht.AUSSERHALB, montag.get(0).text());

        LocalDate samstag = MONTAG.with(DayOfWeek.SATURDAY);
        List<Belegung> sa = am(alle, samstag, Belegungsart.GESPERRT);
        assertEquals(1, sa.size());
        assertEquals(Wochenansicht.KEIN_DIENST, sa.get(0).text());
        assertEquals(um(samstag, 7, 0), sa.get(0).zeitraum().von());
        assertEquals(um(samstag, 19, 0), sa.get(0).zeitraum().bis());

        assertTrue(alle.stream().allMatch(b -> b.art() == Belegungsart.GESPERRT));
    }

    @Test
    @DisplayName("Ein Termin wird zu Rüstzeit, Behandlung, Rüstzeit - mit den Grenzen aus Termin")
    void terminMitRuestzeit() {
        Termin termin =
                new Termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, new Zeitraum(um(MONTAG, 9, 0), um(MONTAG, 9, 20)));
        List<Belegung> alle = alpha(VOLLZEIT, termin);

        List<Belegung> ruest = am(alle, MONTAG, Belegungsart.RUESTZEIT);
        List<Belegung> belegt = am(alle, MONTAG, Belegungsart.BELEGT);
        assertEquals(2, ruest.size());
        assertEquals(1, belegt.size());

        Zeitraum therapeutin = termin.belegtTherapeutin(EINSTELLUNG);
        assertEquals(therapeutin.von(), ruest.get(0).zeitraum().von());
        assertEquals(um(MONTAG, 9, 0), ruest.get(0).zeitraum().bis());
        assertEquals(um(MONTAG, 9, 20), ruest.get(1).zeitraum().von());
        assertEquals(therapeutin.bis(), ruest.get(1).zeitraum().bis());

        assertEquals(Heilmittel.KG_EINZEL.bezeichnung(), belegt.get(0).text());
        assertEquals(Optional.of("Raum 1"), belegt.get(0).raum());
        assertTrue(am(alle, MONTAG, Belegungsart.NACHRUHE).isEmpty(), "Krankengymnastik hat keine Nachruhe");
    }

    @Test
    @DisplayName("Bewegungsbad: die Nachruhe folgt auf die Rüstzeit und nennt den Raum")
    void nachruheNenntDenRaum() {
        Raum bad = new Raum("Bad", 60, 0, EnumSet.of(Raumanforderung.BEWEGUNGSBAD));
        Termin termin =
                new Termin(ALPHA, bad, Heilmittel.KG_BEWEGUNGSBAD, new Zeitraum(um(MONTAG, 10, 0), um(MONTAG, 10, 30)));
        List<Belegung> alle = alpha(VOLLZEIT, termin);

        List<Belegung> nachruhe = am(alle, MONTAG, Belegungsart.NACHRUHE);
        assertEquals(1, nachruhe.size());
        assertEquals(
                termin.belegtTherapeutin(EINSTELLUNG).bis(),
                nachruhe.get(0).zeitraum().von());
        assertEquals(
                termin.belegtRaum(EINSTELLUNG).bis(), nachruhe.get(0).zeitraum().bis());
        assertEquals(Optional.of("Bad"), nachruhe.get(0).raum());
    }

    @Test
    @DisplayName("Abwesenheit: ein Block über den ganzen Tag mit dem Grund, keine Ränder")
    void abwesenheit() {
        LocalDate mittwoch = MONTAG.with(DayOfWeek.WEDNESDAY);
        Dienstplan mitUrlaub = VOLLZEIT.abwesend(Abwesenheit.am(mittwoch, "Fortbildung"));
        List<Belegung> alle = alpha(mitUrlaub);

        List<Belegung> mi = alle.stream()
                .filter(b -> b.zeitraum().von().toLocalDate().equals(mittwoch))
                .toList();
        assertEquals(1, mi.size());
        assertEquals(Belegungsart.ABWESENHEIT, mi.get(0).art());
        assertEquals("Fortbildung", mi.get(0).text());
        assertEquals(um(mittwoch, 7, 0), mi.get(0).zeitraum().von());
        assertEquals(um(mittwoch, 19, 0), mi.get(0).zeitraum().bis());
    }

    @Test
    @DisplayName("Belegungen sind nach Beginn sortiert, über Tage und Arten hinweg")
    void sortiert() {
        Termin spaet =
                new Termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, new Zeitraum(um(MONTAG, 15, 0), um(MONTAG, 15, 20)));
        Termin frueh =
                new Termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, new Zeitraum(um(MONTAG, 8, 30), um(MONTAG, 8, 50)));
        List<Belegung> alle = alpha(VOLLZEIT, spaet, frueh);

        for (int i = 1; i < alle.size(); i++) {
            assertTrue(
                    !alle.get(i)
                            .zeitraum()
                            .von()
                            .isBefore(alle.get(i - 1).zeitraum().von()),
                    "unsortiert bei " + i);
        }
    }

    @Test
    @DisplayName("Kein Montag ist keine Woche")
    void keinMontag() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Wochenansicht.Anfrage(
                        MONTAG.plusDays(1), Map.of(), List.of(), EINSTELLUNG, SIEBEN, NEUNZEHN));
    }
}
