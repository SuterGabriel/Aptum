package de.aptum.scheduling.domain.model;

import java.util.Objects;
import java.util.UUID;

/** Die Kennung eines gebuchten Termins. Siehe {@link VerordnungId} für das Warum. */
public record TerminId(UUID wert) {

    public TerminId {
        Objects.requireNonNull(wert, "wert");
    }

    public static TerminId neu() {
        return new TerminId(UUID.randomUUID());
    }
}
