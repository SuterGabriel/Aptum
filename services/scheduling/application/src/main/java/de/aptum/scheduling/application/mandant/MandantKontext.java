package de.aptum.scheduling.application.mandant;

/**
 * Woher die Anwendung weiß, für welche Praxis sie gerade arbeitet.
 *
 * <p>Ein Port. Die Anwendungsschicht fragt, die Infrastruktur antwortet — aus
 * dem Sicherheitskontext der Anfrage, später aus dem Token, heute aus einem
 * Header, der als Platzhalter benannt ist.
 *
 * <p>Die Antwort setzt die Infrastruktur je Transaktion als Sitzungsvariable
 * in der Datenbank. Row Level Security filtert danach jede Abfrage; ohne
 * gesetzten Mandanten liefert sie null Zeilen. Das ist der Punkt von ADR-002:
 * Es gibt genau eine Stelle, an der der Mandant in die Datenbank gelangt,
 * und die ist nicht in jedem Service.
 */
public interface MandantKontext {

    /** Der Mandant der laufenden Anfrage. Ohne Anfrage gibt es keinen — dann eine Ausnahme, kein null. */
    MandantId aktuell();
}
