package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.TerminBuchen;
import de.aptum.scheduling.application.anwendungsfall.TerminSuchen;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.TerminId;
import de.aptum.scheduling.domain.model.Uebersteuerung;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.model.Wunschfenster;
import de.aptum.scheduling.domain.suche.Suchergebnis;
import java.util.Set;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Suchen und buchen.
 *
 * <p>Alles ist ein {@code POST}: Die Suche trägt einen Körper und ist keine
 * Ressource, die Buchung legt eine an. Die Antwort auf eine blockierte
 * Buchung ist {@code 409} mit demselben Körper wie bei Erfolg — die Regeln
 * stehen drin, damit der Dialog sie anzeigen kann, nur die Kennung fehlt.
 *
 * <p>Die Prüfung unter {@code /termine/pruefung} liefert denselben Körper,
 * bucht aber nichts: Der Dialog zeigt die Regeln, bevor jemand entscheidet.
 */
@RestController
@RequestMapping("/termine")
class TerminController {

    private final TerminSuchen suchen;
    private final TerminBuchen buchen;

    TerminController(TerminSuchen suchen, TerminBuchen buchen) {
        this.suchen = suchen;
        this.buchen = buchen;
    }

    @PostMapping("/suche")
    Dto.Suchantwort suchen(@RequestBody Dto.Suche suche) {
        Wunschfenster wunsch = new Wunschfenster(
                suche.von(), suche.bis(), suche.fruehestens(), suche.spaetestens(), Set.copyOf(suche.wochentage()));
        Suchergebnis ergebnis = suchen.ausfuehren(new TerminSuchen.Anfrage(
                new VerordnungId(suche.verordnung()), Heilmittel.valueOf(suche.heilmittel()), wunsch));
        return Dto.Suchantwort.von(ergebnis);
    }

    @PostMapping("/pruefung")
    Dto.Buchungsantwort pruefen(@RequestBody Dto.Buchung buchung) {
        return Dto.Buchungsantwort.von(null, buchen.pruefen(anfrage(buchung)));
    }

    private static TerminBuchen.Anfrage anfrage(Dto.Buchung buchung) {
        return new TerminBuchen.Anfrage(
                new VerordnungId(buchung.verordnung()),
                Heilmittel.valueOf(buchung.heilmittel()),
                buchung.therapeut(),
                buchung.raum(),
                buchung.beginn());
    }

    @PostMapping
    ResponseEntity<Dto.Buchungsantwort> buchen(@RequestBody Dto.Buchung buchung) {
        TerminBuchen.Anfrage anfrage = anfrage(buchung);
        TerminBuchen.Ergebnis ergebnis = buchung.uebersteuerung() == null
                ? buchen.ausfuehren(anfrage)
                : buchen.ausfuehren(
                        anfrage,
                        new Uebersteuerung(
                                buchung.uebersteuerung().begruendung(),
                                buchung.uebersteuerung().von()));

        Dto.Buchungsantwort antwort =
                Dto.Buchungsantwort.von(ergebnis.termin().map(TerminId::wert).orElse(null), ergebnis.entscheidung());
        HttpStatus status = ergebnis.entscheidung().darfGebuchtWerden() ? HttpStatus.CREATED : HttpStatus.CONFLICT;
        return ResponseEntity.status(status).body(antwort);
    }
}
