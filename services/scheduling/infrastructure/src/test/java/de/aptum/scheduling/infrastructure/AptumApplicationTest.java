package de.aptum.scheduling.infrastructure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/** Die Anwendung startet, migriert und antwortet. Mehr behauptet dieser Test nicht. */
class AptumApplicationTest extends MitDatenbank {

    @Autowired
    private TestRestTemplate http;

    @Test
    @DisplayName("Der Kontext startet, Flyway migriert, der Health-Endpunkt meldet UP")
    void startetUndAntwortet() {
        ResponseEntity<String> antwort = http.getForEntity("/actuator/health", String.class);
        assertEquals(HttpStatus.OK, antwort.getStatusCode());
        assertTrue(antwort.getBody().contains("UP"), antwort.getBody());
    }
}
