package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Behandlungstermin;
import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Pruefbericht;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.Zeitraum;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Alle Regeln, einmal gesammelt. Die eine Stelle, an der eine Buchung geprüft
 * wird — egal, woher sie kommt.
 *
 * <p>Das ist Regel 3 aus {@code CLAUDE.md} in Code: <em>Der AI-Layer schlägt
 * vor, die Domäne entscheidet.</em> Ein Vorschlag aus der Slot-Suche, eine
 * Buchung von Hand und ein Vorschlag eines Sprachmodells laufen durch dieselbe
 * Methode. Es gibt keinen zweiten Weg, und das ist der Punkt.
 *
 * <p>Die Reihenfolge ist die des Buchungsdialogs: erst die Verordnung, dann
 * die Person, dann der Raum. Der erste Verstoß ist der, den die Oberfläche
 * nennt — deshalb stehen die fachlich schwersten Gründe vorn.
 *
 * <p>Die Verordnungsregeln werden gegen den Verlauf geprüft, <em>als wäre der
 * Termin schon dazugekommen</em>. Ob eine Unterbrechung zu lang wird,
 * entscheidet sich ja gerade durch den geplanten Tag.
 */
public final class Regelwerk {

    /** Ein zu prüfender Termin, noch nicht gebucht. */
    public record Kandidat(Zeitraum behandlung, Therapeut therapeut, Dienstplan dienstplan, Raum raum) {
        public LocalDate tag() {
            return behandlung.von().withZoneSameInstant(Zeitraum.PRAXIS).toLocalDate();
        }
    }

    /** Was für alle Kandidaten einer Suche gleich ist. */
    public record Kontext(
            Verordnung verordnung,
            Behandlungsverlauf verlauf,
            Heilmittel heilmittel,
            List<Termin> bestehende,
            Praxiseinstellung einstellung) {
    }

    private final BehandlungsbeginnFrist behandlungsbeginn = new BehandlungsbeginnFrist();
    private final UnterbrechungsFrist unterbrechung = new UnterbrechungsFrist();
    private final UnterbrechungsSumme unterbrechungsSumme = new UnterbrechungsSumme();
    private final VerordnungsGueltigkeit gueltigkeit = new VerordnungsGueltigkeit();
    private final FrequenzGrenze frequenz = new FrequenzGrenze();
    private final RestkontingentGrenze restkontingent = new RestkontingentGrenze();
    private final HoechstmengeGrenze hoechstmenge = new HoechstmengeGrenze();
    private final QualifikationsGrenze qualifikation = new QualifikationsGrenze();
    private final TherapeutVerfuegbar therapeutVerfuegbar = new TherapeutVerfuegbar();
    private final RaumausstattungsGrenze raumausstattung = new RaumausstattungsGrenze();
    private final RaumFrei raumFrei = new RaumFrei();

    /** Die Verordnungsregeln allein. Gleich für alle Kandidaten desselben Tages. */
    public Pruefbericht pruefeVerordnung(Kontext k, LocalDate tag) {
        Behandlungsverlauf mitKandidat = mitTermin(k.verlauf(), tag);
        List<Pruefergebnis> e = new ArrayList<>();
        e.add(hoechstmenge.pruefe(k.verordnung()));
        e.add(restkontingent.pruefe(k.verordnung(), k.verlauf()));
        e.add(behandlungsbeginn.pruefe(k.verordnung(),
                k.verlauf().ersterBehandlungstag().orElse(tag)));
        e.add(unterbrechung.pruefe(k.verordnung(), mitKandidat));
        e.add(unterbrechungsSumme.pruefe(k.verordnung(), mitKandidat));
        e.add(gueltigkeit.pruefe(k.verordnung(), mitKandidat, tag));
        e.add(frequenz.pruefe(k.verordnung(), k.verlauf(), tag));
        return new Pruefbericht(e);
    }

    /** Person und Raum für genau diesen Kandidaten. */
    public Pruefbericht pruefeRessourcen(Kontext k, Kandidat kandidat) {
        List<Pruefergebnis> e = new ArrayList<>();
        e.add(qualifikation.pruefe(k.heilmittel(), kandidat.therapeut()));
        e.add(therapeutVerfuegbar.pruefe(kandidat.therapeut(), kandidat.dienstplan(),
                k.bestehende(), kandidat.behandlung(), k.einstellung()));
        e.add(raumausstattung.pruefe(k.heilmittel(), kandidat.raum()));
        e.add(raumFrei.pruefe(kandidat.raum(), k.heilmittel(), k.bestehende(),
                kandidat.behandlung(), k.einstellung()));
        return new Pruefbericht(e);
    }

    /** Alles auf einmal. Für die Buchung von Hand und für jeden Vorschlag von außen. */
    public Pruefbericht pruefe(Kontext k, Kandidat kandidat) {
        List<Pruefergebnis> alle = new ArrayList<>(pruefeVerordnung(k, kandidat.tag()).ergebnisse());
        alle.addAll(pruefeRessourcen(k, kandidat).ergebnisse());
        return new Pruefbericht(alle);
    }

    private static Behandlungsverlauf mitTermin(Behandlungsverlauf verlauf, LocalDate tag) {
        List<Behandlungstermin> termine = new ArrayList<>(verlauf.termine());
        termine.add(Behandlungstermin.an(tag));
        return Behandlungsverlauf.aus(termine);
    }
}
