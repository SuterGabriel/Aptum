package de.aptum.scheduling.domain.model;

import java.util.Objects;

/**
 * Die bewusste Entscheidung, trotz einer verletzten Regel zu buchen.
 *
 * <p>Sie hat eine Begründung, und die ist Pflicht — nicht als Formalie,
 * sondern weil sie der Text ist, den die Praxis später gegenüber der Kasse
 * vorlegt. Eine Übersteuerung ohne Begründung wäre eine Buchung, die ihre
 * eigene Warnung überschreibt und keine Spur hinterlässt.
 *
 * <p>Wer übersteuert hat, steht dabei. Nicht zur Kontrolle, sondern weil bei
 * einer Absetzung die Frage kommt, wer damals entschieden hat.
 *
 * <p>Grundlage: ADR-009.
 *
 * @param begruendung warum trotzdem gebucht wird, in Klartext
 * @param von         wer die Entscheidung getroffen hat
 */
public record Uebersteuerung(String begruendung, String von) {

    public Uebersteuerung {
        Objects.requireNonNull(begruendung, "begruendung");
        Objects.requireNonNull(von, "von");
        if (begruendung.isBlank()) {
            throw new IllegalArgumentException("Eine Übersteuerung ohne Begründung ist keine.");
        }
        if (von.isBlank()) {
            throw new IllegalArgumentException("Eine Übersteuerung nennt, wer entschieden hat.");
        }
    }
}
