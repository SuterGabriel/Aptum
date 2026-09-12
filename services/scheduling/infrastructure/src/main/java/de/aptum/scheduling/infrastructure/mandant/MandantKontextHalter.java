package de.aptum.scheduling.infrastructure.mandant;

import de.aptum.scheduling.application.mandant.MandantId;
import de.aptum.scheduling.application.mandant.MandantKontext;
import java.util.function.Supplier;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

/**
 * Der Mandant der laufenden Anfrage, gebunden an den Thread.
 *
 * <p>Gesetzt wird er von außen: von einem Filter, der ihn aus der Anfrage
 * liest, oder von einem Test, der ihn vorgibt. Gelesen wird er an genau
 * einer Stelle — vom {@link MandantTransactionManager}, der ihn je
 * Transaktion in die Datenbank gibt.
 *
 * <p>Ohne gesetzten Mandanten gibt es keinen Rückfall auf irgendeinen
 * Standard. {@link #aktuell()} wirft, und die Transaktion beginnt gar nicht
 * erst. Ein Standardmandant wäre die stille Variante der vergessenen
 * {@code WHERE}-Klausel.
 */
@Component
public class MandantKontextHalter implements MandantKontext {

    private static final ThreadLocal<MandantId> AKTUELL = new ThreadLocal<>();

    @Override
    public MandantId aktuell() {
        MandantId mandant = AKTUELL.get();
        if (mandant == null) {
            throw new IllegalStateException(
                    "Kein Mandant im Kontext. Jeder Datenbankzugriff braucht einen; ohne ihn beginnt keine Transaktion.");
        }
        return mandant;
    }

    /** Führt die Arbeit als dieser Mandant aus und räumt danach auf — auch bei einer Ausnahme. */
    /** Schlüssel im Protokoll. Jede Zeile einer Anfrage trägt den Mandanten als Feld. */
    static final String PROTOKOLL_FELD = "mandant";

    public <T> T als(MandantId mandant, Supplier<T> arbeit) {
        MandantId vorher = AKTUELL.get();
        AKTUELL.set(mandant);
        // Derselbe Ort, an dem der Mandant für die Anfrage gebunden wird, legt
        // ihn auch ins Protokoll - DATENSCHUTZ.md, Regel 5: Mandant ja,
        // Personenbezug nein. Eine Stelle, nicht zwei, die auseinanderlaufen.
        String protokollVorher = MDC.get(PROTOKOLL_FELD);
        MDC.put(PROTOKOLL_FELD, mandant.wert());
        try {
            return arbeit.get();
        } finally {
            if (vorher == null) {
                AKTUELL.remove();
            } else {
                AKTUELL.set(vorher);
            }
            if (protokollVorher == null) {
                MDC.remove(PROTOKOLL_FELD);
            } else {
                MDC.put(PROTOKOLL_FELD, protokollVorher);
            }
        }
    }

    public void als(MandantId mandant, Runnable arbeit) {
        als(mandant, () -> {
            arbeit.run();
            return null;
        });
    }
}
