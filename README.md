# Aptum

Terminplanung und Verordnungsverwaltung für Physio- und Ergotherapiepraxen.

*Aptum*, lateinisch für „passend". Die Anwendung beantwortet eine einzige
Frage: Passt dieser Termin zu Therapeut, Raum, Patient **und** Verordnung? Die
vier Dimensionen unten sind der Grund für den Namen.

**Stand: Domänenkern steht, Stufe 1 in Arbeit.** Alle vier Dimensionen sind
als benannte Regeln mit parametrisierten Grenzfalltests modelliert, und die
Slot-Suche schneidet sie: Sie liefert Vorschläge mit ihrem Prüfbericht und
zählt, was sie aus welchem Grund weggelassen hat. Dasselbe Regelwerk, das die
Suche befragt, prüft später jede Buchung von Hand und jeden Vorschlag eines
Sprachmodells — es gibt keinen zweiten Weg. Das Spring-Boot-Gerüst steht in
drei Modulen, die Persistenz trennt Mandanten per Row Level Security und
beweist das gegen ein echtes Postgres (ADR-002). Zwei REST-Endpunkte suchen und buchen
durch dasselbe Regelwerk — nachspielbar mit `curl`. Das Angular-Frontend ist
offen.

Vorgezogen aus Stufe 2 sind die Gestaltungstoken und die Kontrastprüfung
(ADR-006), weil der Auftrag an die Gestaltung eine Grundlage brauchte. Was
hier behauptet wird, ist an der jeweiligen Stelle im Repo nachprüfbar oder als
offen gekennzeichnet.

---

## Das Domänenproblem

Ein freier Termin ist keine Lücke im Kalender. Er ist die Schnittmenge über vier
Dimensionen:

1. **Therapeut** — Arbeitszeit minus Abwesenheiten minus gebuchte Termine,
   gefiltert nach Qualifikation. Manuelle Lymphdrainage darf nicht jeder
   abrechnen.
2. **Raum** — Ausstattung und Belegung. Krankengymnastik am Gerät braucht einen
   Bereich von mindestens 30 m² mit vier Pflichtgeräten.
3. **Patient** — Wunschfenster.
4. **Verordnung** — Fristen, Frequenz, Restkontingent.

Dazu Rüstzeiten zwischen Behandlungen, Nachruhezeiten, die den Raum blockieren
aber nicht die Therapeutin, und Serientermine mit Ausnahmen für Feiertage,
Krankmeldungen und Einzelverschiebungen.

Die Regeln sind nicht erfunden. Sie stehen in der Heilmittel-Richtlinie und in
den Verträgen nach § 125 SGB V. Zwei Beispiele, an denen sichtbar wird, warum
das kein CRUD ist:

- Eine Verordnung verfällt, wenn die Behandlung nicht innerhalb von 28
  Kalendertagen nach Ausstellung beginnt, bei gekennzeichnetem dringlichem
  Bedarf innerhalb von 14.
- Eine Unterbrechung von mehr als 14 Kalendertagen lässt die Verordnung
  verfallen, außer sie ist begründet. In der Ergotherapie summieren sich
  begründete Unterbrechungen auf maximal 70 Tage. In der Physiotherapie gibt es
  diese Summengrenze nicht, dafür verfällt die Verordnung dort absolut nach drei
  oder sechs Monaten ab dem ersten Behandlungstag.

Diese Asymmetrie zwischen zwei Therapieformen in derselben Praxis ist genau die
Art Fachlogik, die sich nicht in ein Formular schreiben lässt.

Belegt und mit Fundstellen: [`.claude/skills/heilmittel-domain/regeln.md`](.claude/skills/heilmittel-domain/regeln.md)

## Die Architekturregel

> Der AI-Layer schlägt vor, die Domäne entscheidet.

Kein Vorschlag eines Sprachmodells wird gebucht, ohne dieselbe deterministische
Regelprüfung zu durchlaufen wie eine manuelle Buchung. In dieser Domäne ist ein
Modell nie die letzte Instanz.

## Was hier nachprüfbar ist

Dieses Repo behauptet eine Arbeitsweise. Der CI-Job
[`beleg-check`](scripts/beleg-check.sh) macht aus jeder Behauptung eine Zusage,
die kaputtgehen kann: Er prüft bei jedem Lauf, dass die Anforderungen erfasst
sind, jede Entscheidung als ADR mit Alternativen und Konsequenzen vorliegt, die
Skills im Repo statt in einer globalen Konfiguration liegen, und der
KI-Einsatz protokolliert wird.

Fehlt ein Beleg, wird die CI rot. Entweder der Beleg wird nachgeliefert, oder
die Behauptung gestrichen.

Daneben stehen vier weitere Gates: die Kontrastprüfung aus den Farbtoken, ein
Verweis-Check über die Markdown-Querverweise, eine Prosa-Prüfung gegen
ausgeschriebene Umlaute und der Regel-Check, der keine Fachzahl ohne belegte
Fundstelle in den Domänencode lässt. Alle fünf laufen auch vor dem Commit auf
der eigenen Maschine. Wie sie zugeschnitten sind und was sie *nicht* abfangen, steht in
[docs/PIPELINE.md](docs/PIPELINE.md).

## Wegweiser

| Datei | Inhalt |
|---|---|
| [docs/ANFORDERUNGEN.md](docs/ANFORDERUNGEN.md) | Die Anforderungen wörtlich, mit Status und Beleg. Auch die Punkte, die nicht belegbar sind. |
| [docs/PRODUKT.md](docs/PRODUKT.md) | Für wen gebaut wird, was zuerst kommt, was bewusst nicht gebaut wird |
| [DECISIONS.md](DECISIONS.md) | Übersicht der Architekturentscheidungen |
| [docs/adr/](docs/adr/) | Die Entscheidungen im Volltext, mit Alternativen und dem Abschnitt "wann wir anders entscheiden würden" |
| [docs/PIPELINE.md](docs/PIPELINE.md) | Die Gates zwischen einer Änderung und `main` — und was sie nicht abfangen |
| [docs/ENTWICKLUNGSLOG.md](docs/ENTWICKLUNGSLOG.md) | Beobachtungen zum KI-Einsatz, ehrlich auch da wo es nicht gut aussieht |
| [docs/DATENSCHUTZ.md](docs/DATENSCHUTZ.md) | Regeln für Testdaten und was in einer echten Anwendung zu klären wäre |
| [CLAUDE.md](CLAUDE.md) | Die Projektregeln auf einer Bildschirmseite |
| [.claude/](.claude/) | Skills, Commands und Subagents, versioniert im Repo |

## Stand der Stufen

| Stufe | Inhalt | Stand |
|---|---|---|
| 0 | Setup, Regeln, Skills, CI-Grundgerüst, ADR-001 | **erledigt** |
| 1 | Domänenkern in reinem Java, dann Spring, Multi-Tenancy, Angular-Suchflow | **in Arbeit** — Domänenkern mit Slot-Suche steht, Spring-Boot-Gerüst startet, Persistenz und Frontend offen |
| 2 | Kalender-Grid, Tabelle, WCAG, End-to-End-Tests | offen |
| 3 | AI-Layer, MCP-Server, Evals, Provider-Vergleich | offen |
| 4 | Terraform, Helm, ArgoCD, Observability | offen |
| 5 | Demo, ADRs vervollständigen, Mapping | offen |

## Daten

Ausschließlich klar erkennbare Testdaten. Keine realistisch aussehenden
Patientennamen, keine echten Verordnungen. Details in
[docs/DATENSCHUTZ.md](docs/DATENSCHUTZ.md).
