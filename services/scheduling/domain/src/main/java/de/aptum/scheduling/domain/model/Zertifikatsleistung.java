package de.aptum.scheduling.domain.model;

/**
 * Eine Leistung, die nur mit personengebundener Abrechnungserlaubnis erbracht
 * werden darf.
 *
 * <p><strong>Hier steht keine Stundenzahl, und das ist Absicht.</strong> Die
 * Weiterbildungsumfänge stehen zwar im Katalog, aber für die Lymphdrainage
 * widersprüchlich: Anlage 1 nennt 170 Stunden, die ab Juni 2026 wirksame
 * Anlage 7 nennt 140 Unterrichtseinheiten ({@code @fundstelle HM-QUAL-02},
 * Status UNSICHER). Nach ADR-007 darf daraus keine Konstante werden.
 *
 * <p>Der Verzicht ist kein Kompromiss, sondern das treffendere Modell. Eine
 * Praxis rechnet keine Weiterbildungsstunden nach. Sie prüft, ob die
 * Arbeitsgemeinschaft nach § 124 Abs. 2 SGB V die Abrechnungserlaubnis erteilt
 * hat — und die ist personengebunden ({@code @fundstelle HM-QUAL-06}). Ob
 * dahinter 140 oder 170 Stunden stehen, ist die Frage der Stelle, die sie
 * erteilt, nicht die der Terminplanung.
 *
 * <p>{@link #KEINE} steht für die Leistungen ohne Zertifikatspflicht und
 * erspart ein {@code null} oder {@code Optional} im Feld.
 */
public enum Zertifikatsleistung {

    /** Keine Zusatzqualifikation nötig. Der Regelfall. */
    KEINE("keine Zusatzqualifikation"),

    /** @fundstelle HM-QUAL-01 */
    MANUELLE_THERAPIE("Manuelle Therapie"),

    /** @fundstelle HM-QUAL-02 */
    LYMPHDRAINAGE("Manuelle Lymphdrainage"),

    /** @fundstelle HM-QUAL-03 */
    GERAET("Krankengymnastik am Gerät"),

    /** @fundstelle HM-QUAL-04 */
    KG_ZNS_ERWACHSENE("KG-ZNS für Erwachsene"),

    /** @fundstelle HM-QUAL-05 */
    KG_ZNS_KINDER("KG-ZNS für Kinder");

    private final String bezeichnung;

    Zertifikatsleistung(String bezeichnung) {
        this.bezeichnung = bezeichnung;
    }

    public String bezeichnung() {
        return bezeichnung;
    }

    public boolean istZertifikatspflichtig() {
        return this != KEINE;
    }
}
