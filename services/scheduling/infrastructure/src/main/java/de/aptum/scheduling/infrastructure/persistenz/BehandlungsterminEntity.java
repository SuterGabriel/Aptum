package de.aptum.scheduling.infrastructure.persistenz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.UUID;

/** Eine erbrachte Behandlung auf einer Verordnung. Nur Spalten, siehe {@link VerordnungEntity}. */
@Entity
@Table(name = "behandlungstermin")
class BehandlungsterminEntity {

    @Id
    UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "verordnung_id", nullable = false)
    VerordnungEntity verordnung;

    @Column(name = "datum", nullable = false)
    LocalDate datum;

    @Column(name = "kennzeichen", nullable = false)
    String kennzeichen;
}
