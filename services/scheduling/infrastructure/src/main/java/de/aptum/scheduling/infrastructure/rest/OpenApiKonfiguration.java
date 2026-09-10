package de.aptum.scheduling.infrastructure.rest;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.parameters.HeaderParameter;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Die Schnittstellenbeschreibung, erzeugt aus den Controllern.
 *
 * <p>Der Mandant steht nicht in jedem Controller als Parameter, sondern hier
 * einmal für alle Operationen — genau wie er im Code an einer Stelle gelesen
 * wird. Die Beschreibung nennt ihn als das, was er ist: ein Platzhalter für
 * Authentifizierung.
 */
@Configuration
class OpenApiKonfiguration {

    @Bean
    OpenAPI aptumApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Aptum Scheduling")
                        .version("0.1")
                        .description("Terminsuche und Buchung für Physio- und Ergotherapiepraxen. "
                                + "Jeder Vorschlag und jede Buchung läuft durch dasselbe deterministische "
                                + "Regelwerk der Heilmittel-Richtlinie; die Antworten nennen je Regel Ausgang "
                                + "und Begründung. Daten sind je Mandant getrennt (Row Level Security)."));
    }

    /** Der Mandant als Kopfzeile an jeder Operation, die nicht mandantenfrei ist. */
    @Bean
    OperationCustomizer mandantKopfzeile() {
        return (operation, handlerMethod) -> operation.addParametersItem(new HeaderParameter()
                .name("X-Mandant")
                .required(true)
                .description("Die Praxis, für die die Anfrage gilt. Platzhalter für Authentifizierung: "
                        + "In einer echten Anwendung käme der Mandant aus einem signierten Token.")
                .example("praxis-a"));
    }
}
