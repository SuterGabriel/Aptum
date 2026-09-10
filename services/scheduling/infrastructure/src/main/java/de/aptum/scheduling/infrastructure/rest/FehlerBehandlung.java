package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.UnbekannteVerordnung;
import de.aptum.scheduling.application.anwendungsfall.UngueltigeVerordnung;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Fachliche Ausnahmen in Antworten übersetzen. Der Text der Ausnahme ist die
 * Begründung — sie ist für Menschen geschrieben, und sie verrät nichts über
 * fremde Mandanten, weil die Anwendungsfälle das schon nicht tun.
 */
@RestControllerAdvice
class FehlerBehandlung {

    @ExceptionHandler(UnbekannteVerordnung.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    Dto.Fehler unbekannt(UnbekannteVerordnung e) {
        return new Dto.Fehler(e.getMessage());
    }

    @ExceptionHandler({UngueltigeVerordnung.class, IllegalArgumentException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    Dto.Fehler ungueltig(RuntimeException e) {
        return new Dto.Fehler(e.getMessage());
    }
}
