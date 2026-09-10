package de.aptum.scheduling.domain.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Ein Behandlungsraum der Praxis.
 *
 * <p>Die Fläche steht in ganzen Quadratmetern. Die Zulassungsvoraussetzungen
 * geben Mindestflächen in dieser Genauigkeit an, und eine Praxis, die auf den
 * Quadratzentimeter an eine Grenze stößt, hat ein anderes Problem als eines der
 * Terminplanung.
 *
 * <p>Die Anzahl der Geräte ist ein eigenes Feld, weil die Anforderung an den
 * Gerätebereich zweiteilig ist: eine Grundfläche und ein Zuschlag je weiterem
 * Gerät. Ohne die Zahl lässt sich die zweite Hälfte nicht prüfen.
 *
 * @param bezeichnung   der Name, unter dem der Raum im Kalender steht
 * @param flaecheQm     die Therapiefläche in ganzen Quadratmetern
 * @param geraeteAnzahl die Anzahl der Trainingsgeräte im Raum
 * @param eignungen     wofür der Raum ausgestattet ist
 */
public record Raum(String bezeichnung, int flaecheQm, int geraeteAnzahl, Set<Raumanforderung> eignungen) {

    public Raum {
        Objects.requireNonNull(bezeichnung, "bezeichnung");
        Objects.requireNonNull(eignungen, "eignungen");
        if (flaecheQm <= 0) {
            throw new IllegalArgumentException("Ein Raum ohne Fläche ist kein Raum: " + flaecheQm);
        }
        if (geraeteAnzahl < 0) {
            throw new IllegalArgumentException("Negative Geräteanzahl: " + geraeteAnzahl);
        }
        eignungen = Set.copyOf(eignungen);
    }

    /** Ein gewöhnlicher Behandlungsraum ohne besondere Ausstattung. */
    public static Raum behandlungsraum(String bezeichnung, int flaecheQm) {
        return new Raum(bezeichnung, flaecheQm, 0, EnumSet.of(Raumanforderung.GRUNDAUSSTATTUNG));
    }

    public boolean istGeeignetFuer(Raumanforderung anforderung) {
        return eignungen.contains(anforderung);
    }
}
