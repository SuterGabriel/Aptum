package de.aptum.scheduling.infrastructure.mandant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.aptum.scheduling.application.mandant.MandantId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

/**
 * Der Mandant steht im Protokoll, solange die Anfrage läuft - und danach nicht
 * mehr. Ein Thread aus dem Pool trägt sonst den Mandanten der letzten Anfrage
 * in die nächste, und das wäre eine falsche Zuordnung im Log.
 */
class MandantKontextHalterTest {

    private final MandantKontextHalter halter = new MandantKontextHalter();

    @Test
    @DisplayName("Während der Arbeit steht der Mandant im MDC, danach ist er weg")
    void mandantImProtokollNurWaehrendDerAnfrage() {
        assertNull(MDC.get(MandantKontextHalter.PROTOKOLL_FELD));

        String gesehen = halter.als(new MandantId("praxis-a"), () -> MDC.get(MandantKontextHalter.PROTOKOLL_FELD));

        assertEquals("praxis-a", gesehen);
        assertNull(MDC.get(MandantKontextHalter.PROTOKOLL_FELD), "nach der Anfrage kein Mandant mehr im Protokoll");
    }

    @Test
    @DisplayName("Verschachtelt: innen der innere, danach wieder der äußere")
    void verschachtelt() {
        String[] gesehen = new String[3];
        halter.als(new MandantId("praxis-a"), () -> {
            gesehen[0] = MDC.get(MandantKontextHalter.PROTOKOLL_FELD);
            halter.als(new MandantId("praxis-b"), () -> gesehen[1] = MDC.get(MandantKontextHalter.PROTOKOLL_FELD));
            gesehen[2] = MDC.get(MandantKontextHalter.PROTOKOLL_FELD);
        });
        assertEquals("praxis-a", gesehen[0]);
        assertEquals("praxis-b", gesehen[1]);
        assertEquals("praxis-a", gesehen[2]);
    }
}
