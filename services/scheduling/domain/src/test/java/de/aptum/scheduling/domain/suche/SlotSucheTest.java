package de.aptum.scheduling.domain.suche;

import de.aptum.scheduling.domain.model.Abwesenheit;
import de.aptum.scheduling.domain.model.Arbeitszeit;
import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.Wunschfenster;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.model.Zertifikatsleistung;
import de.aptum.scheduling.domain.regel.Regelwerk;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Die Schnittmenge, an einer kleinen Praxis durchgespielt.
 *
 * <p>Zwei Personen, zwei Räume, eine Woche. Jeder Test verändert genau eine
 * Dimension und schaut, was aus der Trefferliste verschwindet — und ob der
 * Grund dafür in der Zählung der Ausschlüsse steht.
 */
class SlotSucheTest {

    /** Montag, 2. März 2026. */
    private static final LocalDate MONTAG = LocalDate.of(2026, 3, 2);

    private static final Therapeut ALPHA = Therapeut.ohneZertifikate("T. Alpha");
    private static final Therapeut BETA = Therapeut.mit("T. Beta",
            Zertifikatsleistung.LYMPHDRAINAGE, Zertifikatsleistung.GERAET);

    private static final Raum RAUM_1 = Raum.behandlungsraum("Raum 1", 24);
    private static final Raum GERAETE = new Raum("Gerätebereich", 40, 6, EnumSet.of(Raumanforderung.GERAETEBEREICH));

    private static final Dienstplan VOLLZEIT =
            Dienstplan.mit(Arbeitszeit.werktags(LocalTime.of(8, 0), LocalTime.of(17, 0)));
    private static final Praxiseinstellung EINSTELLUNG =
            new Praxiseinstellung(Duration.ofMinutes(5), Duration.ofMinutes(20));
    private static final Duration RASTER = Duration.ofMinutes(15);

    private final SlotSuche suche = new SlotSuche(new Regelwerk());

    private static Verordnung verordnung(Diagnosegruppe gruppe, int einheiten) {
        return new Verordnung(MONTAG.minusDays(7), false, gruppe, einheiten, Frequenz.spanne(1, 3));
    }

    /** Vormittags in dieser Woche, Montag bis Freitag. */
    private static Wunschfenster dieseWoche() {
        return Wunschfenster.werktags(MONTAG, MONTAG.plusDays(4))
                .zwischen(LocalTime.of(9, 0), LocalTime.of(12, 0));
    }

    private Suchanfrage anfrage(Heilmittel heilmittel, Verordnung verordnung, Behandlungsverlauf verlauf,
                                Wunschfenster wunsch, List<Termin> bestehende) {
        return new Suchanfrage(verordnung, verlauf, heilmittel, wunsch,
                Map.of(ALPHA, VOLLZEIT, BETA, VOLLZEIT), List.of(RAUM_1, GERAETE),
                bestehende, EINSTELLUNG, RASTER);
    }

    @Test
    @DisplayName("Der Grundfall: KG, beide Personen, nur der Behandlungsraum")
    void grundfall() {
        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_EINZEL, verordnung(Diagnosegruppe.WS, 6),
                Behandlungsverlauf.leer(), dieseWoche(), List.of()));

        assertFalse(ergebnis.istLeer());
        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> v.raum().equals(RAUM_1)),
                "KG gehört in den Behandlungsraum, nicht in den Gerätebereich");
        assertTrue(ergebnis.vorschlaege().stream().anyMatch((v) -> v.therapeut().equals(ALPHA)));
        assertTrue(ergebnis.vorschlaege().stream().anyMatch((v) -> v.therapeut().equals(BETA)));
        assertEquals(ergebnis.vorschlaege().size(), ergebnis.ausgeschlossen().get("Raumausstattung"),
                "jeder Vorschlag hat ein Gegenstück im Gerätebereich, das an der Ausstattung scheitert");
    }

    @Test
    @DisplayName("Dimension Therapeut: Lymphdrainage darf nur Beta")
    void qualifikationFiltertDiePerson() {
        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.MLD_GROSSBEHANDLUNG, verordnung(Diagnosegruppe.LY, 6),
                Behandlungsverlauf.leer(), dieseWoche(), List.of()));

        assertFalse(ergebnis.istLeer());
        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> v.therapeut().equals(BETA)));
        assertTrue(ergebnis.ausgeschlossen().containsKey("Qualifikation"),
                "die Ausschlüsse nennen den Grund, nicht nur die Zahl");
    }

    @Test
    @DisplayName("Dimension Raum: Gerätetraining nur im Gerätebereich")
    void ausstattungFiltertDenRaum() {
        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_GERAET, verordnung(Diagnosegruppe.WS, 6),
                Behandlungsverlauf.leer(), dieseWoche(), List.of()));

        assertFalse(ergebnis.istLeer());
        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> v.raum().equals(GERAETE)),
                "Gerätetraining nur im Gerätebereich");
        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> v.therapeut().equals(BETA)),
                "und nur mit der Person, die das Zertifikat hat");
        // Zwei Dimensionen greifen gleichzeitig: Alpha scheitert an der
        // Qualifikation, Raum 1 an der Ausstattung. Beide Gründe stehen da.
        assertTrue(ergebnis.ausgeschlossen().containsKey("Qualifikation"));
        assertTrue(ergebnis.ausgeschlossen().containsKey("Raumausstattung"));
    }

    @Test
    @DisplayName("Dimension Patient: nur dienstags, nur nachmittags")
    void wunschfensterSchneidetDieZeit() {
        Wunschfenster dienstagNachmittag = Wunschfenster.werktags(MONTAG, MONTAG.plusDays(4))
                .zwischen(LocalTime.of(14, 0), LocalTime.of(16, 0))
                .an(DayOfWeek.TUESDAY);

        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_EINZEL, verordnung(Diagnosegruppe.WS, 6),
                Behandlungsverlauf.leer(), dienstagNachmittag, List.of()));

        assertFalse(ergebnis.istLeer());
        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> {
            var lokal = v.behandlung().von().withZoneSameInstant(Zeitraum.PRAXIS);
            return lokal.getDayOfWeek() == DayOfWeek.TUESDAY && !lokal.toLocalTime().isBefore(LocalTime.of(14, 0));
        }));
    }

    @Test
    @DisplayName("Dimension Verordnung: die verfallene Verordnung liefert nichts, und sagt warum")
    void verfalleneVerordnungWirdGezaehlt() {
        // Ausgestellt vor 50 Tagen, letzte Behandlung vor 40, ohne Kennzeichen:
        // die Unterbrechung ist zu lang. Jeder Kandidat scheitert schon an der
        // Verordnung, bevor Person oder Raum gefragt werden.
        Verordnung aeltere = new Verordnung(
                MONTAG.minusDays(50), false, Diagnosegruppe.WS, 6, Frequenz.spanne(1, 3));
        Behandlungsverlauf langePause = Behandlungsverlauf.aus(List.of(
                Behandlungstermin.an(MONTAG.minusDays(40))));

        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_EINZEL, aeltere, langePause, dieseWoche(), List.of()));

        assertTrue(ergebnis.istLeer());
        assertTrue(ergebnis.ausgeschlossen().containsKey("Unterbrechung"),
                "die leere Liste erklärt sich: " + ergebnis.zusammenfassung());
        assertEquals(ergebnis.geprueft(), ergebnis.ausgeschlossen().get("Unterbrechung"),
                "ein verfallener Tag zählt so viele Kandidaten, wie er gehabt hätte");
    }

    @Test
    @DisplayName("Der Kalender: ein bestehender Termin nimmt Person und Raum zugleich")
    void bestehenderTerminBlockiert() {
        Zeitraum montagZehn = Zeitraum.ab(MONTAG.atTime(10, 0).atZone(Zeitraum.PRAXIS), Duration.ofMinutes(25));
        List<Termin> kalender = List.of(new Termin(ALPHA, RAUM_1, Heilmittel.KG_EINZEL, montagZehn));

        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_EINZEL, verordnung(Diagnosegruppe.WS, 6),
                Behandlungsverlauf.leer(), dieseWoche(), kalender));

        assertTrue(ergebnis.vorschlaege().stream().noneMatch((v) ->
                v.behandlung().ueberschneidet(montagZehn)
                        && (v.therapeut().equals(ALPHA) || v.raum().equals(RAUM_1))),
                "um zehn ist weder Alpha noch Raum 1 zu haben");
        assertTrue(ergebnis.ausgeschlossen().containsKey("Therapeut verfügbar")
                || ergebnis.ausgeschlossen().containsKey("Raum frei"));
    }

    @Test
    @DisplayName("Eine Warnung schliesst nicht aus, sie haengt am Vorschlag")
    void warnungBleibtAmVorschlag() {
        // Verordnet einmal wöchentlich, diese Woche schon behandelt: jeder
        // weitere Termin in der Woche ist eine Frequenzabweichung - Warnung,
        // kein Ausschluss, weil darüber ein Mensch entscheidet.
        Verordnung einmalWoechentlich = new Verordnung(
                MONTAG.minusDays(7), false, Diagnosegruppe.WS, 6, Frequenz.genau(1));
        Behandlungsverlauf schonBehandelt = Behandlungsverlauf.aus(List.of(Behandlungstermin.an(MONTAG)));

        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.KG_EINZEL, einmalWoechentlich, schonBehandelt, dieseWoche(), List.of()));

        assertFalse(ergebnis.istLeer(), "die Frequenz blockiert nie");
        assertTrue(ergebnis.vorschlaege().stream().allMatch(Vorschlag::hatWarnung));
        assertFalse(ergebnis.ausgeschlossen().containsKey("Frequenz"));
    }

    @Test
    @DisplayName("Urlaub nimmt eine Person aus der Woche")
    void abwesenheit() {
        Dienstplan alphaImUrlaub = VOLLZEIT.abwesend(new Abwesenheit(MONTAG, MONTAG.plusDays(4), "Urlaub"));
        Suchanfrage mitUrlaub = new Suchanfrage(
                verordnung(Diagnosegruppe.WS, 6), Behandlungsverlauf.leer(), Heilmittel.KG_EINZEL,
                dieseWoche(), Map.of(ALPHA, alphaImUrlaub, BETA, VOLLZEIT), List.of(RAUM_1),
                List.of(), EINSTELLUNG, RASTER);

        Suchergebnis ergebnis = suche.suche(mitUrlaub);

        assertTrue(ergebnis.vorschlaege().stream().allMatch((v) -> v.therapeut().equals(BETA)));
        assertTrue(ergebnis.ausgeschlossen().containsKey("Therapeut verfügbar"));
    }

    @Test
    @DisplayName("Die Zusammenfassung ist ein Satz fuer die Statuszeile")
    void zusammenfassung() {
        Suchergebnis ergebnis = suche.suche(anfrage(
                Heilmittel.MLD_GROSSBEHANDLUNG, verordnung(Diagnosegruppe.LY, 6),
                Behandlungsverlauf.leer(), dieseWoche(), List.of()));

        String satz = ergebnis.zusammenfassung();
        assertTrue(satz.contains("Vorschl"), satz);
        assertTrue(satz.contains("ausgeschlossen"), satz);
        assertTrue(satz.contains("Qualifikation"), satz);
    }
}
