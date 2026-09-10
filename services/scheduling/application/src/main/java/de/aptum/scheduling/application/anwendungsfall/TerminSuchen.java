package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Termin;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.model.Wunschfenster;
import de.aptum.scheduling.domain.model.Zeitraum;
import de.aptum.scheduling.domain.port.TerminRepository;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.suche.SlotSuche;
import de.aptum.scheduling.domain.suche.Suchanfrage;
import de.aptum.scheduling.domain.suche.Suchergebnis;
import java.time.Duration;
import java.util.List;

/**
 * Die Terminsuche als Anwendungsfall: Akte laden, Kalender im Wunschfenster
 * laden, Stammdaten dazu, und die Suche der Domäne fragen.
 *
 * <p>Orchestriert, entscheidet nicht. Welche Kandidaten es in die Liste
 * schaffen, sagt das Regelwerk hinter der {@link SlotSuche}; diese Klasse
 * trägt nur zusammen, was es dafür braucht.
 */
public final class TerminSuchen {

    /** Beginnzeiten im Viertelstundenraster. Ein Praxisparameter, keine Fachzahl. */
    private static final Duration RASTER = Duration.ofMinutes(15);

    private final VerordnungRepository verordnungen;
    private final TerminRepository termine;
    private final Stammdaten stammdaten;
    private final SlotSuche suche;

    public TerminSuchen(
            VerordnungRepository verordnungen, TerminRepository termine, Stammdaten stammdaten, SlotSuche suche) {
        this.verordnungen = verordnungen;
        this.termine = termine;
        this.stammdaten = stammdaten;
        this.suche = suche;
    }

    public record Anfrage(VerordnungId verordnung, Heilmittel heilmittel, Wunschfenster wunsch) {}

    public Suchergebnis ausfuehren(Anfrage anfrage) {
        VerordnungAkte akte = verordnungen
                .lade(anfrage.verordnung())
                .orElseThrow(() -> new UnbekannteVerordnung(anfrage.verordnung()));

        Zeitraum fenster = new Zeitraum(
                anfrage.wunsch().von().atStartOfDay(Zeitraum.PRAXIS),
                anfrage.wunsch().bis().plusDays(1).atStartOfDay(Zeitraum.PRAXIS));
        List<Termin> bestehende = termine.imZeitraum(fenster);

        return suche.suche(new Suchanfrage(
                akte.verordnung(),
                akte.verlauf(),
                anfrage.heilmittel(),
                anfrage.wunsch(),
                stammdaten.therapeuten(),
                stammdaten.raeume(),
                bestehende,
                stammdaten.einstellung(),
                RASTER));
    }
}
