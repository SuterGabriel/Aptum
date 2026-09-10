package de.aptum.scheduling.infrastructure;

import de.aptum.scheduling.application.anwendungsfall.TerminBuchen;
import de.aptum.scheduling.application.anwendungsfall.TerminSuchen;
import de.aptum.scheduling.application.anwendungsfall.VerordnungAnlegen;
import de.aptum.scheduling.application.anwendungsfall.WocheAnzeigen;
import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.port.TerminRepository;
import de.aptum.scheduling.domain.port.VerordnungRepository;
import de.aptum.scheduling.domain.regel.Regelwerk;
import de.aptum.scheduling.domain.suche.SlotSuche;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Die Verdrahtung. Domäne und Anwendungsfälle kennen kein Spring; hier
 * werden sie zu Beans, und nur hier. Das ist das Muster aus dem Skill
 * {@code java-spring-hexagonal}: Die Regel bleibt ein POJO, die
 * Konfiguration außen macht sie verfügbar.
 */
@Configuration
class AnwendungsfallKonfiguration {

    @Bean
    Regelwerk regelwerk() {
        return new Regelwerk();
    }

    @Bean
    SlotSuche slotSuche(Regelwerk regelwerk) {
        return new SlotSuche(regelwerk);
    }

    @Bean
    VerordnungAnlegen verordnungAnlegen(VerordnungRepository verordnungen) {
        return new VerordnungAnlegen(verordnungen);
    }

    @Bean
    TerminSuchen terminSuchen(
            VerordnungRepository verordnungen, TerminRepository termine, Stammdaten stammdaten, SlotSuche suche) {
        return new TerminSuchen(verordnungen, termine, stammdaten, suche);
    }

    @Bean
    WocheAnzeigen wocheAnzeigen(TerminRepository termine, Stammdaten stammdaten) {
        return new WocheAnzeigen(termine, stammdaten);
    }

    @Bean
    TerminBuchen terminBuchen(
            VerordnungRepository verordnungen, TerminRepository termine, Stammdaten stammdaten, Regelwerk regelwerk) {
        return new TerminBuchen(verordnungen, termine, stammdaten, regelwerk);
    }
}
