package de.aptum.scheduling.infrastructure.mandant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.aptum.scheduling.application.mandant.MandantId;
import de.aptum.scheduling.infrastructure.MitDatenbank;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Das Gate der ADR-002: Mandant A sieht nichts von Mandant B.
 *
 * <p>Absichtlich mit rohem SQL statt über Repositories. Der Test soll die
 * Datenbank prüfen, nicht die Disziplin des Codes darüber. Ein
 * {@code select * from verordnung} ohne jede {@code WHERE}-Klausel muss
 * trotzdem nur die eigenen Zeilen liefern — und ohne gesetzten Mandanten gar
 * keine.
 *
 * <p>Die Anwendung verbindet sich als {@code aptum_app}, nicht als
 * Eigentümerin. Wäre sie es, wäre jeder Test hier grün und keiner wahr.
 */
class MandantIsolationTest extends MitDatenbank {

    private static final MandantId PRAXIS_A = new MandantId("praxis-a");
    private static final MandantId PRAXIS_B = new MandantId("praxis-b");

    @Autowired
    private MandantKontextHalter mandant;

    @Autowired
    private TransactionTemplate transaktion;

    @Autowired
    private JdbcTemplate jdbc;

    private UUID zeileVonA;
    private UUID zeileVonB;

    @BeforeEach
    void zweiMandantenSchreiben() {
        // Jeder räumt nur seine eigenen Zeilen weg - mehr lässt die Policy
        // auch beim Löschen nicht zu. Danach hat jeder genau eine.
        loescheAlleAls(PRAXIS_A);
        loescheAlleAls(PRAXIS_B);
        zeileVonA = schreibeAls(PRAXIS_A);
        zeileVonB = schreibeAls(PRAXIS_B);
    }

    private void loescheAlleAls(MandantId praxis) {
        mandant.als(praxis, () -> transaktion.execute((status) -> jdbc.update("delete from verordnung")));
    }

    @Test
    @DisplayName("Jeder Mandant sieht genau seine Zeilen - ohne WHERE-Klausel")
    void jederSiehtNurSeine() {
        assertEquals(1L, zaehleAls(PRAXIS_A));
        assertEquals(1L, zaehleAls(PRAXIS_B));
    }

    @Test
    @DisplayName("Die Zeile des anderen ist auch mit bekannter ID unsichtbar")
    void fremdeZeileBleibtUnsichtbar() {
        long treffer = mandant.als(
                PRAXIS_A,
                () -> transaktion.execute((status) ->
                        jdbc.queryForObject("select count(*) from verordnung where id = ?", Long.class, zeileVonB)));
        assertEquals(0L, treffer, "A kennt die ID von B und sieht die Zeile trotzdem nicht");
    }

    @Test
    @DisplayName("Ohne Mandant in der Sitzung liefert die Datenbank null Zeilen")
    void ohneMandantNullZeilen() {
        // Kein Mandant, keine Transaktion des MandantTransactionManager: eine
        // Verbindung aus dem Pool, wie sie eine vergessene Verdrahtung nutzen
        // würde. Die Variable ist nicht gesetzt, current_setting liefert NULL,
        // die Policy trifft keine Zeile.
        long treffer = jdbc.queryForObject("select count(*) from verordnung", Long.class);
        assertEquals(0L, treffer, "die vergessene WHERE-Klausel faellt geschlossen aus, nicht offen");
    }

    @Test
    @DisplayName("Ohne Mandant im Kontext beginnt keine Transaktion")
    void ohneMandantKeineTransaktion() {
        assertThrows(
                IllegalStateException.class,
                () -> transaktion.execute((status) -> jdbc.queryForObject("select 1", Integer.class)),
                "laut statt leise: kein Rueckfall auf einen Standardmandanten");
    }

    @Test
    @DisplayName("Loeschen und Aendern beim anderen Mandanten treffen nichts")
    void schreibzugriffeAufFremdeZeilenVerpuffen() {
        int geloescht = mandant.als(
                PRAXIS_A,
                () -> transaktion.execute((status) -> jdbc.update("delete from verordnung where id = ?", zeileVonB)));
        int geaendert = mandant.als(
                PRAXIS_A,
                () -> transaktion.execute((status) ->
                        jdbc.update("update verordnung set verordnete_einheiten = 99 where id = ?", zeileVonB)));

        assertEquals(0, geloescht);
        assertEquals(0, geaendert);
        assertEquals(1L, zaehleAls(PRAXIS_B), "B hat seine Zeile noch, unveraendert");
    }

    @Test
    @DisplayName("Eine Zeile fuer einen anderen Mandanten laesst sich nicht schreiben")
    void schreibenFuerFremdenMandantenScheitert() {
        assertThrows(
                Exception.class,
                () -> mandant.als(
                        PRAXIS_A,
                        () -> transaktion.execute((status) -> {
                            einfuegen(UUID.randomUUID(), PRAXIS_B);
                            return null;
                        })),
                "WITH CHECK: die Sitzung traegt A, die Zeile sagt B");
    }

    private UUID schreibeAls(MandantId praxis) {
        UUID id = UUID.randomUUID();
        mandant.als(
                praxis,
                () -> transaktion.execute((status) -> {
                    einfuegen(id, praxis);
                    return null;
                }));
        return id;
    }

    private void einfuegen(UUID id, MandantId praxis) {
        jdbc.update(
                "insert into verordnung (id, mandant_id, ausstellungsdatum, dringlicher_bedarf, diagnosegruppe, "
                        + "verordnete_einheiten, frequenz_min, frequenz_max) values (?, ?, ?, ?, ?, ?, ?, ?)",
                id,
                praxis.wert(),
                LocalDate.of(2026, 3, 2),
                false,
                "WS",
                6,
                1,
                3);
    }

    private long zaehleAls(MandantId praxis) {
        return mandant.als(
                praxis,
                () -> transaktion.execute(
                        (status) -> jdbc.queryForObject("select count(*) from verordnung", Long.class)));
    }
}
