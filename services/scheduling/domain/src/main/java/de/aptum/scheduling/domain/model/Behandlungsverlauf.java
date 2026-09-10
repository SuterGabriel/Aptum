package de.aptum.scheduling.domain.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Die bisher erbrachten Behandlungen einer Verordnung, in zeitlicher Ordnung.
 *
 * <p>Der Grund für diesen Typ: Alle drei Unterbrechungsregeln brauchen
 * dieselbe Vorarbeit — Termine sortieren, Lücken dazwischen bilden. Stünde
 * das in jeder Regel einzeln, wäre es dreimal dieselbe Schleife, und beim
 * vierten Mal würde sie leicht anders gezählt.
 *
 * <p>Die Liste wird beim Anlegen kopiert und sortiert. Wer unsortiert
 * hineingibt, bekommt trotzdem das richtige Ergebnis; das ist billiger als
 * ein Fehler, der nur bei nachgetragenen Terminen auftritt.
 */
public final class Behandlungsverlauf {

    private final List<Behandlungstermin> termine;

    private Behandlungsverlauf(List<Behandlungstermin> termine) {
        this.termine = termine;
    }

    public static Behandlungsverlauf aus(List<Behandlungstermin> termine) {
        Objects.requireNonNull(termine, "termine");
        List<Behandlungstermin> sortiert = new ArrayList<>(termine);
        sortiert.sort(Comparator.comparing(Behandlungstermin::datum));
        return new Behandlungsverlauf(List.copyOf(sortiert));
    }

    public static Behandlungsverlauf leer() {
        return new Behandlungsverlauf(List.of());
    }

    public boolean istLeer() {
        return termine.isEmpty();
    }

    public int anzahlBehandlungen() {
        return termine.size();
    }

    public List<Behandlungstermin> termine() {
        return termine;
    }

    /**
     * Der erste Behandlungstag. Bezugspunkt für die absolute Gültigkeitsdauer
     * der Verordnung, nicht das Ausstellungsdatum.
     */
    public Optional<LocalDate> ersterBehandlungstag() {
        return termine.isEmpty() ? Optional.empty() : Optional.of(termine.get(0).datum());
    }

    public Optional<LocalDate> letzterBehandlungstag() {
        return termine.isEmpty()
                ? Optional.empty()
                : Optional.of(termine.get(termine.size() - 1).datum());
    }

    /**
     * Die Pausen zwischen je zwei aufeinanderfolgenden Behandlungen.
     *
     * <p>Bei weniger als zwei Terminen gibt es keine Unterbrechung — eine
     * einzelne Behandlung ist keine Reihe mit Lücke.
     */
    public List<Unterbrechung> unterbrechungen() {
        List<Unterbrechung> gefunden = new ArrayList<>();
        for (int i = 1; i < termine.size(); i++) {
            Behandlungstermin davor = termine.get(i - 1);
            Behandlungstermin danach = termine.get(i);
            gefunden.add(new Unterbrechung(davor.datum(), danach.datum(), danach.kennzeichen()));
        }
        return List.copyOf(gefunden);
    }
}
