package de.aptum.scheduling.infrastructure;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Der Einstiegspunkt. Das erste, was man in diesem Repo starten kann.
 *
 * <p>Spring existiert nur in diesem Modul. Die Pakete darunter sind Adapter:
 * REST nach außen, Persistenz nach unten, und die Verdrahtung der
 * Domänenregeln als Beans — die Regeln selbst wissen davon nichts.
 */
@SpringBootApplication
public class AptumApplication {

    public static void main(String[] args) {
        SpringApplication.run(AptumApplication.class, args);
    }
}
