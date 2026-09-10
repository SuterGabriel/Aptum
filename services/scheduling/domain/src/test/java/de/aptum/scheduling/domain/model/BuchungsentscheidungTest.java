package de.aptum.scheduling.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Blockieren mit begründungspflichtiger Übersteuerung. ADR-009. */
class BuchungsentscheidungTest {

    private static final Pruefbericht VERLETZT = new Pruefbericht(List.of(
            Pruefergebnis.erfuellt("Verordnungsfrist", "ok"),
            Pruefergebnis.verletzt("Raumausstattung", "Raum 2 hat 24 m², gefordert sind 30")));

    private static final Pruefbericht NUR_WARNUNG =
            new Pruefbericht(List.of(Pruefergebnis.warnung("Frequenz", "dies wäre die 3. Behandlung in dieser Woche")));

    private static final Uebersteuerung BEGRUENDET =
            new Uebersteuerung("Patient kann nur heute, Raum 3 ist defekt", "Rezeption A");

    @Test
    @DisplayName("Eine verletzte Regel blockiert")
    void verletztBlockiert() {
        Buchungsentscheidung e = Buchungsentscheidung.aus(VERLETZT);
        assertEquals(Buchungsentscheidung.Ausgang.BLOCKIERT, e.ausgang());
        assertFalse(e.darfGebuchtWerden());
    }

    @Test
    @DisplayName("Eine Warnung blockiert nicht und braucht keine Uebersteuerung")
    void warnungGehtDurch() {
        Buchungsentscheidung e = Buchungsentscheidung.aus(NUR_WARNUNG);
        assertEquals(Buchungsentscheidung.Ausgang.FREI, e.ausgang());
        assertTrue(e.darfGebuchtWerden());
    }

    @Test
    @DisplayName("Mit Begruendung darf trotzdem gebucht werden, und die Begruendung bleibt")
    void uebersteuertMitBegruendung() {
        Buchungsentscheidung e = Buchungsentscheidung.mitUebersteuerung(VERLETZT, BEGRUENDET);
        assertEquals(Buchungsentscheidung.Ausgang.UEBERSTEUERT, e.ausgang());
        assertTrue(e.darfGebuchtWerden());
        assertEquals(BEGRUENDET, e.uebersteuerung().orElseThrow(), "die Begruendung wird protokolliert");
    }

    @Test
    @DisplayName("Ohne Begruendung gibt es keine Uebersteuerung")
    void leereBegruendungIstKeine() {
        assertThrows(IllegalArgumentException.class, () -> new Uebersteuerung("   ", "Rezeption A"));
        assertThrows(IllegalArgumentException.class, () -> new Uebersteuerung("Grund", ""));
    }

    @Test
    @DisplayName("Eine Uebersteuerung ohne Verstoss wird nicht gespeichert")
    void uebersteuerungOhneVerstossVerpufft() {
        Buchungsentscheidung e = Buchungsentscheidung.mitUebersteuerung(NUR_WARNUNG, BEGRUENDET);
        assertEquals(Buchungsentscheidung.Ausgang.FREI, e.ausgang());
        assertTrue(e.uebersteuerung().isEmpty(), "nichts wurde uebersteuert, also steht nichts im Protokoll");
    }
}
