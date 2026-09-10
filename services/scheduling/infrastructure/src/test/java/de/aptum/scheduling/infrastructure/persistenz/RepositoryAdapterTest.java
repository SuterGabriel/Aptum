package de.aptum.scheduling.infrastructure.persistenz;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.application.mandant.MandantId;
import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.TerminId;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.port.TerminRepository;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.infrastructure.MitDatenbank;
import de.aptum.scheduling.infrastructure.mandant.MandantKontextHalter;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Hin und zurück durch die Tabellen — und dabei durch die Policy.
 *
 * <p>Die Adapter kennen keinen Mandanten. Der Test prüft, dass sie trotzdem
 * getrennt arbeiten: Was A speichert, lädt B nicht, obwohl B dieselbe
 * Kennung kennt.
 */
class RepositoryAdapterTest extends MitDatenbank {

    private static final MandantId PRAXIS_A = new MandantId("praxis-a");
    private static final MandantId PRAXIS_B = new MandantId("praxis-b");

    @Autowired
    private MandantKontextHalter mandant;

    @Autowired
    private VerordnungRepository verordnungen;

    @Autowired
    private TerminRepository termine;

    @Autowired
    private Stammdaten stammdaten;

    @Test
    @DisplayName("Eine Verordnung mit Verlauf kommt so zurueck, wie sie hineinging")
    void verordnungHinUndZurueck() {
        Verordnung verordnung =
                new Verordnung(LocalDate.of(2026, 3, 2), true, Diagnosegruppe.LY, 6, Frequenz.spanne(1, 3));
        VerordnungAkte akte = VerordnungAkte.neu(verordnung)
                .mitBehandlung(Behandlungstermin.an(LocalDate.of(2026, 3, 5)))
                .mitBehandlung(new Behandlungstermin(LocalDate.of(2026, 3, 30), Unterbrechungskennzeichen.K));

        mandant.als(PRAXIS_A, () -> verordnungen.speichere(akte));
        VerordnungAkte geladen =
                mandant.als(PRAXIS_A, () -> verordnungen.lade(akte.id()).orElseThrow());

        assertEquals(akte.verordnung(), geladen.verordnung());
        assertEquals(2, geladen.verlauf().anzahlBehandlungen());
        assertEquals(
                Unterbrechungskennzeichen.K,
                geladen.verlauf().termine().get(1).kennzeichen(),
                "das Kennzeichen der zweiten Behandlung ist erhalten");
    }

    @Test
    @DisplayName("Die Akte des einen Mandanten ist fuer den anderen nicht da")
    void akteBleibtBeimMandanten() {
        Verordnung verordnung =
                new Verordnung(LocalDate.of(2026, 3, 2), false, Diagnosegruppe.WS, 6, Frequenz.genau(2));
        VerordnungAkte akte = VerordnungAkte.neu(verordnung);

        mandant.als(PRAXIS_A, () -> verordnungen.speichere(akte));

        assertTrue(
                mandant.als(PRAXIS_B, () -> verordnungen.lade(akte.id())).isEmpty(),
                "B kennt die Kennung, sieht aber nichts");
    }

    @Test
    @DisplayName("Ein Termin kommt mit Person, Raum und Zone zurueck")
    void terminHinUndZurueck() {
        Zeitraum montagZehn =
                Zeitraum.ab(LocalDate.of(2026, 3, 2).atTime(10, 0).atZone(Zeitraum.PRAXIS), Duration.ofMinutes(25));
        Termin termin = new Termin(
                stammdaten.therapeut("T. Alpha").orElseThrow(),
                stammdaten.raum("Raum 1").orElseThrow(),
                Heilmittel.KG_EINZEL,
                montagZehn);

        mandant.als(PRAXIS_A, () -> termine.speichere(TerminId.neu(), termin));
        List<Termin> imMaerz = mandant.als(
                PRAXIS_A,
                () -> termine.imZeitraum(
                        Zeitraum.ab(LocalDate.of(2026, 3, 1).atStartOfDay(Zeitraum.PRAXIS), Duration.ofDays(31))));

        assertTrue(imMaerz.contains(termin), "derselbe Termin, samt Zone der Praxis");
        assertTrue(
                mandant.als(PRAXIS_B, () -> termine.imZeitraum(montagZehn)).isEmpty(),
                "B sieht den Kalender von A nicht");
    }

    @Test
    @DisplayName("Ein Termin ausserhalb des Zeitraums wird nicht geladen")
    void zeitraumGrenzt() {
        Zeitraum montagZehn =
                Zeitraum.ab(LocalDate.of(2026, 3, 2).atTime(10, 0).atZone(Zeitraum.PRAXIS), Duration.ofMinutes(25));
        Termin termin = new Termin(
                stammdaten.therapeut("T. Beta").orElseThrow(),
                stammdaten.raum("Raum 2").orElseThrow(),
                Heilmittel.KG_EINZEL,
                montagZehn);
        mandant.als(PRAXIS_A, () -> termine.speichere(TerminId.neu(), termin));

        Zeitraum dienstag = Zeitraum.ab(LocalDate.of(2026, 3, 3).atStartOfDay(Zeitraum.PRAXIS), Duration.ofDays(1));
        assertTrue(mandant.als(PRAXIS_A, () -> termine.imZeitraum(dienstag)).stream()
                .noneMatch((t) -> t.equals(termin)));
    }
}
