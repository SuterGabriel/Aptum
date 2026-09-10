package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Buchungsentscheidung;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.TerminId;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Uebersteuerung;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.port.TerminRepository;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.regel.Regelwerk;
import java.time.LocalDate;
import java.time.ZonedDateTime;
import java.util.Optional;

/**
 * Einen Termin buchen — durch dasselbe Regelwerk wie die Suche.
 *
 * <p>Das ist Regel 3 aus {@code CLAUDE.md} an der Stelle, an der sie zählt:
 * Ein Vorschlag aus der Suche, eine Buchung von Hand und ein Vorschlag eines
 * Sprachmodells landen alle hier, und hier fragt niemand, woher der Termin
 * kommt. Er wird geprüft. Ist eine Regel verletzt, blockiert die Buchung;
 * wer trotzdem buchen will, übersteuert mit Begründung (ADR-009).
 *
 * <p>Gebucht wird beides: der Termin im Kalender und die Behandlung auf der
 * Verordnung. Ohne das zweite wüsste die nächste Suche nichts vom
 * Restkontingent.
 */
public final class TerminBuchen {

    private final VerordnungRepository verordnungen;
    private final TerminRepository termine;
    private final Stammdaten stammdaten;
    private final Regelwerk regelwerk;

    public TerminBuchen(
            VerordnungRepository verordnungen, TerminRepository termine, Stammdaten stammdaten, Regelwerk regelwerk) {
        this.verordnungen = verordnungen;
        this.termine = termine;
        this.stammdaten = stammdaten;
        this.regelwerk = regelwerk;
    }

    public record Anfrage(
            VerordnungId verordnung, Heilmittel heilmittel, String therapeut, String raum, ZonedDateTime beginn) {}

    /** Was aus der Buchung wurde. Die Kennung gibt es nur, wenn gebucht wurde. */
    public record Ergebnis(Buchungsentscheidung entscheidung, Optional<TerminId> termin) {}

    public Ergebnis ausfuehren(Anfrage anfrage) {
        return buche(anfrage, null);
    }

    public Ergebnis ausfuehren(Anfrage anfrage, Uebersteuerung uebersteuerung) {
        return buche(anfrage, uebersteuerung);
    }

    private Ergebnis buche(Anfrage anfrage, Uebersteuerung uebersteuerung) {
        VerordnungAkte akte = verordnungen
                .lade(anfrage.verordnung())
                .orElseThrow(() -> new UnbekannteVerordnung(anfrage.verordnung()));
        Therapeut therapeut = stammdaten
                .therapeut(anfrage.therapeut())
                .orElseThrow(() -> new IllegalArgumentException("Unbekannte Person: " + anfrage.therapeut()));
        Dienstplan dienstplan = stammdaten.therapeuten().get(therapeut);
        Raum raum = stammdaten
                .raum(anfrage.raum())
                .orElseThrow(() -> new IllegalArgumentException("Unbekannter Raum: " + anfrage.raum()));

        Zeitraum behandlung = Zeitraum.ab(anfrage.beginn(), anfrage.heilmittel().regeldauer());
        LocalDate tag = behandlung.von().withZoneSameInstant(Zeitraum.PRAXIS).toLocalDate();
        Zeitraum tagesfenster =
                new Zeitraum(tag.atStartOfDay(Zeitraum.PRAXIS), tag.plusDays(1).atStartOfDay(Zeitraum.PRAXIS));

        Regelwerk.Kontext kontext = new Regelwerk.Kontext(
                akte.verordnung(),
                akte.verlauf(),
                anfrage.heilmittel(),
                termine.imZeitraum(tagesfenster),
                stammdaten.einstellung());
        Regelwerk.Kandidat kandidat = new Regelwerk.Kandidat(behandlung, therapeut, dienstplan, raum);
        Pruefbericht bericht = regelwerk.pruefe(kontext, kandidat);

        Buchungsentscheidung entscheidung = uebersteuerung == null
                ? Buchungsentscheidung.aus(bericht)
                : Buchungsentscheidung.mitUebersteuerung(bericht, uebersteuerung);
        if (!entscheidung.darfGebuchtWerden()) {
            return new Ergebnis(entscheidung, Optional.empty());
        }

        TerminId id = TerminId.neu();
        termine.speichere(id, new Termin(therapeut, raum, anfrage.heilmittel(), behandlung));
        verordnungen.speichere(akte.mitBehandlung(Behandlungstermin.an(tag)));
        return new Ergebnis(entscheidung, Optional.of(id));
    }
}
