package de.aptum.scheduling.infrastructure.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import de.aptum.scheduling.infrastructure.MitDatenbank;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Die Schnittstellenbeschreibung im Repo ist die, die die Anwendung liefert.
 *
 * <p>{@code docs/api/openapi.json} ist erzeugt, nicht geschrieben. Dieser Test
 * vergleicht die Datei mit dem, was {@code /v3/api-docs} antwortet, und wird
 * rot, sobald ein Controller sich ändert und die Datei nicht. Aktualisieren:
 * {@code mvn test -Dtest=OpenApiTest -Dopenapi.aktualisieren=true}.
 */
class OpenApiTest extends MitDatenbank {

    private static final Path DOKUMENT =
            Path.of("..", "..", "..", "docs", "api", "openapi.json").normalize();

    @Autowired
    private TestRestTemplate http;

    private final ObjectMapper json = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    @Test
    @DisplayName("Die Beschreibung ist mandantenfrei erreichbar und nennt die Endpunkte")
    void erreichbar() {
        ResponseEntity<String> antwort = http.getForEntity("/v3/api-docs", String.class);
        assertEquals(HttpStatus.OK, antwort.getStatusCode());
        assertTrue(antwort.getBody().contains("/termine/suche"));
        assertTrue(antwort.getBody().contains("/verordnungen"));
        assertTrue(antwort.getBody().contains("X-Mandant"), "der Platzhalter ist dokumentiert");
    }

    @Test
    @DisplayName("docs/api/openapi.json entspricht der laufenden Anwendung")
    void dokumentImRepoIstAktuell() throws IOException {
        JsonNode geliefert =
                json.readTree(http.getForEntity("/v3/api-docs", String.class).getBody());
        // Der Server-Eintrag trägt den zufälligen Port des Testlaufs. Er ist
        // keine Eigenschaft der Schnittstelle und würde jeden Vergleich brechen.
        ((com.fasterxml.jackson.databind.node.ObjectNode) geliefert).remove("servers");

        if (Boolean.getBoolean("openapi.aktualisieren")) {
            Files.createDirectories(DOKUMENT.getParent());
            Files.writeString(DOKUMENT, json.writeValueAsString(geliefert) + "\n", StandardCharsets.UTF_8);
        }

        assertTrue(Files.exists(DOKUMENT), "Dokument fehlt - einmal mit -Dopenapi.aktualisieren=true erzeugen");
        JsonNode imRepo = json.readTree(Files.readString(DOKUMENT, StandardCharsets.UTF_8));
        assertEquals(
                imRepo,
                geliefert,
                "docs/api/openapi.json ist veraltet. Aktualisieren: "
                        + "mvn test -Dtest=OpenApiTest -Dopenapi.aktualisieren=true");
    }
}
