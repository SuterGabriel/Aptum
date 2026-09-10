package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;

/**
 * Stehen mehr Einheiten auf dem Rezept, als die Diagnosegruppe zulässt?
 *
 * <p>Die Regel in zwei Sätzen: Der Heilmittelkatalog gibt je Diagnosegruppe
 * eine Höchstmenge je Verordnung vor. Was darüber hinausgeht, gehört auf ein
 * weiteres Rezept, nicht auf dieses.
 *
 * <p>Diese Regel greift zu einem anderen Zeitpunkt als die übrigen: nicht beim
 * Buchen, sondern beim Erfassen der Verordnung. Sie ist damit die Stelle, an
 * der ein Vorschlag des AI-Layers auf dieselbe deterministische Prüfung trifft
 * wie eine Eingabe von Hand — ein aus einem Formularfoto extrahiertes „16"
 * fällt hier auf, wenn die Gruppe nur 10 zulässt.
 *
 * <p><strong>Was diese Regel bewusst nicht prüft: die orientierende
 * Behandlungsmenge.</strong> Sie sieht der Höchstmenge zum Verwechseln
 * ähnlich, ist aber ausdrücklich ein Richtwert und <em>keine</em> verbindliche
 * Obergrenze; weitere Verordnungen im selben Verordnungsfall sind möglich
 * ({@code @fundstelle HM-MENGE-05}). Wer sie als Grenze auswertet, baut eine
 * Regel, die es nicht gibt, und lehnt Verordnungen ab, die zulässig sind.
 *
 * <p><strong>Zwei Fälle, in denen die Höchstmenge nicht bindet</strong> und die
 * dieses Modell noch nicht kennt: der langfristige Heilmittelbedarf
 * ({@code @fundstelle HM-MENGE-07}) und die Blankoverordnung
 * ({@code @fundstelle HM-MENGE-08}). Beide sind im Katalog belegt und beide
 * brauchen ein Kennzeichen auf der Verordnung, das hier noch nicht modelliert
 * ist. Solange es fehlt, prüft diese Regel den Regelfall — und diese Zeile
 * hält fest, dass das eine Lücke ist und keine Aussage.
 *
 * <p>Fundstellen der Höchstmengen: {@code @fundstelle HM-MENGE-01} für die
 * Physiotherapie, {@code @fundstelle HM-MENGE-02} für die Ergotherapie. Die
 * Werte liegen je Diagnosegruppe.
 */
public final class HoechstmengeGrenze {

    private static final String NAME = "Höchstmenge";

    public Pruefergebnis pruefe(Verordnung verordnung) {
        int hoechstmenge = verordnung.diagnosegruppe().hoechstmengeJeVerordnung();
        int verordnet = verordnung.verordneteEinheiten();

        String lage = "%d Einheiten verordnet, Diagnosegruppe %s lässt %d je Verordnung zu"
                .formatted(verordnet, verordnung.diagnosegruppe(), hoechstmenge);

        if (verordnet > hoechstmenge) {
            return Pruefergebnis.verletzt(
                    NAME, lage + ". Die überzähligen Einheiten gehören auf eine weitere Verordnung.");
        }
        return Pruefergebnis.erfuellt(NAME, lage + ".");
    }
}
