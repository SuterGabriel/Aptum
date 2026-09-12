package de.aptum.scheduling.domain.abrechnung;

/**
 * Was die Abrechnung über eine Verordnung wissen will, in drei Worten.
 *
 * <p>Drei Zustände, nicht mehr: Eine Verordnung ohne Behandlung hat nichts
 * abzurechnen; eine mit Behandlungen ist entweder prüffest oder nicht. Ein
 * vierter Zustand wie „teilweise" wäre eine Zahl, und die steht in der Zeile.
 */
public enum Abrechnungsstatus {
    /** Noch keine Behandlung erbracht — nichts abzurechnen, nichts zu beanstanden. */
    NICHT_BEGONNEN,
    /** Alle Verordnungsregeln über das Erbrachte erfüllt: Die Einheiten sind absetzungssicher. */
    PRUEFFEST,
    /** Mindestens eine Regel ist verletzt. Die Zeile nennt die erste. */
    BEANSTANDET
}
