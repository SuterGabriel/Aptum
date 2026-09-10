package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Die Grenzfälle der 14-Tage-Regel. Gilt für beide Therapieformen gleich. */
class UnterbrechungsFristTest {

    private static final int EINHEITEN = 6;

    private final UnterbrechungsFrist regel = new UnterbrechungsFrist();

    private Pruefergebnis pruefe(Therapieform form, Behandlungsverlauf verlauf) {
        return regel.pruefe(Verlauf.verordnung(form, EINHEITEN), verlauf);
    }

    @DisplayName("Die Grenze liegt bei 14 Tagen, ohne vermerkten Grund")
    @ParameterizedTest(name = "{2}")
    @CsvSource({
        "13, ERFUELLT, Pause kuerzer als die Grenze",
        "14, ERFUELLT, Pause genau auf der Grenze",
        "15, VERLETZT, Pause einen Tag ueber der Grenze",
        "60, VERLETZT, Patient kam zwei Monate nicht",
    })
    void ohneKennzeichen(long abstand, Pruefergebnis.Ausgang erwartet, String situation) {
        assertEquals(
                erwartet,
                pruefe(Therapieform.PHYSIOTHERAPIE, Verlauf.mitAbstaenden(abstand))
                        .ausgang(),
                situation);
    }

    @DisplayName("Ein vermerkter Grund haelt dieselbe Pause zulaessig")
    @ParameterizedTest(name = "Kennzeichen {0}")
    @CsvSource({"T", "K", "F"})
    void mitKennzeichen(Unterbrechungskennzeichen kennzeichen) {
        Behandlungsverlauf verlauf = Verlauf.mitAbstaenden(kennzeichen, 60);
        assertTrue(
                pruefe(Therapieform.PHYSIOTHERAPIE, verlauf).istErfuellt(), "60 Tage sind mit Begruendung zulaessig");
    }

    @Test
    @DisplayName("Das U gilt nur in der Ergotherapie")
    void kennzeichenUNurBeiErgo() {
        Behandlungsverlauf verlauf = Verlauf.mitAbstaenden(Unterbrechungskennzeichen.U, 60);

        assertTrue(
                pruefe(Therapieform.ERGOTHERAPIE, verlauf).istErfuellt(),
                "in der Ergotherapie ist U ein zulaessiges Kennzeichen");
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                pruefe(Therapieform.PHYSIOTHERAPIE, verlauf).ausgang(),
                "auf einer Physio-Verordnung ist U kein Grund, sondern gar keiner");
    }

    @Test
    @DisplayName("Eine einzelne Behandlung ist keine Reihe mit Luecke")
    void einzelnerTermin() {
        assertTrue(pruefe(Therapieform.PHYSIOTHERAPIE, Verlauf.mitAbstaenden()).istErfuellt());
        assertTrue(
                pruefe(Therapieform.PHYSIOTHERAPIE, Behandlungsverlauf.leer()).istErfuellt());
    }

    @Test
    @DisplayName("Es zaehlt die laengste Luecke, nicht die letzte")
    void luckeInDerMitte() {
        Behandlungsverlauf verlauf = Verlauf.mitAbstaenden(7, 40, 7);
        Pruefergebnis ergebnis = pruefe(Therapieform.PHYSIOTHERAPIE, verlauf);
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergebnis.ausgang());
        assertTrue(ergebnis.begruendung().contains("40"), "die Begruendung nennt die Luecke, die es war");
    }
}
