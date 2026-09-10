package de.aptum.scheduling.domain.regel;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.EnumSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Der Fall aus dem README: nicht jeder freie Raum passt zu jedem Termin. */
class RaumausstattungsGrenzeTest {

    private final RaumausstattungsGrenze regel = new RaumausstattungsGrenze();

    private static final Raum RAUM_2 = Raum.behandlungsraum("Raum 2", 24);

    private static Raum geraetebereich(int flaecheQm, int geraete) {
        return new Raum("Gerätebereich", flaecheQm, geraete,
                EnumSet.of(Raumanforderung.GERAETEBEREICH));
    }

    @Test
    @DisplayName("Der Fall aus dem Mockup: Geraet verlangt 30 Quadratmeter, Raum 2 hat 24")
    void raumZweiIstZuKlein() {
        Pruefergebnis ergebnis = regel.pruefe(Heilmittel.KG_GERAET, RAUM_2);

        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("Raum 2"), "welcher Raum");
        assertTrue(ergebnis.begruendung().contains("Gerät"), "welche Leistung");
    }

    @DisplayName("Grundflaeche plus Zuschlag je Geraet ueber den vier Pflichtgeraeten")
    @ParameterizedTest(name = "{3}")
    @CsvSource({
            "30, 4, ERFUELLT, Grundflaeche mit genau den Pflichtgeraeten",
            "29, 4, VERLETZT, Einen Quadratmeter unter der Grundflaeche",
            "34, 5, ERFUELLT, Ein Geraet mehr, vier Quadratmeter mehr Flaeche",
            "33, 5, VERLETZT, Ein Geraet mehr, aber die Flaeche fehlt",
            "30, 10, VERLETZT, Genug Flaeche fuer den Grundfall, zu viele Geraete darin",
            "54, 10, ERFUELLT, Zehn Geraete brauchen vierundfuenfzig Quadratmeter",
    })
    void flaecheUndGeraete(int flaecheQm, int geraete,
                           Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(erwartet,
                regel.pruefe(Heilmittel.KG_GERAET, geraetebereich(flaecheQm, geraete)).ausgang(),
                situation);
    }

    @Test
    @DisplayName("Ohne die vier Pflichtgeraete hilft auch eine grosse Flaeche nicht")
    void pflichtgeraeteFehlen() {
        Pruefergebnis ergebnis = regel.pruefe(Heilmittel.KG_GERAET, geraetebereich(100, 3));

        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("Pflichtgeräte"),
                "die Begruendung nennt den Grund, nicht die Flaeche");
    }

    @Test
    @DisplayName("Das Bewegungsbad ist eine Eignung, keine Rechnung")
    void bewegungsbad() {
        Raum bad = new Raum("Bewegungsbad", 40, 0, EnumSet.of(Raumanforderung.BEWEGUNGSBAD));

        assertTrue(regel.pruefe(Heilmittel.KG_BEWEGUNGSBAD, bad).istErfuellt());
        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(Heilmittel.KG_BEWEGUNGSBAD, RAUM_2).ausgang(),
                "ein Behandlungsraum ist kein Bewegungsbad");
    }

    @Test
    @DisplayName("Ein Geraetebereich ist kein gewoehnlicher Behandlungsraum")
    void eignungGiltNichtInBeideRichtungen() {
        assertEquals(Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(Heilmittel.KG_EINZEL, geraetebereich(60, 6)).ausgang(),
                "wer nur als Geraetebereich ausgewiesen ist, ist kein Behandlungsraum");
    }

    @DisplayName("Jedes Heilmittel der Grundausstattung passt in einen Behandlungsraum")
    @ParameterizedTest(name = "{0}")
    @EnumSource(Heilmittel.class)
    void grundausstattungPasstImmer(Heilmittel heilmittel) {
        if (heilmittel.raumanforderung() != Raumanforderung.GRUNDAUSSTATTUNG) {
            return;
        }
        assertTrue(regel.pruefe(heilmittel, RAUM_2).istErfuellt(),
                heilmittel + " braucht keinen besonderen Bereich");
    }
}
