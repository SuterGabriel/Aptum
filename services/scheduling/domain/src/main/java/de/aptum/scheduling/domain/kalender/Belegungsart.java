package de.aptum.scheduling.domain.kalender;

/**
 * Was eine Zelle im Wochengitter zeigt, wenn sie nicht frei ist.
 *
 * <p>Fünf Arten, weil das Gitter fünf Dinge unterscheiden muss, die sich
 * fachlich unterscheiden — nicht, weil fünf Farben schön aussehen. Frei ist
 * keine Art: Frei ist, wo keine Belegung liegt.
 */
public enum Belegungsart {
    /** Die Behandlung selbst, am Patienten. */
    BELEGT,
    /** Vor- und Nachbereitung. Gehört zum Termin, ist keine Behandlung. */
    RUESTZEIT,
    /** Der Patient ruht im Raum. Bindet den Raum, nicht die Therapeutin. */
    NACHRUHE,
    /** Urlaub, Fortbildung, Krankheit — der ganze Tag. */
    ABWESENHEIT,
    /** Außerhalb der Arbeitszeit oder kein Dienst an diesem Tag. */
    GESPERRT
}
