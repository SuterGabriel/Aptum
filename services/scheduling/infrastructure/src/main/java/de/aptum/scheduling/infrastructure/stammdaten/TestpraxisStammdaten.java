package de.aptum.scheduling.infrastructure.stammdaten;

import de.aptum.scheduling.application.stammdaten.Stammdaten;
import de.aptum.scheduling.domain.model.Arbeitszeit;
import de.aptum.scheduling.domain.model.Dienstplan;
import de.aptum.scheduling.domain.model.Praxiseinstellung;
import de.aptum.scheduling.domain.model.Raum;
import de.aptum.scheduling.domain.model.Raumanforderung;
import de.aptum.scheduling.domain.model.Therapeut;
import de.aptum.scheduling.domain.model.Zertifikatsleistung;
import java.time.Duration;
import java.time.LocalTime;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Die Testpraxis. Zwei Personen, drei Räume, eine Einstellung.
 *
 * <p>Erkennbar synthetisch, wie es {@code DATENSCHUTZ.md} verlangt: „T. Alpha"
 * und „T. Beta" sind keine Namen, die je jemand getragen hat. Beta hat die
 * Abrechnungserlaubnis für Lymphdrainage und Gerätetraining, Alpha nicht —
 * damit die Qualifikationsregel in der Suche etwas zu tun hat.
 */
@Component
class TestpraxisStammdaten implements Stammdaten {

    private static final Therapeut ALPHA = Therapeut.ohneZertifikate("T. Alpha");
    private static final Therapeut BETA =
            Therapeut.mit("T. Beta", Zertifikatsleistung.LYMPHDRAINAGE, Zertifikatsleistung.GERAET);

    private static final Dienstplan VOLLZEIT =
            Dienstplan.mit(Arbeitszeit.werktags(LocalTime.of(8, 0), LocalTime.of(17, 0)));

    private static final List<Raum> RAEUME = List.of(
            Raum.behandlungsraum("Raum 1", 24),
            Raum.behandlungsraum("Raum 2", 24),
            new Raum("Gerätebereich", 40, 6, EnumSet.of(Raumanforderung.GERAETEBEREICH)));

    private static final Praxiseinstellung EINSTELLUNG =
            new Praxiseinstellung(Duration.ofMinutes(5), Duration.ofMinutes(20));

    @Override
    public Map<Therapeut, Dienstplan> therapeuten() {
        return Map.of(ALPHA, VOLLZEIT, BETA, VOLLZEIT);
    }

    @Override
    public List<Raum> raeume() {
        return RAEUME;
    }

    @Override
    public Praxiseinstellung einstellung() {
        return EINSTELLUNG;
    }

    @Override
    public Optional<Therapeut> therapeut(String kuerzel) {
        return therapeuten().keySet().stream()
                .filter((t) -> t.kuerzel().equals(kuerzel))
                .findFirst();
    }

    @Override
    public Optional<Raum> raum(String bezeichnung) {
        return RAEUME.stream()
                .filter((r) -> r.bezeichnung().equals(bezeichnung))
                .findFirst();
    }
}
