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
 * <p>Die Therapieform ist kein Etikett: An ihr hängt, welche
 * Unterbrechungsregel überhaupt gilt. Die verordneten Einheiten entscheiden
 * bei der Physiotherapie über drei oder sechs Monate Gültigkeit.
 *
 * <p>Fristen der Verordnung rechnen auf Kalendertagen, deshalb
 * {@link LocalDate} und nicht ein Zeitpunkt mit Zone. Das ist kein Detail: Ein
 * Termin hat eine Uhrzeit in Europe/Berlin, eine Frist hat sie nicht.
 *
 * @param ausstellungsdatum   das Datum auf dem Vordruck
 * @param dringlicherBedarf   das ärztliche Kennzeichen, das die Frist verkürzt
 * @param therapieform        Physio- oder Ergotherapie
 * @param verordneteEinheiten die verordneten Behandlungseinheiten
 */
public record Verordnung(
        LocalDate ausstellungsdatum,
        boolean dringlicherBedarf,
        Therapieform therapieform,
        int verordneteEinheiten) {

    public Verordnung {
        Objects.requireNonNull(ausstellungsdatum, "ausstellungsdatum");
        Objects.requireNonNull(therapieform, "therapieform");
        if (verordneteEinheiten <= 0) {
            throw new IllegalArgumentException(
                    "Eine Verordnung ohne Behandlungseinheiten gibt es nicht: "
                            + verordneteEinheiten);
        }
    }
}
