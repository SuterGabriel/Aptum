package de.aptum.scheduling.domain.model;

import java.util.Objects;

/**
 * Eine Verordnung samt dem, was darauf schon erbracht ist.
 *
 * <p>Das ist die Einheit, die gespeichert und geladen wird. Die Regeln
 * arbeiten mit den beiden Teilen getrennt — {@link Verordnung} für das, was
 * auf dem Vordruck steht, {@link Behandlungsverlauf} für das, was daraus
 * geworden ist — und die Akte hält sie unter einer Kennung zusammen.
 *
 * @param id         die Kennung
 * @param verordnung der Vordruck
 * @param verlauf    die bisher erbrachten Behandlungen
 */
public record VerordnungAkte(VerordnungId id, Verordnung verordnung, Behandlungsverlauf verlauf) {

    public VerordnungAkte {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(verordnung, "verordnung");
        Objects.requireNonNull(verlauf, "verlauf");
    }

    public static VerordnungAkte neu(Verordnung verordnung) {
        return new VerordnungAkte(VerordnungId.neu(), verordnung, Behandlungsverlauf.leer());
    }

    /** Dieselbe Akte mit einer weiteren erbrachten Behandlung. */
    public VerordnungAkte mitBehandlung(Behandlungstermin termin) {
        var termine = new java.util.ArrayList<>(verlauf.termine());
        termine.add(termin);
        return new VerordnungAkte(id, verordnung, Behandlungsverlauf.aus(termine));
    }
}
