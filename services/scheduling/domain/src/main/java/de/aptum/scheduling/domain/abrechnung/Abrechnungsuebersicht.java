package de.aptum.scheduling.domain.abrechnung;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.regel.Regelwerk;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Die Abrechnungsübersicht: je Verordnung eine Zeile, und die Antwort auf die
 * Frage der Abrechnung aus {@code PRODUKT.md} — welche Einheiten sind erbracht
 * und absetzungssicher?
 *
 * <p>Ein Lesemodell, keine neue Regel. Ob das Erbrachte prüffest ist,
 * entscheidet das {@link Regelwerk} mit denselben Regelklassen, die jede
 * Buchung geprüft haben; hier laufen sie noch einmal über den Verlauf, wie er
 * jetzt ist. Stünde hier ein eigenes {@code if} über Fristen, gäbe es zwei
 * Meinungen darüber, wann eine Verordnung verfallen ist — und die Abrechnung
 * hätte die falsche.
 *
 * <p>Kein Stichtag von außen: Geprüft wird die Lage am Tag der letzten
 * Behandlung. Ob die erbrachten Einheiten damals regelkonform waren, ändert
 * sich nicht dadurch, dass Zeit vergeht.
 */
public final class Abrechnungsuebersicht {

    private final Regelwerk regelwerk;

    public Abrechnungsuebersicht(Regelwerk regelwerk) {
        this.regelwerk = Objects.requireNonNull(regelwerk, "regelwerk");
    }

    /** Eine Zeile je Akte, nach Ausstellungsdatum; sortieren und filtern ist Sache der Oberfläche. */
    public List<Abrechnungszeile> berechne(List<VerordnungAkte> akten) {
        return akten.stream()
                .map(this::zeile)
                .sorted(Comparator.comparing(
                                (Abrechnungszeile z) -> z.verordnung().ausstellungsdatum())
                        .thenComparing((z) -> z.id().wert()))
                .toList();
    }

    private Abrechnungszeile zeile(VerordnungAkte akte) {
        Behandlungsverlauf verlauf = akte.verlauf();
        if (verlauf.istLeer()) {
            return new Abrechnungszeile(
                    akte.id(),
                    akte.verordnung(),
                    0,
                    verlauf.ersterBehandlungstag(),
                    verlauf.letzterBehandlungstag(),
                    Abrechnungsstatus.NICHT_BEGONNEN,
                    "Noch keine Behandlung erbracht, nichts abzurechnen.",
                    new Pruefbericht(List.of()));
        }

        Pruefbericht bericht = regelwerk.pruefeErbrachtes(akte.verordnung(), verlauf);
        Abrechnungsstatus status = bericht.blockiert() ? Abrechnungsstatus.BEANSTANDET : Abrechnungsstatus.PRUEFFEST;
        String begruendung = bericht.ersterVerstoss()
                .map((e) -> e.regel() + ": " + e.begruendung())
                .orElse("%d Regeln geprüft, keine verletzt."
                        .formatted(bericht.ergebnisse().size()));
        return new Abrechnungszeile(
                akte.id(),
                akte.verordnung(),
                verlauf.anzahlBehandlungen(),
                verlauf.ersterBehandlungstag(),
                verlauf.letzterBehandlungstag(),
                status,
                begruendung,
                bericht);
    }
}
