package de.aptum.scheduling.domain.model;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * Eine Therapeutin oder ein Therapeut der Praxis.
 *
 * <p>Bewusst klein, wie die Verordnung auch: Hier steht, was die bereits
 * gebaute Regel braucht. Arbeitszeiten und Abwesenheiten kommen mit der Regel,
 * die sie auswertet.
 *
 * <p>Die Abrechnungserlaubnisse sind eine Menge von Tatsachen, keine Menge von
 * Weiterbildungsnachweisen. Erteilt werden sie personengebunden durch die
 * Arbeitsgemeinschaft nach § 124 Abs. 2 SGB V
 * ({@code @fundstelle HM-QUAL-06}) — die Praxis kennt das Ergebnis, nicht die
 * Stundenzahl dahinter.
 *
 * <p>Der Name ist ein Kürzel und bleibt es. Die Testdaten dieses Projekts
 * heißen „T. Alpha" und „T. Beta"; das Modell braucht nicht mehr als eine
 * Bezeichnung für die Oberfläche.
 */
public record Therapeut(String kuerzel, Set<Zertifikatsleistung> abrechnungserlaubnisse) {

    public Therapeut {
        Objects.requireNonNull(kuerzel, "kuerzel");
        Objects.requireNonNull(abrechnungserlaubnisse, "abrechnungserlaubnisse");
        abrechnungserlaubnisse = Set.copyOf(abrechnungserlaubnisse);
    }

    /** Eine Person ohne Zusatzqualifikation. */
    public static Therapeut ohneZertifikate(String kuerzel) {
        return new Therapeut(kuerzel, EnumSet.noneOf(Zertifikatsleistung.class));
    }

    public static Therapeut mit(String kuerzel, Zertifikatsleistung... erlaubnisse) {
        return new Therapeut(kuerzel, Set.of(erlaubnisse));
    }

    public boolean darfAbrechnen(Zertifikatsleistung leistung) {
        return !leistung.istZertifikatspflichtig() || abrechnungserlaubnisse.contains(leistung);
    }
}
