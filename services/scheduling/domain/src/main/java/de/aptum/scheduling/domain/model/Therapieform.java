package de.aptum.scheduling.domain.model;

/**
 * Physio- oder Ergotherapie.
 *
 * <p>Diese Unterscheidung ist kein Etikett, sondern der Grund, warum die
 * Unterbrechungsregeln getrennt modelliert sind. Die Ergotherapie begrenzt die
 * <em>Summe</em> der begründeten Unterbrechungen, die Physiotherapie die
 * <em>Gesamtlaufzeit</em> der Verordnung. Zwei gegenläufige Ansätze für
 * dasselbe Problem, in derselben Praxis nebeneinander.
 *
 * <p>Podologie, Logopädie und Ernährungstherapie fehlen mit Absicht. Sie sind
 * in der Produktsicht als Nicht-Ziel benannt: Ein dritter Zweig fügt
 * Datenpflege hinzu, aber keine neue Regelstruktur.
 */
public enum Therapieform {
    PHYSIOTHERAPIE,
    ERGOTHERAPIE;

    public boolean istErgotherapie() {
        return this == ERGOTHERAPIE;
    }

    public boolean istPhysiotherapie() {
        return this == PHYSIOTHERAPIE;
    }
}
