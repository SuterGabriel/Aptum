package de.aptum.scheduling.infrastructure.persistenz;

import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.TerminId;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.port.TerminRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Der Kalender in der Datenbank.
 *
 * <p>Person und Raum liegen als Kürzel in der Tabelle und werden beim Lesen
 * aus den {@link Stammdaten} aufgelöst. Ein Kürzel, das dort nicht mehr
 * existiert, ist ein Datenfehler und wirft — ein Termin ohne Person wäre im
 * Kalender ein Loch, das niemand sieht.
 */
@Repository
@Transactional
class TerminRepositoryAdapter implements TerminRepository {

    private final EntityManager em;
    private final Stammdaten stammdaten;

    TerminRepositoryAdapter(EntityManager em, Stammdaten stammdaten) {
        this.em = em;
        this.stammdaten = stammdaten;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Termin> imZeitraum(Zeitraum zeitraum) {
        return em
                .createQuery(
                        "select t from TerminEntity t where t.behandlungVon < :bis and t.behandlungBis > :von",
                        TerminEntity.class)
                .setParameter("von", zeitraum.von().toOffsetDateTime())
                .setParameter("bis", zeitraum.bis().toOffsetDateTime())
                .getResultList()
                .stream()
                .map(this::zurDomaene)
                .toList();
    }

    @Override
    public void speichere(TerminId id, Termin termin) {
        TerminEntity e = new TerminEntity();
        e.id = id.wert();
        e.therapeut = termin.therapeut().kuerzel();
        e.raum = termin.raum().bezeichnung();
        e.heilmittel = termin.heilmittel().name();
        e.behandlungVon = termin.behandlung().von().toOffsetDateTime();
        e.behandlungBis = termin.behandlung().bis().toOffsetDateTime();
        em.persist(e);
    }

    private Termin zurDomaene(TerminEntity e) {
        Therapeut therapeut = stammdaten
                .therapeut(e.therapeut)
                .orElseThrow(() -> new IllegalStateException("Unbekannte Person im Kalender: " + e.therapeut));
        Raum raum = stammdaten
                .raum(e.raum)
                .orElseThrow(() -> new IllegalStateException("Unbekannter Raum im Kalender: " + e.raum));
        Zeitraum behandlung = new Zeitraum(
                e.behandlungVon.atZoneSameInstant(Zeitraum.PRAXIS), e.behandlungBis.atZoneSameInstant(Zeitraum.PRAXIS));
        return new Termin(therapeut, raum, Heilmittel.valueOf(e.heilmittel), behandlung);
    }
}
