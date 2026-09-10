package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Diagnosegruppe;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Die harte Mengengrenze je Rezept - und die weiche, die keine ist. */
class HoechstmengeGrenzeTest {

    private final HoechstmengeGrenze regel = new HoechstmengeGrenze();

    private Pruefergebnis pruefe(Diagnosegruppe gruppe, int einheiten) {
        return regel.pruefe(Verlauf.verordnung(gruppe, einheiten, Verlauf.REGELFREQUENZ));
    }

    @DisplayName("Jede Gruppe: die Hoechstmenge selbst geht, eine mehr nicht")
    @ParameterizedTest(name = "{0}")
    @EnumSource(Diagnosegruppe.class)
    void anDerGrenzeJederGruppe(Diagnosegruppe gruppe) {
        int hoechstmenge = gruppe.hoechstmengeJeVerordnung();

        assertTrue(pruefe(gruppe, hoechstmenge).istErfuellt(), "genau die Hoechstmenge ist zulaessig");
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                pruefe(gruppe, hoechstmenge + 1).ausgang(),
                "eine Einheit darueber nicht mehr");
    }

    @Test
    @DisplayName("Die ZNS-Gruppen duerfen mehr als die uebrigen Physio-Gruppen")
    void znsGruppenHabenEineHoehereGrenze() {
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                pruefe(Diagnosegruppe.WS, 10).ausgang(),
                "zehn Einheiten sind fuer die Wirbelsaeule zu viel");
        assertTrue(pruefe(Diagnosegruppe.ZN, 10).istErfuellt(), "fuer die ZNS-Gruppe sind zehn zulaessig");
    }

    @Test
    @DisplayName("Geprueft wird die Hoechstmenge, nicht die orientierende Menge")
    void dieOrientierendeMengeIstNichtDieGrenze() {
        // ZN lässt 10 Einheiten je Verordnung zu, die orientierende Menge
        // liegt bei 30. Zwanzig Einheiten sind also unterhalb des Richtwerts
        // und trotzdem unzulässig - wer hier gegen die orientierende Menge
        // prüft, lässt ein ungültiges Rezept durch.
        assertTrue(Diagnosegruppe.ZN.orientierendeBehandlungsmenge() > 20);
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT, pruefe(Diagnosegruppe.ZN, 20).ausgang());
    }

    @Test
    @DisplayName("Keine einzelne Verordnung erreicht die orientierende Menge")
    void dieBeidenMengenSindVerschiedeneDinge() {
        // Der beste verfügbare Beleg dafür, dass die orientierende Menge ein
        // anderes Ding ist als die Höchstmenge: Sie liegt bei jeder Gruppe
        // darüber. Sie bezieht sich auf den Verordnungsfall, also auf mehrere
        // Rezepte, und kann von einem einzelnen gar nicht ausgeschöpft werden.
        for (Diagnosegruppe gruppe : Diagnosegruppe.values()) {
            assertTrue(
                    gruppe.orientierendeBehandlungsmenge() > gruppe.hoechstmengeJeVerordnung(),
                    "orientierende Menge ueber der Hoechstmenge bei " + gruppe);
        }
    }

    @Test
    @DisplayName("Die Therapieform folgt aus der Diagnosegruppe")
    void therapieformFolgtAusDerGruppe() {
        assertEquals(Therapieform.PHYSIOTHERAPIE, Diagnosegruppe.WS.therapieform());
        assertEquals(Therapieform.ERGOTHERAPIE, Diagnosegruppe.SB1.therapieform());
    }
}
