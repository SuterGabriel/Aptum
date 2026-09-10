package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.WocheAnzeigen;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Die Woche fürs Gitter. Lesend, deshalb {@code GET}; der Tag ist ein
 * Parameter, weil die Woche keine Ressource mit Kennung ist.
 */
@RestController
@RequestMapping("/kalender")
class KalenderController {

    private final WocheAnzeigen anzeigen;

    KalenderController(WocheAnzeigen anzeigen) {
        this.anzeigen = anzeigen;
    }

    /** Ohne Parameter: die laufende Woche. */
    @GetMapping("/woche")
    Dto.Woche woche(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate tag) {
        return Dto.Woche.von(anzeigen.ausfuehren(tag != null ? tag : LocalDate.now(Zeitraum.PRAXIS)));
    }
}
