package de.aptum.scheduling.infrastructure.mandant;

import de.aptum.scheduling.application.mandant.MandantId;
import de.aptum.scheduling.application.mandant.MandantKontext;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.orm.jpa.EntityManagerHolder;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Die eine Stelle, an der der Mandant in die Datenbank gelangt.
 *
 * <p>Zu Beginn jeder Transaktion setzt dieser Manager die Sitzungsvariable
 * {@code app.mandant_id}, gegen die Row Level Security jede Zeile prüft.
 * Kein Service, kein Repository muss daran denken — und keines kann es
 * vergessen. Das ist die Antwort auf den Nachteil, den ADR-002 selbst
 * benennt: Die Sitzungsvariable ist der neue Ort zum Vergessen, also gehört
 * sie an genau eine Stelle.
 *
 * <p>Gesetzt wird mit {@code set_config(name, wert, true)}. Das {@code true}
 * ist der entscheidende Teil: Die Variable gilt nur für diese Transaktion.
 * Ein {@code SET} auf der Verbindung überlebte die Rückgabe an den Pool und
 * liefe zum nächsten Mandanten weiter.
 *
 * <p><strong>Die Reihenfolge ist nicht beliebig.</strong> Der Mandant wird
 * geholt, <em>bevor</em> die Transaktion beginnt. Wirft der Kontext erst
 * danach, bleibt ein EntityManager an den Thread gebunden, den Spring nicht
 * mehr aufräumt — und die nächste Transaktion auf demselben Thread hält ihn
 * für ihre eigene, ruft {@code doBegin} nie und setzt keinen Mandanten. Der
 * Isolationstest hat genau das gezeigt: Der Test „ohne Mandant beginnt keine
 * Transaktion" ließ die fünf anderen scheitern.
 */
public class MandantTransactionManager extends JpaTransactionManager {

    private final MandantKontext kontext;

    public MandantTransactionManager(EntityManagerFactory entityManagerFactory, MandantKontext kontext) {
        super(entityManagerFactory);
        this.kontext = kontext;
    }

    @Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
        // Zuerst, damit ohne Mandanten nichts gebunden wird. Laut statt leise.
        MandantId mandant = kontext.aktuell();

        super.doBegin(transaction, definition);
        try {
            EntityManagerHolder halter =
                    (EntityManagerHolder) TransactionSynchronizationManager.getResource(obtainEntityManagerFactory());
            halter.getEntityManager()
                    .createNativeQuery("select set_config('app.mandant_id', :mandant, true)")
                    .setParameter("mandant", mandant.wert())
                    .getSingleResult();
        } catch (RuntimeException e) {
            // Dieselbe Aufräumarbeit, die Spring bei einem Fehler im eigenen
            // doBegin leistet: Holder lösen, EntityManager schließen. Sonst
            // bleibt eine halbe Transaktion am Thread hängen.
            doCleanupAfterCompletion(transaction);
            throw new CannotCreateTransactionException("Mandant konnte nicht gesetzt werden: " + mandant.wert(), e);
        }
    }
}
