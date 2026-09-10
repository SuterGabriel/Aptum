package de.aptum.scheduling.application.stammdaten;

import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Therapeut;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Wer und was in der Praxis vorhanden ist: Personen mit Dienstplan, Räume,
 * die Einstellung für Rüstzeit und Nachruhe.
 *
 * <p>Ein Port, dessen einzige Implementierung vorerst eine Testpraxis im Code
 * ist. Das ist eine bewusste Grenze dieses Projekts, keine vergessene: Eine
 * Stammdatenpflege wäre ein Formular über einer Tabelle und belegt nichts,
 * was die Terminsuche nicht schon belegt. Die Mandantentrennung zählt bei
 * Patientendaten, nicht bei Raumnamen.
 */
public interface Stammdaten {

    Map<Therapeut, Dienstplan> therapeuten();

    List<Raum> raeume();

    Praxiseinstellung einstellung();

    Optional<Therapeut> therapeut(String kuerzel);

    Optional<Raum> raum(String bezeichnung);
}
