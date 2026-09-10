package de.aptum.scheduling.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.infrastructure.MitDatenbank;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Die ganze Strecke: Verordnung anlegen, suchen, buchen, noch einmal buchen.
 *
 * <p>Über HTTP, gegen ein echtes Postgres, mit dem Mandanten im Header. Was
 * hier grün ist, kann man mit {@code curl} nachspielen — das ist der Punkt.
 */
class RestApiTest extends MitDatenbank {

    private static final LocalDate MONTAG = LocalDate.of(2026, 3, 2);

    @Autowired
    private TestRestTemplate http;

    private <T> ResponseEntity<T> als(String mandant, String pfad, Object koerper, Class<T> antwort) {
        HttpHeaders kopf = new HttpHeaders();
        kopf.setContentType(MediaType.APPLICATION_JSON);
        if (mandant != null) {
            kopf.set("X-Mandant", mandant);
        }
        return http.postForEntity(pfad, new HttpEntity<>(koerper, kopf), antwort);
    }

    private <T> ResponseEntity<T> lese(String mandant, String pfad, Class<T> antwort) {
        HttpHeaders kopf = new HttpHeaders();
        kopf.set("X-Mandant", mandant);
        return http.exchange(pfad, org.springframework.http.HttpMethod.GET, new HttpEntity<>(kopf), antwort);
    }

    @Test
    @DisplayName("Die Prüfung nennt die Regeln, bucht aber nichts - zweimal geprüft bleibt frei")
    void pruefungBuchtNicht() {
        UUID verordnung = verordnungAnlegen("praxis-a");
        Dto.Terminvorschlag slot = als("praxis-a", "/termine/suche", dieseWoche(verordnung), Dto.Suchantwort.class)
                .getBody()
                .vorschlaege()
                .get(0);
        Dto.Buchung buchung =
                new Dto.Buchung(verordnung, "KG_EINZEL", slot.therapeut(), slot.raum(), slot.beginn(), null);

        ResponseEntity<Dto.Buchungsantwort> erste =
                als("praxis-a", "/termine/pruefung", buchung, Dto.Buchungsantwort.class);
        assertEquals(HttpStatus.OK, erste.getStatusCode());
        assertEquals("FREI", erste.getBody().ausgang());
        assertNull(erste.getBody().termin(), "geprüft ist nicht gebucht");
        assertFalse(erste.getBody().regeln().isEmpty(), "alle Regeln stehen in der Antwort");
        assertTrue(erste.getBody().regeln().stream()
                .allMatch(r -> r.begruendung() != null && !r.begruendung().isBlank()));

        ResponseEntity<Dto.Buchungsantwort> zweite =
                als("praxis-a", "/termine/pruefung", buchung, Dto.Buchungsantwort.class);
        assertEquals("FREI", zweite.getBody().ausgang(), "die erste Prüfung hat nichts belegt");
    }

    @Test
    @DisplayName("Die Woche zeigt den gebuchten Termin mit Rüstzeit - und dem anderen Mandanten nichts")
    void dieWocheNachDerBuchung() {
        UUID verordnung = verordnungAnlegen("praxis-a");
        Dto.Terminvorschlag slot = als("praxis-a", "/termine/suche", dieseWoche(verordnung), Dto.Suchantwort.class)
                .getBody()
                .vorschlaege()
                .get(0);
        als(
                "praxis-a",
                "/termine",
                new Dto.Buchung(verordnung, "KG_EINZEL", slot.therapeut(), slot.raum(), slot.beginn(), null),
                Dto.Buchungsantwort.class);

        ResponseEntity<Dto.Woche> woche =
                lese("praxis-a", "/kalender/woche?tag=" + MONTAG.plusDays(2), Dto.Woche.class);
        assertEquals(HttpStatus.OK, woche.getStatusCode());
        assertEquals(MONTAG, woche.getBody().montag(), "ein Mittwoch ergibt die Woche seines Montags");
        assertEquals(2, woche.getBody().spalten().size());

        Dto.Spalte spalte = woche.getBody().spalten().stream()
                .filter(s -> s.therapeut().equals(slot.therapeut()))
                .findFirst()
                .orElseThrow();
        // Andere Tests buchen in derselben Woche; gezählt wird deshalb nicht,
        // sondern der Block gesucht, der zu dieser Buchung gehört.
        Dto.Belegung belegt = spalte.belegungen().stream()
                .filter(b -> b.art().equals("BELEGT"))
                .filter(b -> b.von().toInstant().equals(slot.beginn().toInstant()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("gebuchter Termin fehlt im Gitter"));
        assertEquals(slot.raum(), belegt.raum());
        assertTrue(
                spalte.belegungen().stream()
                        .anyMatch(b -> b.art().equals("RUESTZEIT")
                                && b.bis().toInstant().equals(belegt.von().toInstant())),
                "Vorbereitung endet, wo die Behandlung beginnt");
        assertTrue(
                spalte.belegungen().stream()
                        .anyMatch(b -> b.art().equals("RUESTZEIT")
                                && b.von().toInstant().equals(belegt.bis().toInstant())),
                "Nachbereitung beginnt, wo die Behandlung endet");

        Dto.Woche fremd = lese("praxis-b", "/kalender/woche?tag=" + MONTAG, Dto.Woche.class)
                .getBody();
        assertTrue(
                fremd.spalten().stream().flatMap(s -> s.belegungen().stream()).noneMatch(b -> b.art()
                        .equals("BELEGT")),
                "Mandant B sieht keinen Termin von A");
    }

    private UUID verordnungAnlegen(String mandant) {
        ResponseEntity<Dto.VerordnungAngelegt> antwort = als(
                mandant,
                "/verordnungen",
                new Dto.VerordnungAnlage(MONTAG.minusDays(3), false, "WS", 6, 1, 3),
                Dto.VerordnungAngelegt.class);
        assertEquals(HttpStatus.CREATED, antwort.getStatusCode());
        return antwort.getBody().id();
    }

    private Dto.Suche dieseWoche(UUID verordnung) {
        return new Dto.Suche(
                verordnung,
                "KG_EINZEL",
                MONTAG,
                MONTAG.plusDays(4),
                LocalTime.of(9, 0),
                LocalTime.of(12, 0),
                List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY));
    }

    @Test
    @DisplayName("Anlegen, suchen, buchen - und der zweite Versuch auf denselben Slot wird blockiert")
    void dieGanzeStrecke() {
        UUID verordnung = verordnungAnlegen("praxis-a");

        ResponseEntity<Dto.Suchantwort> suche =
                als("praxis-a", "/termine/suche", dieseWoche(verordnung), Dto.Suchantwort.class);
        assertEquals(HttpStatus.OK, suche.getStatusCode());
        assertFalse(suche.getBody().vorschlaege().isEmpty(), suche.getBody().zusammenfassung());
        Dto.Terminvorschlag erster = suche.getBody().vorschlaege().get(0);

        Dto.Buchung buchung =
                new Dto.Buchung(verordnung, "KG_EINZEL", erster.therapeut(), erster.raum(), erster.beginn(), null);
        ResponseEntity<Dto.Buchungsantwort> gebucht = als("praxis-a", "/termine", buchung, Dto.Buchungsantwort.class);
        assertEquals(HttpStatus.CREATED, gebucht.getStatusCode());
        assertEquals("FREI", gebucht.getBody().ausgang());
        assertNotNull(gebucht.getBody().termin());

        ResponseEntity<Dto.Buchungsantwort> nochmal = als("praxis-a", "/termine", buchung, Dto.Buchungsantwort.class);
        assertEquals(HttpStatus.CONFLICT, nochmal.getStatusCode());
        assertEquals("BLOCKIERT", nochmal.getBody().ausgang());
        assertNull(nochmal.getBody().termin());
        assertTrue(
                nochmal.getBody().regeln().stream()
                        .anyMatch((r) ->
                                r.ausgang().equals("VERLETZT") && r.regel().contains("Therapeut")),
                "die Antwort nennt die verletzte Regel");
    }

    @Test
    @DisplayName("Mit Begruendung darf uebersteuert werden, und die Antwort sagt es")
    void uebersteuern() {
        UUID verordnung = verordnungAnlegen("praxis-a");
        Dto.Terminvorschlag slot = als("praxis-a", "/termine/suche", dieseWoche(verordnung), Dto.Suchantwort.class)
                .getBody()
                .vorschlaege()
                .get(0);
        Dto.Buchung einmal =
                new Dto.Buchung(verordnung, "KG_EINZEL", slot.therapeut(), slot.raum(), slot.beginn(), null);
        als("praxis-a", "/termine", einmal, Dto.Buchungsantwort.class);

        Dto.Buchung trotzdem = new Dto.Buchung(
                verordnung,
                "KG_EINZEL",
                slot.therapeut(),
                slot.raum(),
                slot.beginn(),
                new Dto.Uebersteuerung(
                        "Patient kann nur zu dieser Zeit, Absprache mit der Praxisleitung", "Rezeption A"));
        ResponseEntity<Dto.Buchungsantwort> antwort = als("praxis-a", "/termine", trotzdem, Dto.Buchungsantwort.class);

        assertEquals(HttpStatus.CREATED, antwort.getStatusCode());
        assertEquals("UEBERSTEUERT", antwort.getBody().ausgang());
        assertNotNull(antwort.getBody().termin());
    }

    @Test
    @DisplayName("Ohne Mandant im Header: 400, kein Standardmandant")
    void ohneMandant() {
        ResponseEntity<String> antwort =
                als(null, "/verordnungen", new Dto.VerordnungAnlage(MONTAG, false, "WS", 6, 1, 3), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, antwort.getStatusCode());
        assertTrue(antwort.getBody().contains("X-Mandant"));
    }

    @Test
    @DisplayName("Die Verordnung des einen Mandanten ist fuer den anderen 404")
    void fremdeVerordnungIstNichtDa() {
        UUID verordnung = verordnungAnlegen("praxis-a");
        ResponseEntity<String> antwort = als("praxis-b", "/termine/suche", dieseWoche(verordnung), String.class);
        assertEquals(HttpStatus.NOT_FOUND, antwort.getStatusCode(), antwort.getBody());
    }

    @Test
    @DisplayName("Eine Verordnung ueber der Hoechstmenge wird beim Erfassen abgewiesen")
    void hoechstmengeBeimErfassen() {
        ResponseEntity<String> antwort =
                als("praxis-a", "/verordnungen", new Dto.VerordnungAnlage(MONTAG, false, "WS", 10, 1, 3), String.class);
        assertEquals(HttpStatus.BAD_REQUEST, antwort.getStatusCode());
        assertTrue(antwort.getBody().contains("Höchstmenge"), antwort.getBody());
    }

    @Test
    @DisplayName("Der Health-Endpunkt braucht keinen Mandanten")
    void healthOhneMandant() {
        assertEquals(
                HttpStatus.OK,
                http.getForEntity("/actuator/health", String.class).getStatusCode());
    }
}
