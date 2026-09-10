package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Die Zahl, die in der Terminsuche als „4 von 10" steht. */
class RestkontingentGrenzeTest {

    private final RestkontingentGrenze regel = new RestkontingentGrenze();

    /** Ein Verlauf mit so vielen Behandlungen, alle im zulaessigen Abstand. */
    private Behandlungsverlauf erbracht(int anzahl) {
        if (anzahl == 0) {
            return Behandlungsverlauf.leer();
        }
        long[] abstaende = IntStream.range(1, anzahl).mapToLong(i -> 7L).toArray();
        return Verlauf.mitAbstaenden(abstaende);
    }

    private Pruefergebnis pruefe(int verordnet, int schonErbracht) {
        return regel.pruefe(
                Verlauf.verordnung(Diagnosegruppe.ZN, verordnet, Verlauf.REGELFREQUENZ), erbracht(schonErbracht));
    }

    @DisplayName("Solange eine Einheit offen ist, kann gebucht werden")
    @ParameterizedTest(name = "{3}")
    @CsvSource({
        "6, 0, ERFUELLT, Noch keine Behandlung erbracht",
        "6, 5, ERFUELLT, Die letzte Einheit ist noch offen",
        "6, 6, VERLETZT, Alle Einheiten erbracht",
        "6, 7, VERLETZT, Mehr erbracht als verordnet, darf nicht vorkommen",
    })
    void restkontingent(int verordnet, int schonErbracht, Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(erwartet, pruefe(verordnet, schonErbracht).ausgang(), situation);
    }

    @Test
    @DisplayName("Die Begruendung nennt beide Zahlen, damit die Rezeption sie vorlesen kann")
    void begruendungNenntRestUndGesamt() {
        Pruefergebnis ergebnis = pruefe(10, 6);
        assertTrue(ergebnis.begruendung().contains("4"), "der Rest");
        assertTrue(ergebnis.begruendung().contains("10"), "die verordnete Menge");
    }

    @Test
    @DisplayName("Ist das Kontingent aufgebraucht, nennt die Begruendung den Ausweg")
    void aufgebrauchtNenntDieNeueVerordnung() {
        Pruefergebnis ergebnis = pruefe(6, 6);
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(
                ergebnis.begruendung().contains("neue"), "eine Ablehnung ohne Ausweg schickt die Rezeption ins Leere");
    }
}
