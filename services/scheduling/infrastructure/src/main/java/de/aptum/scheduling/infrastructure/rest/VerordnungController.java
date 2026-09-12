package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.VerordnungAnlegen;
import de.aptum.scheduling.application.anwendungsfall.VerordnungAnzeigen;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungId;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Verordnungen erfassen und eine ansehen. Ändern kommt mit dem Bedarf. */
@RestController
@RequestMapping("/verordnungen")
class VerordnungController {

    private final VerordnungAnlegen anlegen;
    private final VerordnungAnzeigen anzeigen;

    VerordnungController(VerordnungAnlegen anlegen, VerordnungAnzeigen anzeigen) {
        this.anlegen = anlegen;
        this.anzeigen = anzeigen;
    }

    /** Der Vordruck samt Verlauf und Prüfstand; 404 für unbekannte wie für fremde Kennungen. */
    @GetMapping("/{id}")
    Dto.Verordnungsakte ansehen(@PathVariable UUID id) {
        return Dto.Verordnungsakte.von(anzeigen.ausfuehren(new VerordnungId(id)));
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
