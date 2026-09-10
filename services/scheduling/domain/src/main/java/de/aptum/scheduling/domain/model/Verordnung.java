package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.util.Objects;

/**
 * Die ärztliche Verordnung, Vordruck Muster 13.
 *
 * <p>Bewusst klein. Hier stehen genau die Felder, die eine bereits gebaute
 * Regel braucht. Das Modell wächst mit den Regeln, nicht mit der Vorstellung
 * davon, was ein Rezept alles enthält — Diagnosegruppe, Leitsymptomatik und
 * Heilmittel kommen, wenn die Regel entsteht, die sie prüft.
 *
 * <p>Die Therapieform ist kein eigenes Feld: Sie folgt aus der
 * Diagnosegruppe. Es gibt keine Physio-Verordnung mit
 * ergotherapeutischer Gruppe, und ein Feld, das man nicht hat, kann nicht
 * widersprüchlich gefüllt werden.
 *
 * <p>Fristen der Verordnung rechnen auf Kalendertagen, deshalb
 * {@link LocalDate} und nicht ein Zeitpunkt mit Zone. Das ist kein Detail: Ein
 * Termin hat eine Uhrzeit in Europe/Berlin, eine Frist hat sie nicht.
 *
 * @param ausstellungsdatum   das Datum auf dem Vordruck
 * @param dringlicherBedarf   das ärztliche Kennzeichen, das die Frist verkürzt
 * @param diagnosegruppe      der Kurzschlüssel des Heilmittelkatalogs
 * @param verordneteEinheiten die verordneten Behandlungseinheiten
 * @param frequenz            die verordnete Behandlungshäufigkeit, für die Praxis bindend
 */
public record Verordnung(
        LocalDate ausstellungsdatum,
        boolean dringlicherBedarf,
        Diagnosegruppe diagnosegruppe,
        int verordneteEinheiten,
        Frequenz frequenz) {

    public Verordnung {
        Objects.requireNonNull(ausstellungsdatum, "ausstellungsdatum");
        Objects.requireNonNull(diagnosegruppe, "diagnosegruppe");
        Objects.requireNonNull(frequenz, "frequenz");
        if (verordneteEinheiten <= 0) {
            throw new IllegalArgumentException(
                    "Eine Verordnung ohne Behandlungseinheiten gibt es nicht: " + verordneteEinheiten);
        }
    }

    /** Folgt aus der Diagnosegruppe, siehe {@link Diagnosegruppe}. */
    public Therapieform therapieform() {
        return diagnosegruppe.therapieform();
    }
}
