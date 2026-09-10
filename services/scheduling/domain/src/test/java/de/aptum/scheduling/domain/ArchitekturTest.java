package de.aptum.scheduling.domain;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * ADR-001, maschinell geprüft.
 *
 * <p>Die Regel „die Domäne kennt kein Framework" stand bisher in
 * {@code CLAUDE.md} mit dem Zusatz, ArchUnit prüfe sie bei jedem Lauf. Das war
 * eine Behauptung ohne Gate. Jetzt ist es eines: Ein Agent, der eine
 * Spring-Annotation in den Domänenkern schreibt, bekommt den Build rot zurück
 * statt eines Review-Kommentars.
 *
 * <p>Solange es nur das Domain-Modul gibt, prüfen die Regeln gegen Pakete, die
 * gar nicht auf dem Klassenpfad liegen — ein Import würde schon nicht
 * kompilieren. Die Regeln stehen trotzdem hier, weil sie mit der
 * Anwendungsschicht scharf werden und dann nicht erst geschrieben werden
 * sollen. Die Regel gegen {@code java.sql} und die Legacy-Zeitklassen greift
 * heute schon.
 */
@AnalyzeClasses(packages = "de.aptum.scheduling.domain", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitekturTest {

    @ArchTest
    static final ArchRule keinFrameworkImDomaenenkern = noClasses()
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework..",
                    "jakarta..",
                    "javax.persistence..",
                    "javax.validation..",
                    "com.fasterxml.jackson..")
            .because("ADR-001: Das Domain-Modul kennt kein Framework. Spring existiert nur außen.");

    @ArchTest
    static final ArchRule keineDatenbankImDomaenenkern = noClasses()
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("java.sql..", "javax.sql..")
            .because("ADR-001: Repository-Interfaces liegen im Domain-Modul, ihre Implementierungen außen.");

    @ArchTest
    static final ArchRule nurJavaTime = noClasses()
            .should()
            .dependOnClassesThat()
            .belongToAnyOf(java.util.Date.class, java.util.Calendar.class, java.text.SimpleDateFormat.class)
            .because("Skill heilmittel-domain: immer java.time, nie Date oder Calendar.");

    @ArchTest
    static final ArchRule abhaengigkeitNurNachInnen = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..")
            .because("ADR-001: infrastructure zu application zu domain, niemals umgekehrt.");
}
