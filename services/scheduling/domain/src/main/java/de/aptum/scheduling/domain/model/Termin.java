package de.aptum.scheduling.domain.model;

import java.time.Duration;
import java.util.Objects;

/**
 * Ein gebuchter Termin: wer behandelt was, wo und wann.
 *
 * <p>Die {@link #behandlung()} ist die Zeit am Patienten. Was der Termin
 * darüber hinaus <em>belegt</em>, ist je Ressource verschieden, und das ist
 * der Kern dieses Typs:
 *
 * <ul>
 *   <li>{@link #belegtTherapeutin(Praxiseinstellung)} — Behandlung plus
 *       Rüstzeit davor und danach. Die Therapeutin bereitet vor und räumt auf.
 *   <li>{@link #belegtRaum(Praxiseinstellung)} — dasselbe, plus die Nachruhe
 *       danach, wenn das Heilmittel eine vorsieht. Der Patient ruht im Raum;
 *       die Therapeutin ist schon beim nächsten.
 * </ul>
 *
 * <p>Wer beide Fragen mit derselben Antwort beantwortet, blockiert entweder
 * die Therapeutin während einer Nachruhe, in der sie behandeln könnte — oder
 * gibt den Raum frei, während noch jemand darin liegt.
 *
 * @param therapeut  wer behandelt
 * @param raum       wo
 * @param heilmittel was
 * @param behandlung die Zeit am Patienten, ohne Rüstzeit und Nachruhe
 */
public record Termin(Therapeut therapeut, Raum raum, Heilmittel heilmittel, Zeitraum behandlung) {

    public Termin {
        Objects.requireNonNull(therapeut, "therapeut");
        Objects.requireNonNull(raum, "raum");
        Objects.requireNonNull(heilmittel, "heilmittel");
        Objects.requireNonNull(behandlung, "behandlung");
    }

    /** Behandlung plus Rüstzeit vor und nach ihr. */
    public Zeitraum belegtTherapeutin(Praxiseinstellung einstellung) {
        return behandlung.erweitertUm(einstellung.ruestzeit(), einstellung.ruestzeit());
    }

    /** Behandlung plus Rüstzeit, plus Nachruhe danach, wenn das Heilmittel sie vorsieht. */
    public Zeitraum belegtRaum(Praxiseinstellung einstellung) {
        Duration danach = heilmittel.brauchtNachruhe()
                ? einstellung.ruestzeit().plus(einstellung.nachruhe())
                : einstellung.ruestzeit();
        return behandlung.erweitertUm(einstellung.ruestzeit(), danach);
    }
}
