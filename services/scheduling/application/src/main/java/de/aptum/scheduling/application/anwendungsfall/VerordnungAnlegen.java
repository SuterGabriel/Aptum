package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Verordnung;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.regel.HoechstmengeGrenze;

/**
 * Eine Verordnung erfassen.
 *
 * <p>Die einzige Regel, die hier greift, ist die Höchstmenge je Rezept — sie
 * gilt beim Erfassen, nicht beim Buchen. Das ist die Stelle, an der später
 * ein Vorschlag aus einem Formularfoto auf dieselbe deterministische Prüfung
 * trifft wie eine Eingabe von Hand.
 */
public final class VerordnungAnlegen {

    private final VerordnungRepository verordnungen;
    private final HoechstmengeGrenze hoechstmenge = new HoechstmengeGrenze();

    public VerordnungAnlegen(VerordnungRepository verordnungen) {
        this.verordnungen = verordnungen;
    }

    public VerordnungId ausfuehren(Verordnung verordnung) {
        Pruefergebnis ergebnis = hoechstmenge.pruefe(verordnung);
        if (!ergebnis.istErfuellt()) {
            throw new UngueltigeVerordnung(ergebnis);
        }
        VerordnungAkte akte = VerordnungAkte.neu(verordnung);
        verordnungen.speichere(akte);
        return akte.id();
    }
}
