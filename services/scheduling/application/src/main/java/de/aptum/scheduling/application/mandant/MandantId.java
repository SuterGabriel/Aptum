package de.aptum.scheduling.application.mandant;

import java.util.Objects;

/**
 * Die Kennung einer Praxis.
 *
 * <p>Sie liegt in der Anwendungsschicht, nicht im Domänenkern — mit Absicht.
 * Eine Verordnung weiß nicht, welcher Praxis sie gehört; eine Frist ist eine
 * Frist. Die Persistenz weiß es, und sie erfährt es aus dem Sicherheitskontext
 * der Anfrage, nicht aus einem Feld am Objekt (ADR-002).
 *
 * <p>Ein eigener Typ statt eines Strings, damit die Kennung nicht versehentlich
 * in eine Antwort gerät: Ein Serializer, der {@code String} kennt, kennt
 * {@code MandantId} nicht. Das ist Regel 4 aus {@code DATENSCHUTZ.md} als
 * Typ.
 *
 * @param wert die Kennung, wie sie in der Spalte {@code mandant_id} steht
 */
public record MandantId(String wert) {

    public MandantId {
        Objects.requireNonNull(wert, "wert");
        if (wert.isBlank()) {
            throw new IllegalArgumentException("Eine Mandanten-ID ist nicht leer.");
        }
    }
}
