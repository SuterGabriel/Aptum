package de.aptum.scheduling.domain.kalender;

import de.aptum.scheduling.domain.model.Abwesenheit;
import de.aptum.scheduling.domain.model.Arbeitszeit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zeitraum;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Die Woche, wie das Gitter sie zeigt: je Therapeutin eine Spalte, darin
 * alles, was nicht frei ist.
 *
 * <p>Das ist keine Suche und keine Regel. Es ist die Frage „was liegt wo?",
 * beantwortet aus denselben Bausteinen, die die Suche befragt: Was ein
 * Termin für die Therapeutin belegt und was er für den Raum belegt, sagt
 * {@link Termin} — deshalb kann das Gitter Rüstzeit und Nachruhe gar nicht
 * anders zeichnen als die Suche sie rechnet.
 *
 * <p>Frei wird nicht geliefert. Frei ist, was übrig bleibt, und das weiß
 * jedes Gitter selbst.
 */
public final class Wochenansicht {

    private static final EnumSet<DayOfWeek> TAGE = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);

    /** Text der Sperre außerhalb der Dienstzeit. */
    static final String AUSSERHALB = "außerhalb der Arbeitszeit";

    /** Text der Sperre an Tagen ohne Dienst. */
    static final String KEIN_DIENST = "kein Dienst";

    /**
     * @param montag      der Montag der Woche
     * @param therapeuten wer, mit Dienstplan
     * @param termine     alles, was in der Woche gebucht ist
     * @param einstellung Rüstzeit und Nachruhe der Praxis
     * @param tagesbeginn erste Uhrzeit im Gitter
     * @param tagesende   letzte Uhrzeit im Gitter
     */
    public record Anfrage(
            LocalDate montag,
            Map<Therapeut, Dienstplan> therapeuten,
            List<Termin> termine,
            Praxiseinstellung einstellung,
            LocalTime tagesbeginn,
            LocalTime tagesende) {

        public Anfrage {
            Objects.requireNonNull(montag, "montag");
            Objects.requireNonNull(therapeuten, "therapeuten");
            Objects.requireNonNull(termine, "termine");
            Objects.requireNonNull(einstellung, "einstellung");
            Objects.requireNonNull(tagesbeginn, "tagesbeginn");
            Objects.requireNonNull(tagesende, "tagesende");
            if (montag.getDayOfWeek() != DayOfWeek.MONDAY) {
                throw new IllegalArgumentException("Kein Montag: " + montag);
            }
            if (!tagesende.isAfter(tagesbeginn)) {
                throw new IllegalArgumentException("Tagesende vor Tagesbeginn: " + tagesbeginn + " bis " + tagesende);
            }
        }
    }

    /** Eine Spalte des Gitters: die Person und ihre Belegungen, nach Beginn sortiert. */
    public record Spalte(Therapeut therapeut, List<Belegung> belegungen) {}

    private Wochenansicht() {}

    /** Spalten in der Reihenfolge der Kürzel, damit das Gitter stabil bleibt. */
    public static List<Spalte> berechne(Anfrage anfrage) {
        return anfrage.therapeuten().entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getKey().kuerzel()))
                .map(e -> new Spalte(e.getKey(), belegungen(anfrage, e.getKey(), e.getValue())))
                .toList();
    }

    private static List<Belegung> belegungen(Anfrage anfrage, Therapeut therapeut, Dienstplan dienstplan) {
        List<Belegung> liste = new ArrayList<>();
        for (DayOfWeek wochentag : TAGE) {
            LocalDate tag = anfrage.montag().with(wochentag);
            liste.addAll(dienst(anfrage, dienstplan, tag));
        }
        anfrage.termine().stream()
                .filter(t -> t.therapeut().equals(therapeut))
                .forEach(t -> liste.addAll(termin(t, anfrage.einstellung())));
        liste.sort(Comparator.comparing(b -> b.zeitraum().von()));
        return List.copyOf(liste);
    }

    /** Abwesenheit, kein Dienst, oder die Ränder außerhalb der Arbeitszeit. */
    private static List<Belegung> dienst(Anfrage anfrage, Dienstplan dienstplan, LocalDate tag) {
        Zeitraum ganzerTag = new Zeitraum(um(tag, anfrage.tagesbeginn()), um(tag, anfrage.tagesende()));

        Abwesenheit abwesenheit = dienstplan.abwesenheiten().stream()
                .filter(a -> a.umfasst(tag))
                .findFirst()
                .orElse(null);
        if (abwesenheit != null) {
            return List.of(Belegung.ohneRaum(Belegungsart.ABWESENHEIT, ganzerTag, abwesenheit.grund()));
        }

        Arbeitszeit.Block block =
                dienstplan.arbeitszeit().am(tag.getDayOfWeek()).orElse(null);
        if (block == null) {
            return List.of(Belegung.ohneRaum(Belegungsart.GESPERRT, ganzerTag, KEIN_DIENST));
        }

        List<Belegung> raender = new ArrayList<>();
        if (block.von().isAfter(anfrage.tagesbeginn())) {
            raender.add(Belegung.ohneRaum(
                    Belegungsart.GESPERRT, new Zeitraum(ganzerTag.von(), um(tag, block.von())), AUSSERHALB));
        }
        if (block.bis().isBefore(anfrage.tagesende())) {
            raender.add(Belegung.ohneRaum(
                    Belegungsart.GESPERRT, new Zeitraum(um(tag, block.bis()), ganzerTag.bis()), AUSSERHALB));
        }
        return raender;
    }

    /**
     * Ein Termin wird zu drei oder vier Blöcken: Rüstzeit, Behandlung,
     * Rüstzeit — und die Nachruhe, wenn das Heilmittel eine vorsieht.
     *
     * <p>Die Grenzen kommen aus {@link Termin#belegtTherapeutin} und
     * {@link Termin#belegtRaum}, nicht aus eigener Rechnung. Was das Gitter
     * zeigt, ist genau das, was die Suche als belegt behandelt.
     */
    private static List<Belegung> termin(Termin termin, Praxiseinstellung einstellung) {
        Zeitraum behandlung = termin.behandlung();
        Zeitraum therapeutin = termin.belegtTherapeutin(einstellung);
        Zeitraum raum = termin.belegtRaum(einstellung);
        String heilmittel = termin.heilmittel().bezeichnung();
        String raumname = termin.raum().bezeichnung();

        List<Belegung> bloecke = new ArrayList<>();
        bloecke.add(Belegung.ohneRaum(
                Belegungsart.RUESTZEIT, new Zeitraum(therapeutin.von(), behandlung.von()), "Vorbereitung"));
        bloecke.add(Belegung.imRaum(Belegungsart.BELEGT, behandlung, heilmittel, raumname));
        bloecke.add(Belegung.ohneRaum(
                Belegungsart.RUESTZEIT, new Zeitraum(behandlung.bis(), therapeutin.bis()), "Nachbereitung"));
        if (raum.bis().isAfter(therapeutin.bis())) {
            bloecke.add(Belegung.imRaum(
                    Belegungsart.NACHRUHE, new Zeitraum(therapeutin.bis(), raum.bis()), "Nachruhe", raumname));
        }
        return bloecke;
    }

    private static ZonedDateTime um(LocalDate tag, LocalTime uhrzeit) {
        return tag.atTime(uhrzeit).atZone(Zeitraum.PRAXIS);
    }
}
