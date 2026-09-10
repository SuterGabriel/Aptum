package de.aptum.scheduling.infrastructure.persistenz;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Die Tabellenzeile, nicht die Verordnung.
 *
 * <p>Diese Klasse verlässt den Adapter nie (ADR-001). Sie kennt keine Regel,
 * keine Frequenzspanne, keine Diagnosegruppe als Typ — nur Spalten. Die
 * Übersetzung in die Domäne macht {@link VerordnungRepositoryAdapter}, von
 * Hand, damit eine Spaltenumbenennung kein API-Bruch wird.
 *
 * <p>Keine Spalte {@code mandant_id}: Die füllt die Datenbank aus der
 * Sitzungsvariable, und die Anwendung liest sie nie (ADR-002).
 */
@Entity
@Table(name = "verordnung")
class VerordnungEntity {

    @Id
    UUID id;

    @Column(name = "ausstellungsdatum", nullable = false)
    LocalDate ausstellungsdatum;

    @Column(name = "dringlicher_bedarf", nullable = false)
    boolean dringlicherBedarf;

    @Column(name = "diagnosegruppe", nullable = false)
    String diagnosegruppe;

    @Column(name = "verordnete_einheiten", nullable = false)
    int verordneteEinheiten;

    @Column(name = "frequenz_min", nullable = false)
    int frequenzMin;

    @Column(name = "frequenz_max", nullable = false)
    int frequenzMax;

    @OneToMany(mappedBy = "verordnung", cascade = CascadeType.ALL, orphanRemoval = true)
    List<BehandlungsterminEntity> behandlungen = new ArrayList<>();
}
