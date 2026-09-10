package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Die absolute Laufzeit der Physio-Verordnung, drei oder sechs Monate. */
class VerordnungsGueltigkeitTest {

    private final VerordnungsGueltigkeit regel = new VerordnungsGueltigkeit();

    private Pruefergebnis physio(int einheiten, LocalDate stichtag) {
        return regel.pruefe(
                Verlauf.verordnung(Therapieform.PHYSIOTHERAPIE, einheiten), Verlauf.mitAbstaenden(7), stichtag);
    }

    @DisplayName("Sechs Einheiten oder weniger laufen drei Monate, mehr laufen sechs")
    @ParameterizedTest(name = "{3}")
    @CsvSource({
        "6,  3, ERFUELLT, Kleine Verordnung am letzten Tag der Laufzeit",
        "6,  4, VERLETZT, Kleine Verordnung einen Monat spaeter",
        "7,  3, ERFUELLT, Grosse Verordnung nach drei Monaten noch gueltig",
        "7,  6, ERFUELLT, Grosse Verordnung am letzten Tag der Laufzeit",
        "7,  7, VERLETZT, Grosse Verordnung einen Monat nach Ablauf",
    })
    void laufzeitNachMenge(int einheiten, int monate, Pruefergebnis.Ausgang erwartet, String situation) {
        LocalDate stichtag = Verlauf.ERSTER_TAG.plusMonths(monate);
        assertEquals(erwartet, physio(einheiten, stichtag).ausgang(), situation);
    }

    @Test
    @DisplayName("Der Stichtag selbst gehoert noch dazu, der Tag danach nicht")
    void anDerTagesgrenze() {
        LocalDate verfallstag = Verlauf.ERSTER_TAG.plusMonths(3);

        assertTrue(physio(6, verfallstag).istErfuellt(), "am Verfallstag noch gueltig");
        assertEquals(
                Pruefergebnis.Ausgang.VERLETZT,
                physio(6, verfallstag.plusDays(1)).ausgang(),
                "einen Tag danach nicht mehr");
    }

    @Test
    @DisplayName("Die Laufzeit beginnt mit der ersten Behandlung, nicht mit der Ausstellung")
    void bezugspunktIstDieErsteBehandlung() {
        Pruefergebnis ergebnis = physio(6, Verlauf.ERSTER_TAG.plusMonths(3));
        assertTrue(
                ergebnis.begruendung().contains(Verlauf.ERSTER_TAG.toString()),
                "die Begruendung nennt den ersten Behandlungstag als Bezugspunkt");
    }

    @Test
    @DisplayName("Ohne Behandlung laeuft noch keine Frist")
    void ohneBehandlung() {
        Pruefergebnis ergebnis = regel.pruefe(
                Verlauf.verordnung(Therapieform.PHYSIOTHERAPIE, 6),
                Behandlungsverlauf.leer(),
                Verlauf.ERSTER_TAG.plusYears(1));
        assertTrue(ergebnis.istErfuellt());
    }

    @Test
    @DisplayName("Fuer die Ergotherapie ist keine Laufzeit belegt, und die Regel sagt es")
    void ergotherapieHatKeineBelegteLaufzeit() {
        Pruefergebnis ergebnis = regel.pruefe(
                Verlauf.verordnung(Therapieform.ERGOTHERAPIE, 10),
                Verlauf.mitAbstaenden(7),
                Verlauf.ERSTER_TAG.plusYears(5));

        assertTrue(ergebnis.istErfuellt(), "eine unsichere Quelle wird nicht zur erfundenen Frist");
        assertTrue(
                ergebnis.begruendung().contains("Ergotherapie"),
                "das Ergebnis benennt, warum hier nichts geprueft wird");
    }
}
