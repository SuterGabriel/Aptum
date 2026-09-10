package de.aptum.scheduling.infrastructure.persistenz;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Ein gebuchter Termin als Tabellenzeile.
 *
 * <p>Person und Raum als Kürzel, das Heilmittel als Name. Zeiten als
 * {@code timestamptz}: Postgres speichert den Zeitpunkt, nicht die Zone — die
 * Zone der Praxis legt der Adapter beim Lesen wieder an.
 */
@Entity
@Table(name = "termin")
class TerminEntity {

    @Id
    UUID id;

    @Column(name = "therapeut", nullable = false)
    String therapeut;

    @Column(name = "raum", nullable = false)
    String raum;

    @Column(name = "heilmittel", nullable = false)
    String heilmittel;

    @Column(name = "behandlung_von", nullable = false)
    OffsetDateTime behandlungVon;

    @Column(name = "behandlung_bis", nullable = false)
    OffsetDateTime behandlungBis;
}
