package de.aptum.scheduling.infrastructure;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Ein echtes Postgres für die ganze Test-JVM.
 *
 * <p>Ein Container, einmal gestartet, nie von Hand gestoppt — Ryuk räumt ihn
 * am Ende auf. Nicht {@code @Container}: Das würde ihn je Testklasse starten
 * und stoppen, während Spring den Kontext über Klassen hinweg cacht. Die
 * zweite Klasse fände dann einen Kontext mit der JDBC-URL eines Containers,
 * der nicht mehr läuft, und scheiterte an einem toten Port.
 *
 * <p>Zwei Verbindungen, wie im Betrieb: Flyway migriert als Superuser des
 * Containers, die Anwendung verbindet sich als {@code aptum_app} — angelegt
 * vom Init-Skript, ohne Superuser-Rechte, nicht Eigentümerin der Tabellen.
 * Wer hier den Container-Benutzer für die Anwendung nähme, bekäme grüne
 * Isolationstests, die nichts prüfen: Superuser sehen jede Zeile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class MitDatenbank {

    static final PostgreSQLContainer<?> DB =
            new PostgreSQLContainer<>("postgres:16-alpine").withInitScript("db/test-rolle.sql");

    static {
        DB.start();
    }

    @DynamicPropertySource
    static void verbindungen(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", DB::getJdbcUrl);
        registry.add("spring.datasource.username", () -> "aptum_app");
        registry.add("spring.datasource.password", () -> "aptum");
        registry.add("spring.flyway.url", DB::getJdbcUrl);
        registry.add("spring.flyway.user", DB::getUsername);
        registry.add("spring.flyway.password", DB::getPassword);
    }
}
