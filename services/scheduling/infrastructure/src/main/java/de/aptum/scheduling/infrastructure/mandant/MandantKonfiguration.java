package de.aptum.scheduling.infrastructure.mandant;

import de.aptum.scheduling.application.mandant.MandantKontext;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Ersetzt den Transaktionsmanager von Spring Boot durch den, der den
 * Mandanten setzt. Spring Boot tritt zurück, sobald eine Bean dieses Typs
 * existiert — es gibt danach keinen zweiten Manager, der ohne Mandant
 * arbeiten könnte.
 */
@Configuration
class MandantKonfiguration {

    @Bean
    PlatformTransactionManager transactionManager(EntityManagerFactory entityManagerFactory, MandantKontext kontext) {
        return new MandantTransactionManager(entityManagerFactory, kontext);
    }
}
