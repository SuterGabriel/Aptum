package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.VerordnungAnlegen;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungId;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Verordnungen erfassen. Nur das — Lesen und Ändern kommen mit dem Bedarf. */
@RestController
@RequestMapping("/verordnungen")
class VerordnungController {

    private final VerordnungAnlegen anlegen;

    VerordnungController(VerordnungAnlegen anlegen) {
        this.anlegen = anlegen;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    Dto.VerordnungAngelegt anlegen(@RequestBody Dto.VerordnungAnlage anlage) {
        Verordnung verordnung = new Verordnung(
                anlage.ausstellungsdatum(),
                anlage.dringlicherBedarf(),
                Diagnosegruppe.valueOf(anlage.diagnosegruppe()),
                anlage.verordneteEinheiten(),
                Frequenz.spanne(anlage.frequenzMin(), anlage.frequenzMax()));
        VerordnungId id = anlegen.ausfuehren(verordnung);
        return new Dto.VerordnungAngelegt(id.wert());
    }
}
