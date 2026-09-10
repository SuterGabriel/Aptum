package de.aptum.scheduling.infrastructure;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Die Abhängigkeitsrichtung über Modulgrenzen: infrastructure zu application
 * zu domain, niemals umgekehrt (ADR-001).
 *
 * <p>Dieser Test liegt hier und nicht im Domain-Modul, weil nur hier alle
 * drei Module auf dem Klassenpfad sind. Der Test im Domain-Modul prüft, was
 * die Domäne <em>nicht</em> kennt; dieser prüft, wer wen kennen darf.
 *
 * <p>Maven erzwingt die Richtung schon über die Modulabhängigkeiten — domain
 * kann application gar nicht importieren, weil es nicht auf seinem Klassenpfad
 * liegt. Die Regel hier ist der zweite Riegel für den Fall, dass jemand die
 * Pom-Abhängigkeit „nur kurz" umdreht.
 */
@AnalyzeClasses(packages = "de.aptum.scheduling", importOptions = ImportOption.DoNotIncludeTests.class)
class ModulgrenzenTest {

    @ArchTest
    static final ArchRule domaeneKenntNiemanden = noClasses()
            .that()
            .resideInAPackage("..domain..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..")
            .because("ADR-001: Der Domänenkern hängt von nichts ab, das außerhalb liegt.");

    @ArchTest
    static final ArchRule anwendungKenntKeineInfrastruktur = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..infrastructure..")
            .because("ADR-001: Die Anwendungsschicht kennt ihre Ports, nicht deren Implementierung.");

    @ArchTest
    static final ArchRule keineEntityVerlaesstDenAdapter = noClasses()
            .that()
            .resideOutsideOfPackage("..persistenz..")
            .should()
            .dependOnClassesThat()
            .areAnnotatedWith(jakarta.persistence.Entity.class)
            .because("ADR-001: JPA-Entities werden nie als API-DTO durchgereicht. "
                    + "Eine Spaltenumbenennung darf kein API-Bruch sein, und die Entity kennt Spalten, "
                    + "die niemand nach außen sehen soll.");

    @ArchTest
    static final ArchRule anwendungKenntKeinFramework = noClasses()
            .that()
            .resideInAPackage("..application..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("org.springframework..", "jakarta..")
            .because("ADR-001: Spring existiert nur außen. Ein Anwendungsfall ist ohne Kontext testbar.");
}
