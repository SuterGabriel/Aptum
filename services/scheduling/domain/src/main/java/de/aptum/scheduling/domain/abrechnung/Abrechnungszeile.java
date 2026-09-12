package de.aptum.scheduling.domain.abrechnung;

import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungId;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

/**
 * Eine Verordnung aus Sicht der Abrechnung: was verordnet, was erbracht, und
 * ob das Erbrachte prüffest ist.
 *
 * <p>Der Bericht steht mit in der Zeile. Die Übersicht sagt nicht nur
 * „beanstandet", sie sagt, welche Regel — dieselben Namen und Begründungen
 * wie im Buchungsdialog, weil es dieselben Regeln sind.
 *
 * @param id               die Kennung der Verordnung
 * @param verordnung       der Vordruck
 * @param erbracht         Anzahl erbrachter Behandlungen
 * @param ersteBehandlung  Tag der ersten Behandlung, wenn es eine gab
 * @param letzteBehandlung Tag der letzten Behandlung, wenn es eine gab
 * @param status           die Antwort in einem Wort
 * @param begruendung      die Antwort in einem Satz
 * @param bericht          alle geprüften Regeln; leer, wenn nichts erbracht ist
 */
public record Abrechnungszeile(
        VerordnungId id,
        Verordnung verordnung,
        int erbracht,
        Optional<LocalDate> ersteBehandlung,
        Optional<LocalDate> letzteBehandlung,
        Abrechnungsstatus status,
        String begruendung,
        Pruefbericht bericht) {

    public Abrechnungszeile {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(verordnung, "verordnung");
        Objects.requireNonNull(ersteBehandlung, "ersteBehandlung");
        Objects.requireNonNull(letzteBehandlung, "letzteBehandlung");
        Objects.requireNonNull(status, "status");
        Objects.requireNonNull(begruendung, "begruendung");
        Objects.requireNonNull(bericht, "bericht");
    }

    /** Noch offene Einheiten; nie negativ, auch wenn übersteuert mehr gebucht wurde. */
    public int offen() {
        return Math.max(0, verordnung.verordneteEinheiten() - erbracht);
    }
}
