package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Heilmittel;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Zertifikatsleistung;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Wer darf was erbringen. Die erste Regel zur Dimension Therapeut. */
class QualifikationsGrenzeTest {

    private final QualifikationsGrenze regel = new QualifikationsGrenze();

    private static final Therapeut ALPHA = Therapeut.ohneZertifikate("T. Alpha");
    private static final Therapeut BETA = Therapeut.mit("T. Beta", Zertifikatsleistung.LYMPHDRAINAGE);

    @Test
    @DisplayName("Der Fall aus dem README: Lymphdrainage darf nicht jeder abrechnen")
    void lymphdrainageBrauchtDieErlaubnis() {
        Heilmittel mld = Heilmittel.MLD_GROSSBEHANDLUNG;

        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(mld, ALPHA).ausgang(),
                "ohne Abrechnungserlaubnis nicht erbringbar");
        assertTrue(regel.pruefe(mld, BETA).istErfuellt(), "mit Erlaubnis erbringbar");
    }

    @Test
    @DisplayName("Die Erlaubnis gilt nur fuer die Leistung, fuer die sie erteilt wurde")
    void erlaubnisIstNichtUebertragbar() {
        assertTrue(regel.pruefe(Heilmittel.MLD_GROSSBEHANDLUNG, BETA).istErfuellt());
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                regel.pruefe(Heilmittel.MANUELLE_THERAPIE, BETA).ausgang(),
                "Lymphdrainage berechtigt nicht zur Manuellen Therapie");
    }

    @Test
    @DisplayName("Ohne Zertifikatspflicht darf jede Person behandeln")
    void ohneZertifikatspflicht() {
        assertTrue(regel.pruefe(Heilmittel.KG_EINZEL, ALPHA).istErfuellt());
        assertTrue(regel.pruefe(Heilmittel.KLASSISCHE_MASSAGE, ALPHA).istErfuellt());
    }

    @DisplayName("In der Ergotherapie greift die Regel nie, und sie sagt es")
    @ParameterizedTest(name = "{0}")
    @EnumSource(value = Heilmittel.class, names = "ERGO_.*", mode = EnumSource.Mode.MATCH_ALL)
    void ergotherapieHatKeineZertifikatspositionen(Heilmittel heilmittel) {
        assertEquals(Therapieform.ERGOTHERAPIE, heilmittel.therapieform());
        assertFalse(heilmittel.brauchtAbrechnungserlaubnis(), "die Ergotherapie kennt keine Zertifikatspositionen");
        assertTrue(regel.pruefe(heilmittel, ALPHA).istErfuellt());
    }

    @Test
    @DisplayName("Die Begruendung nennt Person und Leistung, nicht nur ein Nein")
    void begruendungIstBrauchbar() {
        Pruefergebnis ergebnis = regel.pruefe(Heilmittel.KG_GERAET, ALPHA);

        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("T. Alpha"), "wer");
        assertTrue(ergebnis.begruendung().contains("Gerät"), "was");
        assertTrue(ergebnis.begruendung().contains("abrechenbar"), "warum es zaehlt");
    }

    @Test
    @DisplayName("Jede zertifikatspflichtige Leistung ist einem Heilmittel zugeordnet")
    void keineVerwaisteZertifikatsleistung() {
        // Eine Zertifikatsleistung ohne Heilmittel wäre eine Erlaubnis, die
        // niemand braucht - ein Zeichen dafür, dass der Katalog und das
        // Modell auseinandergelaufen sind.
        for (Zertifikatsleistung leistung : Zertifikatsleistung.values()) {
            if (!leistung.istZertifikatspflichtig()) {
                continue;
            }
            boolean gefunden = false;
            for (Heilmittel heilmittel : Heilmittel.values()) {
                if (heilmittel.zertifikatsleistung() == leistung) {
                    gefunden = true;
                    break;
                }
            }
            assertTrue(gefunden, "kein Heilmittel verlangt " + leistung);
        }
    }
}
