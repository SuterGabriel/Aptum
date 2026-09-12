package de.aptum.scheduling.infrastructure.rest;

import de.aptum.scheduling.application.anwendungsfall.WocheAnzeigen;
import de.aptum.scheduling.domain.abrechnung.Abrechnungsstatus;
import de.aptum.scheduling.domain.abrechnung.Abrechnungszeile;
import de.aptum.scheduling.domain.kalender.Wochenansicht;
import de.aptum.scheduling.domain.model.Buchungsentscheidung;
import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.suche.Suchergebnis;
import de.aptum.scheduling.domain.suche.Vorschlag;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Was über die Schnittstelle geht — und nur das.
 *
 * <p>Eigene Typen statt der Domänen-Records: Die Domäne darf sich ändern,
 * ohne dass die Schnittstelle bricht, und die Schnittstelle zeigt nur, was
 * ein Aufrufer sehen soll. Keine Mandanten-ID, in keiner Antwort
 * ({@code DATENSCHUTZ.md}, Regel 4). Keine Entity ({@code ADR-001}).
 */
final class Dto {

    private Dto() {}

    record VerordnungAnlage(
            LocalDate ausstellungsdatum,
            boolean dringlicherBedarf,
            String diagnosegruppe,
            int verordneteEinheiten,
            int frequenzMin,
            int frequenzMax) {}

    record VerordnungAngelegt(UUID id) {}

    record Suche(
            UUID verordnung,
            String heilmittel,
            LocalDate von,
            LocalDate bis,
            LocalTime fruehestens,
            LocalTime spaetestens,
            List<DayOfWeek> wochentage) {}

    record Regel(String regel, String ausgang, String begruendung) {
        static Regel von(Pruefergebnis e) {
            return new Regel(e.regel(), e.ausgang().name(), e.begruendung());
        }

        static List<Regel> alle(Pruefbericht bericht) {
            return bericht.ergebnisse().stream().map(Regel::von).toList();
        }
    }

    record Terminvorschlag(
            ZonedDateTime beginn, ZonedDateTime ende, String therapeut, String raum, List<Regel> warnungen) {
        static Terminvorschlag von(Vorschlag v) {
            return new Terminvorschlag(
                    v.behandlung().von().withZoneSameInstant(Zeitraum.PRAXIS),
                    v.behandlung().bis().withZoneSameInstant(Zeitraum.PRAXIS),
                    v.therapeut().kuerzel(),
                    v.raum().bezeichnung(),
                    v.bericht().warnungen().stream().map(Regel::von).toList());
        }
    }

    record Suchantwort(
            String zusammenfassung,
            List<Terminvorschlag> vorschlaege,
            Map<String, Integer> ausgeschlossen,
            int geprueft) {
        static Suchantwort von(Suchergebnis e) {
            return new Suchantwort(
                    e.zusammenfassung(),
                    e.vorschlaege().stream().map(Terminvorschlag::von).toList(),
                    e.ausgeschlossen(),
                    e.geprueft());
        }
    }

    record Uebersteuerung(String begruendung, String von) {}

    /** Eine Verordnung aus Sicht der Abrechnung. Die Regeln stehen dabei, damit die Tabelle sie aufklappen kann. */
    record Abrechnungsposten(
            UUID verordnung,
            LocalDate ausstellungsdatum,
            String diagnosegruppe,
            String therapieform,
            int verordnet,
            int erbracht,
            int offen,
            LocalDate ersteBehandlung,
            LocalDate letzteBehandlung,
            String status,
            String begruendung,
            List<Regel> regeln) {
        static Abrechnungsposten von(Abrechnungszeile z) {
            return new Abrechnungsposten(
                    z.id().wert(),
                    z.verordnung().ausstellungsdatum(),
                    z.verordnung().diagnosegruppe().name(),
                    z.verordnung().therapieform().name(),
                    z.verordnung().verordneteEinheiten(),
                    z.erbracht(),
                    z.offen(),
                    z.ersteBehandlung().orElse(null),
                    z.letzteBehandlung().orElse(null),
                    z.status().name(),
                    z.begruendung(),
                    Regel.alle(z.bericht()));
        }
    }

    /** Die Zeilen und die Summen darüber — dieselben Zahlen, die die Tabelle unten zeigt. */
    record Abrechnungsuebersicht(List<Abrechnungsposten> posten, int erbracht, int prueffest, int beanstandet) {
        static Abrechnungsuebersicht von(List<Abrechnungszeile> zeilen) {
            return new Abrechnungsuebersicht(
                    zeilen.stream().map(Abrechnungsposten::von).toList(),
                    zeilen.stream().mapToInt(Abrechnungszeile::erbracht).sum(),
                    (int) zeilen.stream()
                            .filter((z) -> z.status() == Abrechnungsstatus.PRUEFFEST)
                            .count(),
                    (int) zeilen.stream()
                            .filter((z) -> z.status() == Abrechnungsstatus.BEANSTANDET)
                            .count());
        }
    }

    record Buchung(
            UUID verordnung,
            String heilmittel,
            String therapeut,
            String raum,
            ZonedDateTime beginn,
            Uebersteuerung uebersteuerung) {}

    record Buchungsantwort(UUID termin, String ausgang, List<Regel> regeln) {
        static Buchungsantwort von(UUID termin, Buchungsentscheidung e) {
            return new Buchungsantwort(termin, e.ausgang().name(), Regel.alle(e.bericht()));
        }
    }

    record Fehler(String fehler) {}

    record Belegung(String art, ZonedDateTime von, ZonedDateTime bis, String text, String raum) {
        static Belegung von(de.aptum.scheduling.domain.kalender.Belegung b) {
            return new Belegung(
                    b.art().name(),
                    b.zeitraum().von().withZoneSameInstant(Zeitraum.PRAXIS),
                    b.zeitraum().bis().withZoneSameInstant(Zeitraum.PRAXIS),
                    b.text(),
                    b.raum().orElse(null));
        }
    }

    record Spalte(String therapeut, List<Belegung> belegungen) {
        static Spalte von(Wochenansicht.Spalte s) {
            return new Spalte(
                    s.therapeut().kuerzel(),
                    s.belegungen().stream().map(Belegung::von).toList());
        }
    }

    /** Rüstzeit und Nachruhe in Minuten, damit das Gitter seine Legende beschriften kann. */
    record Woche(
            LocalDate montag,
            LocalTime tagesbeginn,
            LocalTime tagesende,
            int ruestzeitMinuten,
            int nachruheMinuten,
            List<Spalte> spalten) {
        static Woche von(WocheAnzeigen.Woche w) {
            return new Woche(
                    w.montag(),
                    w.tagesbeginn(),
                    w.tagesende(),
                    (int) w.einstellung().ruestzeit().toMinutes(),
                    (int) w.einstellung().nachruhe().toMinutes(),
                    w.spalten().stream().map(Spalte::von).toList());
        }
    }
}
