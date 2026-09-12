package de.aptum.scheduling.infrastructure.persistenz;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Frequenz;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Der Adapter zwischen dem Port der Domäne und den Tabellen.
 *
 * <p>Die Übersetzung ist Handarbeit und soll es bleiben: Record hinein,
 * Entity heraus, und zurück. Das kostet Zeilen, die ein Mapping-Werkzeug
 * spart — und bringt, dass eine Spaltenumbenennung hier auffällt und nicht
 * in einem Controller.
 *
 * <p>Kein Mandant in dieser Klasse. Die Sitzungsvariable setzt der
 * Transaktionsmanager, die Spalte füllt die Datenbank, die Policy filtert.
 * Ein {@code lade} für eine fremde Kennung liefert {@code empty}, nicht eine
 * fremde Akte.
 */
@Repository
@Transactional
class VerordnungRepositoryAdapter implements VerordnungRepository {

    private final EntityManager em;

    VerordnungRepositoryAdapter(EntityManager em) {
        this.em = em;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VerordnungAkte> lade(VerordnungId id) {
        return Optional.ofNullable(em.find(VerordnungEntity.class, id.wert())).map(this::zurDomaene);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VerordnungAkte> alle() {
        // Die Policy filtert auf den Mandanten der Sitzung; hier steht kein where.
        return em
                .createQuery(
                        "select v from VerordnungEntity v order by v.ausstellungsdatum, v.id", VerordnungEntity.class)
                .getResultList()
                .stream()
                .map(this::zurDomaene)
                .toList();
    }

    @Override
    public void speichere(VerordnungAkte akte) {
        VerordnungEntity vorhanden = em.find(VerordnungEntity.class, akte.id().wert());
        if (vorhanden != null) {
            zurTabelle(akte, vorhanden);
            return;
        }
        // Erst füllen, dann persistieren. Andersherum schreibt der Flush im
        // Verlauf eine leere Zeile, und die Datenbank meldet das Pflichtfeld.
        VerordnungEntity neu = new VerordnungEntity();
        neu.id = akte.id().wert();
        zurTabelle(akte, neu);
        em.persist(neu);
    }

    private VerordnungAkte zurDomaene(VerordnungEntity e) {
        Verordnung verordnung = new Verordnung(
                e.ausstellungsdatum,
                e.dringlicherBedarf,
                Diagnosegruppe.valueOf(e.diagnosegruppe),
                e.verordneteEinheiten,
                Frequenz.spanne(e.frequenzMin, e.frequenzMax));
        List<Behandlungstermin> termine = e.behandlungen.stream()
                .map((b) -> new Behandlungstermin(b.datum, Unterbrechungskennzeichen.valueOf(b.kennzeichen)))
                .toList();
        return new VerordnungAkte(new VerordnungId(e.id), verordnung, Behandlungsverlauf.aus(termine));
    }

    private void zurTabelle(VerordnungAkte akte, VerordnungEntity e) {
        Verordnung v = akte.verordnung();
        e.ausstellungsdatum = v.ausstellungsdatum();
        e.dringlicherBedarf = v.dringlicherBedarf();
        e.diagnosegruppe = v.diagnosegruppe().name();
        e.verordneteEinheiten = v.verordneteEinheiten();
        e.frequenzMin = v.frequenz().minProWoche();
        e.frequenzMax = v.frequenz().maxProWoche();

        // Der Verlauf wird ersetzt, nicht abgeglichen. Er ist klein, und ein
        // Abgleich hätte mehr Zeilen als die ganze Klasse.
        e.behandlungen.clear();
        em.flush();
        for (Behandlungstermin t : akte.verlauf().termine()) {
            BehandlungsterminEntity b = new BehandlungsterminEntity();
            b.id = UUID.randomUUID();
            b.verordnung = e;
            b.datum = t.datum();
            b.kennzeichen = t.kennzeichen().name();
            e.behandlungen.add(b);
        }
    }
}
