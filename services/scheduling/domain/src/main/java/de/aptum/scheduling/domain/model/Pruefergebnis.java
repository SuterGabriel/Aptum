package de.aptum.scheduling.domain.model;

import java.util.Objects;

/**
 * Das Ergebnis einer Regelprüfung.
 *
 * <p>Trägt immer eine Begründung, nie nur ein Ja oder Nein. Der Grund steht im
 * Skill {@code heilmittel-domain}: Wenn ein Slot abgelehnt wird, muss die
 * Oberfläche sagen können, <em>warum</em>. Ein nacktes {@code boolean} zwingt
 * später zur Rekonstruktion.
 *
 * <p>Der Name ist bewusst in ASCII geschrieben. Das Glossar der Domäne führt
 * ihn so, damit deutsche Fachbegriffe als Bezeichner funktionieren.
 *
 * @param ausgang     erfüllt, Warnung oder verletzt
 * @param regel       der Name der Regel, die geprüft hat
 * @param begruendung Klartext für die Oberfläche, kein Schlüssel
 */
public record Pruefergebnis(Ausgang ausgang, String regel, String begruendung) {

    /**
     * Die drei Ausgänge, die die Oberfläche unterscheidet.
     *
     * <p>WARNUNG ist kein abgeschwächtes VERLETZT: Es ist der Fall, in dem ein
     * Mensch entscheidet. Die Frequenzabweichung ist das Vorbild - sie ist
     * zulässig nach Rücksprache mit der verordnenden Person, also kann die
     * Domäne sie nicht allein ablehnen.
     */
    public enum Ausgang { ERFUELLT, WARNUNG, VERLETZT }

    public Pruefergebnis {
        Objects.requireNonNull(ausgang, "ausgang");
        Objects.requireNonNull(regel, "regel");
        Objects.requireNonNull(begruendung, "begruendung");
    }

    public static Pruefergebnis erfuellt(String regel, String begruendung) {
        return new Pruefergebnis(Ausgang.ERFUELLT, regel, begruendung);
    }

    public static Pruefergebnis warnung(String regel, String begruendung) {
        return new Pruefergebnis(Ausgang.WARNUNG, regel, begruendung);
    }

    public static Pruefergebnis verletzt(String regel, String begruendung) {
        return new Pruefergebnis(Ausgang.VERLETZT, regel, begruendung);
    }

    public boolean istErfuellt() {
        return ausgang == Ausgang.ERFUELLT;
    }
}
