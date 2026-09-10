package de.aptum.scheduling.infrastructure.mandant;

import de.aptum.scheduling.application.mandant.MandantId;
import de.aptum.scheduling.application.mandant.MandantKontext;
import java.util.function.Supplier;
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
    public <T> T als(MandantId mandant, Supplier<T> arbeit) {
        MandantId vorher = AKTUELL.get();
        AKTUELL.set(mandant);
        try {
            return arbeit.get();
        } finally {
            if (vorher == null) {
                AKTUELL.remove();
            } else {
                AKTUELL.set(vorher);
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
