package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Die ärztliche Verordnung, Vordruck Muster 13.
 *
 * <p>Bewusst klein. Hier stehen genau die Felder, die eine bereits gebaute
 * Regel braucht - im Moment ist das nur die Frist für den Behandlungsbeginn.
 * Das Modell wächst mit den Regeln, nicht mit der Vorstellung davon, was ein
 * Rezept alles enthält. Heilmittel, Menge, Frequenz und Diagnosegruppe kommen,
 * wenn die Regel dazu entsteht, die sie prüft.
 *
 * <p>Fristen der Verordnung rechnen auf Kalendertagen, deshalb
 * {@link LocalDate} und nicht ein Zeitpunkt mit Zone. Das ist kein Detail: Ein
 * Termin hat eine Uhrzeit in Europe/Berlin, eine Frist hat sie nicht.
 *
 * @param ausstellungsdatum  das Datum auf dem Vordruck
 * @param dringlicherBedarf  das ärztliche Kennzeichen, das die Frist verkürzt
 */
public record Verordnung(LocalDate ausstellungsdatum, boolean dringlicherBedarf) {

    public Verordnung {
        Objects.requireNonNull(ausstellungsdatum, "ausstellungsdatum");
    }
}
