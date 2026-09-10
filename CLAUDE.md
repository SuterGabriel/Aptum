# Aptum

Terminplanung und Verordnungsverwaltung für Physio- und Ergotherapiepraxen.
Portfolio-Projekt. Alles Agent-bezogene liegt versioniert im Repo — es *ist*
Teil des Ergebnisses, nicht nur das Werkzeug.

## Die vier Regeln

1. **Fachregeln sind benannte Domänenregeln.** Nie ein `if` mitten im Service.
   Jede Regel bekommt parametrisierte Tests für ihre Grenzfälle.
2. **Die Domäne kennt kein Framework.** Kein Spring, keine Annotation, keine
   JPA im Domain-Modul. ArchUnit prüft das bei jedem Lauf.
3. **Der AI-Layer schlägt vor, die Domäne entscheidet.** Kein LLM-Vorschlag
   wird gebucht, ohne dieselbe deterministische Regelprüfung zu durchlaufen
   wie eine manuelle Buchung.
4. **Was nicht in zwei Sätzen erklärbar ist, fliegt raus oder wird
   verstanden.** Gilt besonders für generierten Java-Code.

## Entscheidungen

ADRs entstehen **im Moment der Entscheidung**, nicht rückwirkend. Format und
Ablauf: Skill `adr`, Command `/adr`. Übersicht in `DECISIONS.md`.

## Daten

Ausschließlich klar erkennbare Testdaten. Keine realistischen Patientennamen.
Pseudonymisierung vor jedem LLM-Aufruf. Details: `docs/DATENSCHUTZ.md`.

## Sprache

Fachbegriffe der Domäne auf Deutsch (Verordnung, Heilmittel, Frequenz,
Rüstzeit) — auch im Code. Technische Bezeichner auf Englisch. Dokumentation
auf Deutsch, weil die Leser deutschsprachig sind.

## Skills

Alles Längere als eine Bildschirmseite gehört in einen Skill, nicht hierher.

| Skill | Wofür |
|---|---|
| `heilmittel-domain` | Fachregeln, Fristen, Glossar. **Vor jeder Domänenänderung lesen.** |
| `java-spring-hexagonal` | Wo Code hingehört, Ports, Negativbeispiele |
| `angular-rxjs` | Hausstil für Streams und Signals |
| `a11y-grid` | Tastaturnavigation, ARIA, Live-Regions |
| `adr` | Format und Ablauf für Entscheidungen |
| `llm-evals` | Testfälle, Metriken, Regressionslauf |

## Commands

`/domaenenregel` · `/adr` · `/a11y-audit` · `/eval` · `/anforderungs-mapping`

## Was jede Änderung erfüllen muss

Formatiert · Domain-Tests grün · ArchUnit grün · bei UI-Änderung ein
axe-Test · bei Prompt-Änderung ein Eval-Lauf · Commit erklärt die
Entscheidung, nicht die Zeilen.
