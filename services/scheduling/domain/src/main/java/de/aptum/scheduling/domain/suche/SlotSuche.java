package de.aptum.scheduling.domain.suche;

import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.regel.Regelwerk;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Die Schnittmenge über vier Dimensionen.
 *
 * <p>Ein freier Termin ist keine Lücke im Kalender. Er ist ein Zeitpunkt, an
 * dem die Verordnung noch gilt, die Person darf und kann, der Raum passt und
 * frei ist, und der Patient will. Diese Klasse schreitet die Kandidaten ab
 * und fragt für jeden das {@link Regelwerk}. Sie entscheidet nichts selbst.
 *
 * <p>Bewusst einfach: Tage, Beginnzeiten im Raster, Personen, Räume — vier
 * Schleifen. Die Verordnungsregeln hängen nur am Tag und werden je Tag einmal
 * geprüft; alles andere je Kandidat. Für eine Praxis mit einer Handvoll
 * Personen und Räumen reicht das, und es ist in zwei Sätzen erklärbar.
 *
 * <p>Was ausgeschlossen wird, wird gezählt — je Regel, die den Ausschlag gab.
 * Eine leere Trefferliste ohne diese Zählung sähe aus wie ein leerer Kalender;
 * mit ihr steht da, dass zwanzig Kandidaten an der Qualifikation gescheitert
 * sind und nicht an der Zeit.
 */
public final class SlotSuche {

    private final Regelwerk regelwerk;

    public SlotSuche(Regelwerk regelwerk) {
        this.regelwerk = regelwerk;
    }

    public Suchergebnis suche(Suchanfrage anfrage) {
        Regelwerk.Kontext kontext = new Regelwerk.Kontext(
                anfrage.verordnung(), anfrage.verlauf(), anfrage.heilmittel(),
                anfrage.bestehende(), anfrage.einstellung());
        Duration dauer = anfrage.heilmittel().regeldauer();

        List<Vorschlag> vorschlaege = new ArrayList<>();
        Map<String, Integer> ausgeschlossen = new LinkedHashMap<>();
        int geprueft = 0;

        for (LocalDate tag = anfrage.wunsch().von(); !tag.isAfter(anfrage.wunsch().bis()); tag = tag.plusDays(1)) {
            if (!anfrage.wunsch().erlaubt(tag)) {
                continue;
            }

            Pruefbericht verordnung = regelwerk.pruefeVerordnung(kontext, tag);
            if (verordnung.blockiert()) {
                // Ein ganzer Tag scheidet aus, ohne dass Person oder Raum
                // gefragt werden. Gezählt wird trotzdem je Kandidat, den es
                // gegeben hätte - sonst wiegt ein verfallener Tag weniger als
                // ein belegter Raum.
                int kandidaten = beginnzeiten(anfrage, tag, dauer).size()
                        * anfrage.therapeuten().size() * anfrage.raeume().size();
                geprueft += kandidaten;
                zaehle(ausgeschlossen, verordnung, kandidaten);
                continue;
            }

            for (ZonedDateTime beginn : beginnzeiten(anfrage, tag, dauer)) {
                Zeitraum behandlung = Zeitraum.ab(beginn, dauer);
                for (Map.Entry<Therapeut, Dienstplan> person : anfrage.therapeuten().entrySet()) {
                    for (Raum raum : anfrage.raeume()) {
                        geprueft++;
                        Regelwerk.Kandidat kandidat =
                                new Regelwerk.Kandidat(behandlung, person.getKey(), person.getValue(), raum);
                        Pruefbericht ressourcen = regelwerk.pruefeRessourcen(kontext, kandidat);

                        if (ressourcen.blockiert()) {
                            zaehle(ausgeschlossen, ressourcen, 1);
                            continue;
                        }
                        List<Pruefergebnis> alle = new ArrayList<>(verordnung.ergebnisse());
                        alle.addAll(ressourcen.ergebnisse());
                        vorschlaege.add(new Vorschlag(behandlung, person.getKey(), raum, new Pruefbericht(alle)));
                    }
                }
            }
        }

        return new Suchergebnis(vorschlaege, ausgeschlossen, geprueft);
    }

    /** Alle Beginnzeiten im Raster, bei denen die Behandlung noch ins Tagesfenster passt. */
    private static List<ZonedDateTime> beginnzeiten(Suchanfrage anfrage, LocalDate tag, Duration dauer) {
        List<ZonedDateTime> zeiten = new ArrayList<>();
        LocalTime fruehestens = anfrage.wunsch().fruehestens();
        LocalTime spaetestens = anfrage.wunsch().spaetestens();
        for (LocalTime t = fruehestens; !t.plus(dauer).isAfter(spaetestens) && t.plus(dauer).isAfter(t); t = t.plus(anfrage.raster())) {
            zeiten.add(tag.atTime(t).atZone(Zeitraum.PRAXIS));
            if (t.plus(anfrage.raster()).isBefore(t)) {
                break; // über Mitternacht gewickelt
            }
        }
        return zeiten;
    }

    private static void zaehle(Map<String, Integer> ausgeschlossen, Pruefbericht bericht, int anzahl) {
        String grund = bericht.ersterVerstoss().map(Pruefergebnis::regel).orElse("unbekannt");
        ausgeschlossen.merge(grund, anzahl, Integer::sum);
    }
}
