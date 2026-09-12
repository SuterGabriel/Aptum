package de.aptum.scheduling.domain.abrechnung;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.regel.Regelwerk;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Die Übersicht urteilt nicht selbst; sie lässt die Regeln urteilen. Die
 * Tests hier prüfen deshalb nicht Fristen — das tun die Regeltests — sondern,
 * dass jede Zeile den Zustand trägt, den die Regeln ergeben, und die Zahlen,
 * die die Abrechnung braucht.
 */
class AbrechnungsuebersichtTest {

    /** Ausstellung am 2. Januar 2026; erkennbar synthetisch, kein Bezug zu einer Person. */
    private static final LocalDate AUSSTELLUNG = LocalDate.of(2026, 1, 2);

    private final Abrechnungsuebersicht uebersicht = new Abrechnungsuebersicht(new Regelwerk());

    private static Verordnung physio(int einheiten) {
        return new Verordnung(AUSSTELLUNG, false, Diagnosegruppe.WS, einheiten, Frequenz.spanne(1, 3));
    }

    private static VerordnungAkte akte(Verordnung verordnung, LocalDate... behandlungen) {
        List<Behandlungstermin> termine =
                Arrays.stream(behandlungen).map(Behandlungstermin::an).toList();
        return new VerordnungAkte(VerordnungId.neu(), verordnung, Behandlungsverlauf.aus(termine));
    }

    private Abrechnungszeile einzige(VerordnungAkte akte) {
        List<Abrechnungszeile> zeilen = uebersicht.berechne(List.of(akte));
        assertEquals(1, zeilen.size());
        return zeilen.get(0);
    }

    @Test
    @DisplayName("Ohne Behandlung: nicht begonnen, nichts erbracht, alles offen, kein Bericht")
    void nichtBegonnen() {
        Abrechnungszeile z = einzige(akte(physio(6)));
        assertEquals(Abrechnungsstatus.NICHT_BEGONNEN, z.status());
        assertEquals(0, z.erbracht());
        assertEquals(6, z.offen());
        assertTrue(z.ersteBehandlung().isEmpty());
        assertTrue(z.bericht().ergebnisse().isEmpty(), "ohne Erbrachtes gibt es nichts zu prüfen");
    }

    @Test
    @DisplayName("Drei Behandlungen im Takt: prüffest, drei erbracht, drei offen, jede Regel benannt")
    void prueffest() {
        Abrechnungszeile z =
                einzige(akte(physio(6), AUSSTELLUNG.plusDays(3), AUSSTELLUNG.plusDays(6), AUSSTELLUNG.plusDays(10)));
        assertEquals(Abrechnungsstatus.PRUEFFEST, z.status());
        assertEquals(3, z.erbracht());
        assertEquals(3, z.offen());
        assertEquals(AUSSTELLUNG.plusDays(3), z.ersteBehandlung().orElseThrow());
        assertEquals(AUSSTELLUNG.plusDays(10), z.letzteBehandlung().orElseThrow());
        assertEquals(5, z.bericht().ergebnisse().size(), "Höchstmenge, Frist, Unterbrechung, Summe, Gültigkeit");
        assertTrue(z.begruendung().contains("keine verletzt"));
    }

    @Test
    @DisplayName("Erste Behandlung 40 Tage nach Ausstellung: beanstandet, und die Zeile nennt die Frist")
    void fristVersaeumt() {
        Abrechnungszeile z = einzige(akte(physio(6), AUSSTELLUNG.plusDays(40)));
        assertEquals(Abrechnungsstatus.BEANSTANDET, z.status());
        assertEquals(1, z.erbracht());
        assertTrue(z.begruendung().startsWith("Verordnungsfrist:"), z.begruendung());
        assertTrue(z.begruendung().contains("Gültigkeit verloren"), z.begruendung());
    }

    @Test
    @DisplayName("Eine unbegründete Pause von 30 Tagen: beanstandet wegen Unterbrechung, nicht wegen Frist")
    void unterbrechungZuLang() {
        Abrechnungszeile z = einzige(akte(physio(6), AUSSTELLUNG.plusDays(3), AUSSTELLUNG.plusDays(33)));
        assertEquals(Abrechnungsstatus.BEANSTANDET, z.status());
        assertTrue(z.begruendung().startsWith("Unterbrechung:"), z.begruendung());
    }

    @Test
    @DisplayName("Mehr erbracht als verordnet - nach Übersteuerung möglich - ergibt null offen, nicht minus")
    void offenNieNegativ() {
        Abrechnungszeile z =
                einzige(akte(physio(2), AUSSTELLUNG.plusDays(3), AUSSTELLUNG.plusDays(6), AUSSTELLUNG.plusDays(9)));
        assertEquals(3, z.erbracht());
        assertEquals(0, z.offen());
    }

    @Test
    @DisplayName("Die Zeilen kommen nach Ausstellungsdatum, gleich wie hineingegeben oder nicht")
    void nachAusstellungsdatum() {
        Verordnung spaeter =
                new Verordnung(AUSSTELLUNG.plusDays(20), false, Diagnosegruppe.WS, 6, Frequenz.spanne(1, 3));
        List<Abrechnungszeile> zeilen = uebersicht.berechne(List.of(akte(spaeter), akte(physio(6))));
        assertEquals(AUSSTELLUNG, zeilen.get(0).verordnung().ausstellungsdatum());
        assertEquals(AUSSTELLUNG.plusDays(20), zeilen.get(1).verordnung().ausstellungsdatum());
    }
}
