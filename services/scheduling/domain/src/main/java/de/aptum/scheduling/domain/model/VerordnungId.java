package de.aptum.scheduling.domain.model;

import java.util.Objects;
import java.util.UUID;

/**
 * Die Kennung einer gespeicherten Verordnung.
 *
 * <p>Ein eigener Typ statt {@code UUID} überall: Ein Aufruf mit vertauschten
 * Parametern kompiliert dann nicht mehr. Die {@link Verordnung} selbst trägt
 * keine Kennung — die Regeln brauchen keine, und ein Wert, den keine Regel
 * liest, gehört nicht ins Regelmodell.
 */
public record VerordnungId(UUID wert) {

    public VerordnungId {
        Objects.requireNonNull(wert, "wert");
    }

    public static VerordnungId neu() {
        return new VerordnungId(UUID.randomUUID());
    }
}
