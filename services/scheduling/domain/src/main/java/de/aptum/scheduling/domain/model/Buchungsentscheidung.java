package de.aptum.scheduling.domain.model;

import java.util.Objects;
import java.util.Optional;

/**
 * Was aus einem Prüfbericht folgt: frei, blockiert, oder übersteuert.
 *
 * <p>Das ist die Antwort auf die erste offene Produktfrage: Eine Buchung, die
 * gegen eine Regel verstößt — blockieren oder warnen? Die Entscheidung
 * (ADR-009): <em>blockieren, mit begründungspflichtiger Übersteuerung.</em>
 * Das System verhindert den Fehler im Moment der Buchung; wer ihn trotzdem
 * will, sagt warum, und das wird protokolliert.
 *
 * <p>Warnungen blockieren nicht. Über eine Frequenzabweichung entscheidet ein
 * Mensch nach Rücksprache — die Warnung hängt am Termin, die Buchung geht
 * durch. Eine Übersteuerung braucht es dafür nicht.
 *
 * <p>Eine Übersteuerung ohne verletzte Regel wird nicht gespeichert. Sie
 * hätte nichts übersteuert, und ein Protokoll voller leerer Einträge ist
 * schlechter als keines.
 *
 * @param ausgang       frei, blockiert oder übersteuert
 * @param bericht       der Bericht, aus dem die Entscheidung folgt
 * @param uebersteuerung die Begründung, wenn übersteuert wurde
 */
public record Buchungsentscheidung(Ausgang ausgang, Pruefbericht bericht, Optional<Uebersteuerung> uebersteuerung) {

    public enum Ausgang {
        FREI,
        BLOCKIERT,
        UEBERSTEUERT
    }

    public Buchungsentscheidung {
        Objects.requireNonNull(ausgang, "ausgang");
        Objects.requireNonNull(bericht, "bericht");
        Objects.requireNonNull(uebersteuerung, "uebersteuerung");
    }

    /** Der Regelfall: keine Übersteuerung angeboten. */
    public static Buchungsentscheidung aus(Pruefbericht bericht) {
        return bericht.blockiert()
                ? new Buchungsentscheidung(Ausgang.BLOCKIERT, bericht, Optional.empty())
                : new Buchungsentscheidung(Ausgang.FREI, bericht, Optional.empty());
    }

    /** Jemand hat entschieden, trotzdem zu buchen. */
    public static Buchungsentscheidung mitUebersteuerung(Pruefbericht bericht, Uebersteuerung uebersteuerung) {
        Objects.requireNonNull(uebersteuerung, "uebersteuerung");
        if (!bericht.blockiert()) {
            return aus(bericht);
        }
        return new Buchungsentscheidung(Ausgang.UEBERSTEUERT, bericht, Optional.of(uebersteuerung));
    }

    public boolean darfGebuchtWerden() {
        return ausgang != Ausgang.BLOCKIERT;
    }
}
