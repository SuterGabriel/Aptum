# Produktsicht

Dieses Dokument beantwortet die Fragen, die vor der Architektur kommen: für wen
das gebaut wird, welcher Teil des Problems zuerst gelöst wird und was bewusst
nicht gebaut wird.

Es ist in beiden Ausschreibungen als Beleg benannt (SA8, CA5, C11) und steht
deshalb hier, statt im Kopf zu bleiben.

**Vorab, damit die Grenze klar ist:** Dieses Projekt hat keine echten Nutzer und
keinen echten Auftraggeber. Was hier als Produktarbeit steht, ist der *Prozess*
— Anforderung, Schnitt, Priorisierung, Nicht-Ziel, Erfolgskriterium —
angewendet auf eine reale Domäne mit belegten Regeln. Die Bedarfe sind aus den
Quellen und der Rechtslage hergeleitet, nicht aus Interviews. Wo eine Annahme
ungeprüft ist, steht das dabei.

---

## 1. Für wen

Vier Rollen in einer Physio- oder Ergotherapiepraxis. Sie unterscheiden sich
nicht durch Berechtigungen, sondern durch die Frage, die sie an das System
stellen.

| Rolle | Die Frage, die sie stellt | Was sie unter einem guten Tag versteht |
|---|---|---|
| **Rezeption** | „Wann kann Herr X das nächste Mal kommen?" | Keine Rückfrage an die Therapeutin nötig, kein Blättern durch Papierrezepte. |
| **Therapeutin / Therapeut** | „Was steht heute an und ist die Verordnung noch gültig?" | Kein Termin, der abgesagt werden muss, weil eine Frist gerissen ist. |
| **Praxisleitung** | „Welche Verordnungen laufen in den nächsten zwei Wochen ab?" | Keine Behandlung, die am Ende nicht abgerechnet werden kann. |
| **Abrechnung** | „Welche Einheiten sind erbracht und absetzungssicher?" | Keine Absetzung durch die Krankenkasse. |

Der gemeinsame Nenner: **Alle vier stellen im Kern dieselbe Frage — ist diese
Behandlung zu diesem Zeitpunkt regelkonform?** Nur zu vier verschiedenen
Zeitpunkten. Das ist der Grund, warum die Regelprüfung ein Domänenkern ist und
kein Formularvalidator.

## 2. Der Schmerz, den das Produkt adressiert

Nicht „Termine verwalten". Termine verwalten kann ein Kalender.

Der Schmerz ist, dass ein Termin **nachträglich wertlos werden kann**. Eine
Behandlung wird durchgeführt, dokumentiert, abgerechnet — und später abgesetzt,
weil die Verordnung zwischen zwei Terminen 15 statt 14 Tage Pause hatte und der
Grund nicht auf dem Blatt vermerkt war. Die Arbeit ist geleistet, das Geld kommt
nicht.

Die Regeln, die darüber entscheiden, sind einzeln simpel und in Kombination
nicht im Kopf zu halten. Sie stehen in der Heilmittel-Richtlinie und den
Verträgen nach § 125 SGB V, belegt in
[`.claude/skills/heilmittel-domain/regeln.md`](../.claude/skills/heilmittel-domain/regeln.md).

**Die Produktentscheidung, die daraus folgt:** Das System verhindert den Fehler
zum Zeitpunkt der Buchung, statt ihn hinterher zu berichten. Ein Warnhinweis in
einer Auswertung kommt zu spät — die Behandlung hat dann stattgefunden.

## 3. Was zuerst gebaut wird und warum

Priorisiert nach einer Frage: *Welcher Teil trägt das meiste fachliche Risiko?*
Nicht nach dem, was am schnellsten sichtbar ist.

| Rang | Fähigkeit | Begründung |
|---|---|---|
| 1 | Verordnung mit Fristen-, Unterbrechungs- und Mengenprüfung | Hier liegt das Absetzungsrisiko. Ohne diesen Kern ist der Rest eine Terminliste. |
| 2 | Slot-Berechnung über Therapeut, Raum, Patient, Verordnung | Die eigentliche Rechenleistung. Vier Dimensionen, nicht eine Kalenderlücke. |
| 3 | Kalender-Grid und Suchflow | Macht 1 und 2 bedienbar. Vorher hat es nichts anzuzeigen. |
| 4 | Serientermine mit Ausnahmen | Häufigster Praxisfall, aber nur sinnvoll, wenn die Einzelbuchung sicher ist. |
| 5 | AI-Assistenz für die Verordnungserfassung | Nimmt Tipparbeit ab. Setzt voraus, dass die Domäne jeden Vorschlag prüfen kann. |
| 6 | Abrechnungsübersicht | Lesende Sicht auf bereits korrekte Daten. |

Der Reihenfolge liegt eine bewusste Unbequemlichkeit zugrunde: Das Sichtbare
(Kalender) kommt nach dem Unsichtbaren (Regelkern). Bei einem Portfolio-Projekt
ist das die teurere Reihenfolge, weil es länger nichts zu zeigen gibt. Sie ist
trotzdem richtig, weil ein Kalender-Grid über einem unsicheren Regelkern genau
den Eindruck erweckt, den dieses Projekt widerlegen soll.

## 4. Wo die AI-Assistenz sitzt — und wo nicht

**Sie sitzt beim Erfassen.** Ein abfotografiertes Muster-13-Formular in
strukturierte Felder überführen: Heilmittel, Anzahl, Frequenz, Diagnosegruppe,
Leitsymptomatik. Das ist Fleißarbeit mit hoher Fehlerquote von Hand und eine
Aufgabe, bei der ein falscher Vorschlag sofort auffällt, weil ein Mensch ihn
gegen das Blatt prüft.

**Sie sitzt nicht beim Entscheiden.** Kein Modell beantwortet die Frage, ob eine
Verordnung noch gültig ist. Diese Frage hat eine deterministische Antwort aus
einer Regel mit Fundstelle. Ein Modell, das sie zu 97 % richtig beantwortet, ist
in dieser Domäne unbrauchbar — die 3 % sind abgesetzte Rechnungen und im
Zweifel ein Haftungsfall.

Das ist Regel 3 aus [CLAUDE.md](../CLAUDE.md), hier aus der Produktperspektive
begründet statt aus der Architekturperspektive.

## 5. Nicht-Ziele

Bewusst nicht gebaut, damit der Umfang nicht ausfranst:

- **Keine Dokumentation der Behandlungsinhalte.** Therapieberichte, Befunde,
  Verlaufsdokumentation. Angrenzende Domäne, eigener Regelkreis, verdoppelt den
  Umfang ohne neuen Erkenntnisgewinn.
- **Keine echte Kassenabrechnung.** Kein Datenträgeraustausch nach § 302 SGB V,
  keine Zertifikate, keine Schnittstelle zu Abrechnungsdienstleistern. Die
  Abrechnungssicht endet bei „diese Einheiten sind erbracht und prüffest".
- **Keine Patienten-App.** Online-Terminbuchung durch Patienten wäre ein
  eigenes Produkt mit eigenen Datenschutzfragen.
- **Kein Podologie-, Logopädie- oder Ernährungstherapie-Zweig.** Physio und Ergo
  reichen: Sie unterscheiden sich in den Unterbrechungsregeln genug, um die
  Asymmetrie im Modell zu erzwingen. Ein dritter Zweig fügt Datenpflege hinzu,
  keine neue Regelstruktur.
- **Keine Migration von Bestandsdaten.** In der Ausschreibung als SA2 benannt
  und in [ANFORDERUNGEN.md](ANFORDERUNGEN.md) offen als nicht abgebildet geführt.

## 6. Woran sich Erfolg messen ließe

Ein Portfolio-Projekt hat keine Nutzungszahlen. Diese Kriterien sind deshalb als
das formuliert, was sie sind: **die Messgrößen, die man in einer echten
Einführung erheben würde**, daneben das, was hier tatsächlich prüfbar ist.

| Was gemessen würde | Wie es hier prüfbar ist |
|---|---|
| Anteil Behandlungen, die wegen Formfehlern abgesetzt werden | Parametrisierte Tests je Regel, inklusive der Grenzfälle 14/15 Tage und 28/14 Tage |
| Zeit von der Anfrage bis zum bestätigten Termin | Antwortzeit der Slot-Suche unter Testlast |
| Anteil Verordnungen, die vor Ablauf auffallen | Fristenmonitor mit Testfällen je Vorlaufzeit |
| Erfassungsdauer je Verordnung, mit und ohne AI | Eval-Suite mit feldweiser Genauigkeit, siehe Skill `llm-evals` |
| Bedienbarkeit ohne Maus | axe-Test und Tastaturpfad über das Kalender-Grid in der CI |

Die letzte Zeile ist keine Kür. In einer Praxis wird der Kalender im Stehen,
zwischen zwei Patienten, mit einer Hand bedient. Tastaturbedienbarkeit ist dort
Arbeitsgeschwindigkeit, bevor sie Barrierefreiheit ist.

## 7. Wie eine Anforderung hier durch das System läuft

Der Prozess, nicht das Ergebnis — das ist der Teil, der auf CA5 und C11
antwortet:

```
Ausschreibungstext  →  docs/ANFORDERUNGEN.md    wörtlich, mit Status und Beleg
        ↓
Fachliche Klärung   →  Skill heilmittel-domain  Regel mit Fundstelle, oder Status OFFEN
        ↓
Entscheidung        →  docs/adr/ADR-NNN         Optionen, Konsequenzen, Revisionsbedingung
        ↓
Umsetzung           →  benannte Domänenregel    parametrisierte Tests für die Grenzfälle
        ↓
Rückkopplung        →  scripts/beleg-check.sh   die Behauptung geht kaputt, wenn der Beleg fehlt
```

Der letzte Schritt ist der ungewöhnliche. Er sorgt dafür, dass dieses Dokument
nicht unbemerkt veralten kann: Sobald in einer Aufgaben-Tabelle etwas als
Abbildung behauptet wird, das im Repo nicht existiert, wird die CI rot.

## 8. Offene Produktfragen

Ehrlich offen, nicht rhetorisch:

- **Wie streng darf das System sein?** Eine Buchung, die gegen eine Regel
  verstößt: blockieren oder warnen und dokumentieren lassen? Es gibt legitime
  Fälle für die Übersteuerung. Aktuelle Annahme: blockieren mit
  begründungspflichtiger Übersteuerung — ungeprüft, gehört in eine ADR, sobald
  die erste Regel gebaut wird.
- **Ausfallregel als Praxiseinstellung.** Fachlich zwingend, weil es keine
  sozialrechtliche Grundlage gibt (siehe [OFFENE-PUNKTE.md](OFFENE-PUNKTE.md),
  Punkt 5). Offen ist, wie viele weitere Parameter dieselbe Behandlung brauchen,
  bevor die Konfiguration selbst zum Problem wird.
- **Mockups.** In [docs/mockups/](mockups/) liegt bisher nur der
  die beiden Design-Prompts ([Wireframes](mockups/PROMPT-WIREFRAMES.md), [Mockup](mockups/PROMPT-MOCKUP.md)) — der Auftrag, noch nicht das
  Ergebnis. Der Kalender-Schnitt gehört visuell festgehalten, bevor er gebaut
  wird; offen bis Stufe 2.
