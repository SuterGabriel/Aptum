---
name: heilmittel-domain
description: Fachregeln der Heilmittelverordnung für Physio- und Ergotherapie - Fristen, Unterbrechung, Frequenz, Qualifikation, Ausstattung, Ausfallregelung - und das Vokabular der Domäne. Nutze diesen Skill vor jeder Änderung am Domänenmodell, bei jeder neuen Fachregel, bei jedem Test zur Slot-Berechnung und immer wenn Begriffe wie Verordnung, Heilmittel, Frequenz, Rüstzeit oder Diagnosegruppe auftauchen.
---

# Heilmittel-Domäne

Ohne diesen Skill erfindet jeder Agent plausibel klingende, aber falsche
Fachregeln. Das ist der teuerste Fehler in diesem Projekt: Falscher Code fällt
im Test auf, falsche Fachlogik nicht.

## Die harte Regel

> Jede Fachregel entsteht als **benannte Domänenregel** mit parametrisierten
> Tests. Nie als `if` mitten in einem Service.

Konkret heißt das: eine Klasse pro Regel, benannt wie die Regel heißt
(`BehandlungsbeginnFrist`, `UnterbrechungsFrist`, `FrequenzGrenze`), eine
Methode, die ein Ergebnis mit Begründung liefert — nicht nur `boolean`. Der
Grund: Wenn ein Slot abgelehnt wird, muss die Oberfläche sagen können,
*warum*. Ein nacktes `false` zwingt später zur Rekonstruktion.

## Zweite harte Regel: keine erfundenen Zahlen

Fristen ändern sich. Wenn eine Zahl nicht in `regeln.md` mit Quelle steht,
wird sie **nicht** in den Code geschrieben. Stattdessen: als offene Frage
markieren und nachfragen.

Für den Portfolio-Effekt zählt nicht, dass jede Frist tagesaktuell stimmt.
Es zählt, dass sie als benannte, parametrisierte, austauschbare Regel
modelliert ist — und dass die Quelle danebensteht.

**Seit ADR-007 ist das geprüft, nicht nur verabredet.** Jede Zeile in
`regeln.md` trägt eine ID, jede fachliche Konstante nennt sie:

```java
/** @fundstelle HM-FRIST-01 */
private static final int BEGINN_FRIST_TAGE = 28;
```

`scripts/regel-check.mjs` lehnt ab, was dagegen verstößt: eine nackte Zahl in
einer Regelklasse, eine ID, die es nicht gibt, oder eine ID mit Status
`UNSICHER`. Die vier unsicheren Regeln werden Parameter je Praxis, nicht
Konstanten — die Ausfallregel ist das Vorbild.

## Referenzdateien

- `regeln.md` — Regeltabelle mit Wert, Quelle und Status je Regel
- `glossar.md` — Vokabular der Domäne

Beide vor der Arbeit am Domänenmodell lesen.

## Das Kernproblem

Verfügbarkeit ist eine Schnittmenge über vier Dimensionen:

1. **Therapeut** — Arbeitszeit minus Abwesenheiten minus gebuchte Termine,
   gefiltert nach Qualifikation
2. **Raum** — Ausstattung, Belegung
3. **Patient** — Wunschfenster
4. **Verordnung** — Fristen, Frequenz, Restkontingent

Dazu **Rüstzeiten** zwischen Behandlungen und **Serientermine mit Ausnahmen**
(Feiertag, Krankmeldung, Einzelverschiebung).

## Die Grenzfälle, die dieses Projekt tragen

Diese Liste ist das eigentliche Vorzeigestück. Jeder Fall bekommt einen Test,
bevor die Regel implementiert wird:

- Überlappende Abwesenheiten desselben Therapeuten
- Termin exakt an der Frequenzgrenze (unten und oben)
- Verordnung läuft mitten in einer Serie ab
- Sommerzeitumstellung innerhalb eines Serientermins
- Rüstzeit kollidiert mit dem Folgetermin
- Raum verfügbar, Therapeut qualifiziert, aber Verordnungskontingent erschöpft
- Gleichzeitige Buchung desselben Slots durch zwei Mandanten

## Zeitrechnung

- Immer `java.time`, nie `Date` oder `Calendar`.
- Termine haben eine Zeitzone (`Europe/Berlin`). Fristen der Verordnung rechnen
  auf **Kalendertagen** (`LocalDate`), nicht auf Stunden.
- Sommerzeit: Eine Serie "jeden Dienstag 14:00" bleibt bei 14:00 Ortszeit. Wer
  in UTC rechnet und stur 168 Stunden addiert, verschiebt den Termin im
  Oktober um eine Stunde. Genau dafür gibt es den Testfall.

## Vokabular im Code

Fachbegriffe bleiben deutsch, auch als Bezeichner. `Verordnung`, nicht
`Prescription`. Grund: Die Übersetzung erzeugt eine zweite Sprache, in der die
Regeln nicht mehr wörtlich nachschlagbar sind, und genau das ist die Quelle
subtiler Fachfehler.
