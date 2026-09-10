package de.aptum.scheduling.domain.regel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.aptum.scheduling.domain.model.Behandlungsverlauf;
import de.aptum.scheduling.domain.model.Pruefergebnis;
import de.aptum.scheduling.domain.model.Therapieform;
import de.aptum.scheduling.domain.model.Unterbrechungskennzeichen;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Die 70-Tage-Summe der Ergotherapie. */
class UnterbrechungsSummeTest {

    private static final int EINHEITEN = 10;

    private final UnterbrechungsSumme regel = new UnterbrechungsSumme();

    private Pruefergebnis ergo(Behandlungsverlauf verlauf) {
        return regel.pruefe(Verlauf.verordnung(Therapieform.ERGOTHERAPIE, EINHEITEN), verlauf);
    }

    @Test
    @DisplayName("Genau 70 Tage sind zulaessig, 71 nicht")
    void anDerGrenze() {
        Behandlungsverlauf gerade = Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 35, 35);
        Behandlungsverlauf darueber = Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 35, 36);

        assertTrue(ergo(gerade).istErfuellt(), "Summe genau 70");
        assertEquals(Pruefergebnis.Ausgang.VERLETZT, ergo(darueber).ausgang(), "Summe 71");
    }

    @Test
    @DisplayName("Kurze Pausen zaehlen gar nicht mit, auch viele nicht")
    void nurLuckenUeberVierzehnTagenZaehlen() {
        // Zehn Pausen von je 14 Tagen sind zusammen 140 Kalendertage. Die
        // Summenregel zählt davon keinen einzigen, weil jede einzelne Pause
        // die 14 Tage nicht überschreitet. Das ist die Feinheit, die der
        // Vertragstext ausdrücklich festhält.
        Behandlungsverlauf verlauf =
                Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 14, 14, 14, 14, 14, 14, 14, 14, 14, 14);

        Pruefergebnis ergebnis = ergo(verlauf);
        assertTrue(ergebnis.istErfuellt(), "keine einzige Pause ueberschreitet die 14 Tage");
        assertTrue(
                ergebnis.begruendung().contains("Summe 0"),
                "die Begruendung macht sichtbar, dass nichts gezaehlt wurde");
    }

    @Test
    @DisplayName("Eine 15-Tage-Pause zaehlt dagegen voll mit")
    void knappUeberDerZaehlgrenze() {
        Behandlungsverlauf verlauf = Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 15);
        assertTrue(
                ergo(verlauf).begruendung().contains("Summe 15"),
                "gezaehlt wird die ganze Pause, nicht nur der Teil ueber 14 Tagen");
    }

    @Test
    @DisplayName("Fuer die Physiotherapie greift die Regel nicht, und sie sagt es")
    void physiotherapieHatKeineSummengrenze() {
        Behandlungsverlauf weitUeberSiebzig = Verlauf.mitAbstaenden(Unterbrechungskennzeichen.K, 60, 60, 60);

        Pruefergebnis ergebnis =
                regel.pruefe(Verlauf.verordnung(Therapieform.PHYSIOTHERAPIE, EINHEITEN), weitUeberSiebzig);

        assertTrue(ergebnis.istErfuellt());
        assertTrue(
                ergebnis.begruendung().contains("Physiotherapie"),
                "ein Nichtfund darf im Ergebnis nicht wie eine vergessene Pruefung aussehen");
    }
}
