package de.aptum.scheduling.domain.port;

import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import java.util.Optional;

/**
 * Was die Domäne von der Ablage einer Verordnung braucht. Nicht mehr.
 *
 * <p>Ein Port nach ADR-001: Das Interface gehört der Domäne, weil sie es
 * braucht; die Implementierung liegt außen, weil sie JPA kennt. Es spricht
 * Domänensprache — {@code lade}, {@code speichere} — und nicht SQL.
 *
 * <p>Kein Mandant in der Signatur. Die Ablage weiß, für wen sie arbeitet, aus
 * dem Kontext der Anfrage (ADR-002). Eine Methode {@code ladeFuer(mandant, id)}
 * wäre die Einladung, den falschen Mandanten hineinzureichen.
 */
public interface VerordnungRepository {

    Optional<VerordnungAkte> lade(VerordnungId id);

    void speichere(VerordnungAkte akte);
}
