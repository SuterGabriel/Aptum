package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.domain.abrechnung.Abrechnungsuebersicht;
import de.aptum.scheduling.domain.abrechnung.Abrechnungszeile;
import de.aptum.scheduling.domain.model.VerordnungAkte;
import de.aptum.scheduling.domain.model.VerordnungId;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.regel.Regelwerk;
import java.util.List;

/**
 * Eine Verordnung ansehen: der Vordruck, was darauf erbracht ist, und ob das
 * prüffest ist — dieselbe Zeile wie in der Abrechnungsübersicht, für eine
 * Kennung. Wer in der Suche eine Kennung eintippt, sieht so, wovon er spricht,
 * bevor er sucht.
 *
 * <p>Eine fremde Kennung ist eine unbekannte (ADR-002): Die Ablage liefert
 * für den falschen Mandanten {@code empty}, und die Antwort unterscheidet
 * nicht zwischen „gibt es nicht" und „gehört jemand anderem".
 */
public final class VerordnungAnzeigen {

    private final VerordnungRepository verordnungen;
    private final Abrechnungsuebersicht uebersicht;

    public VerordnungAnzeigen(VerordnungRepository verordnungen, Regelwerk regelwerk) {
        this.verordnungen = verordnungen;
        this.uebersicht = new Abrechnungsuebersicht(regelwerk);
    }

    public Abrechnungszeile ausfuehren(VerordnungId id) {
        VerordnungAkte akte = verordnungen.lade(id).orElseThrow(() -> new UnbekannteVerordnung(id));
        return uebersicht.berechne(List.of(akte)).get(0);
    }
}
