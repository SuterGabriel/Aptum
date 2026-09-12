package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.domain.abrechnung.Abrechnungsuebersicht;
import de.aptum.scheduling.domain.abrechnung.Abrechnungszeile;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.regel.Regelwerk;
import java.util.List;

/**
 * Die Abrechnungsübersicht als Anwendungsfall: alle Akten des Mandanten
 * laden, die Domäne fragen, was davon prüffest ist.
 *
 * <p>Lesend, ohne Nebenwirkung. Welcher Mandant, entscheidet die Datenbank
 * aus dem Kontext der Anfrage (ADR-002) — dieser Anwendungsfall weiß es
 * nicht und muss es nicht wissen.
 */
public final class AbrechnungAnzeigen {

    private final VerordnungRepository verordnungen;
    private final Abrechnungsuebersicht uebersicht;

    public AbrechnungAnzeigen(VerordnungRepository verordnungen, Regelwerk regelwerk) {
        this.verordnungen = verordnungen;
        this.uebersicht = new Abrechnungsuebersicht(regelwerk);
    }

    public List<Abrechnungszeile> ausfuehren() {
        return uebersicht.berechne(verordnungen.alle());
    }
}
