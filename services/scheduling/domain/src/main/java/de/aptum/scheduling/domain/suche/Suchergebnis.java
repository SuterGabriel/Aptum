package de.aptum.scheduling.domain.suche;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Was die Suche zurückgibt: die Vorschläge — und was sie weggelassen hat,
 * mit Grund.
 *
 * <p>Der zweite Teil ist der, an dem das Produkt seine Fachlogik zeigt. Das
 * Wireframe verlangt einen Hinweis wie „zwei Termine wurden weggelassen, weil
 * die Verordnung dann bereits verfallen wäre". Ohne die Zählung der
 * Ausschlüsse sähe eine leere Trefferliste aus wie ein leerer Kalender.
 *
 * @param vorschlaege   die Kandidaten, die alle Regeln bestanden haben
 * @param ausgeschlossen wie viele Kandidaten je Regel ausgeschieden sind
 * @param geprueft      wie viele Kandidaten insgesamt geprüft wurden
 */
public record Suchergebnis(List<Vorschlag> vorschlaege, Map<String, Integer> ausgeschlossen, int geprueft) {

    public Suchergebnis {
        Objects.requireNonNull(vorschlaege, "vorschlaege");
        Objects.requireNonNull(ausgeschlossen, "ausgeschlossen");
        vorschlaege = List.copyOf(vorschlaege);
        ausgeschlossen = Map.copyOf(ausgeschlossen);
    }

    public boolean istLeer() {
        return vorschlaege.isEmpty();
    }

    /** Für die Statuszeile: „12 Vorschläge, 40 ausgeschlossen: Qualifikation 20, Raum frei 20". */
    public String zusammenfassung() {
        StringBuilder s = new StringBuilder();
        s.append(vorschlaege.size()).append(vorschlaege.size() == 1 ? " Vorschlag" : " Vorschläge");
        if (!ausgeschlossen.isEmpty()) {
            int summe =
                    ausgeschlossen.values().stream().mapToInt(Integer::intValue).sum();
            s.append(", ").append(summe).append(" ausgeschlossen: ");
            s.append(String.join(
                    ", ",
                    ausgeschlossen.entrySet().stream()
                            .map((e) -> e.getKey() + " " + e.getValue())
                            .toList()));
        }
        return s.toString();
    }
}
