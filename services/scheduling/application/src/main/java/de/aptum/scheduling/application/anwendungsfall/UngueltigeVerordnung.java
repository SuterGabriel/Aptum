package de.aptum.scheduling.application.anwendungsfall;

import de.aptum.scheduling.domain.model.Pruefergebnis;

/** Der Vordruck verstößt gegen eine Regel, die beim Erfassen gilt. Die Begründung nennt, welche. */
public class UngueltigeVerordnung extends RuntimeException {

    public UngueltigeVerordnung(Pruefergebnis ergebnis) {
        super(ergebnis.regel() + ": " + ergebnis.begruendung());
    }
}
