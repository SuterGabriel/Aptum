package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.AbrechnungAnzeigen;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Die Abrechnungsübersicht: eine Zeile je Verordnung, lesend. Keine
 * Parameter — sortieren und filtern tut die Tabelle im Frontend, und für die
 * Größe einer Praxis ist das die richtige Stelle (ADR-011).
 */
@RestController
@RequestMapping("/abrechnung")
class AbrechnungController {

    private final AbrechnungAnzeigen anzeigen;

    AbrechnungController(AbrechnungAnzeigen anzeigen) {
        this.anzeigen = anzeigen;
    }

    @GetMapping("/uebersicht")
    Dto.Abrechnungsuebersicht uebersicht() {
        return Dto.Abrechnungsuebersicht.von(anzeigen.ausfuehren());
    }
}
