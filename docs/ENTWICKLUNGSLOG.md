# Entwicklungslog

Beobachtungen zum Einsatz von KI-Agenten in diesem Projekt. Ehrlich, auch wo es
nicht gut aussieht.

Der Grund für dieses Dokument: consultingheads führt "nachweisbarer, effektiver
Einsatz von KI in der Softwareentwicklung" als Must-have. Ein Vermittler bekommt
zwanzig Profile, die das behaupten. Eine konkrete Beobachtung, wo Agenten
getragen haben und wo nicht, ist glaubwürdiger als jede Produktivitätszahl.

Regel für dieses Log: Einträge entstehen am selben Tag. Rückwirkend geschriebene
Beobachtungen sind Erinnerungen, und Erinnerungen bevorzugen die Fälle, die gut
ausgingen.

---

## 2026-09-09 — Stufe 0, Setup

**Was delegiert wurde:** Das gesamte Gerüst. Ordnerstruktur,
Anforderungs-Mapping aus den beiden Ausschreibungstexten, ADR-001, sechs Skills,
fünf Commands, zwei Subagents, der Beleg-Check und die CI. Zusätzlich als
eigener Auftrag an einen Subagenten die Quellenrecherche zur
Heilmittel-Richtlinie.

**Was gut lief.** Das Anforderungs-Mapping. Zwei Ausschreibungstexte in eine
Tabelle mit Status und Belegspalte zu überführen ist genau die Sorte Arbeit, die
von Hand eine Stunde dauert und langweilig genug ist, dass man sie verschiebt.
Ebenso die Skills: Sie bestehen aus Regeln, die ich formulieren kann, in einer
Struktur, die ich nicht jedes Mal neu erfinden will.

**Die Quellenrecherche war der klare Gewinn.** Ein Subagent hat die
Primärdokumente geladen und im Volltext ausgewertet: Heilmittel-Richtlinie,
Heilmittelkatalog, die Verträge nach § 125 SGB V für Physio- und Ergotherapie
samt Anlagen. Ergebnis sind rund siebzig belegte Regelzeilen mit Fundstelle und
Rechtsstand.

Wertvoller als die belegten Zeilen sind aber die sechs Widersprüche, die dabei
auffielen. Zwei Beispiele:

- Die Qualifikation für Manuelle Lymphdrainage steht in Anlage 1 mit 170
  Stunden, in der neuen Anlage 7 mit 140 Unterrichtseinheiten. Eine Zahl hätte
  man aus jeder einzelnen Quelle mit voller Überzeugung übernommen.
- Für das verbreitete Ausfallhonorar mit 24-Stunden-Frist gibt es überhaupt
  keine sozialrechtliche Grundlage. Es ist eine zivilrechtliche Vereinbarung.
  Ein Modell, das die 24 Stunden fest verdrahtet, wäre fachlich falsch.

Beide Erkenntnisse haben das Domänenmodell verändert, bevor die erste Zeile Code
existiert: Ausfallfrist und Rüstzeit werden Parameter je Praxis, keine
Konstanten.

**Was auffiel.** Der Agent wollte an mehreren Stellen konkrete Fristen
einsetzen, bevor die Recherche zurück war. Plausible Zahlen, keine Quelle. Genau
der Fehler, vor dem der Skill `heilmittel-domain` warnt, und er trat schon beim
Anlegen dieses Skills auf.

Gegenmaßnahme: `regeln.md` startete leer mit Status OFFEN, und die Regel "keine
Zahl ohne Quelle" steht ganz oben im Skill. Erst nach der Recherche wurden
Zahlen eingetragen, jede mit Fundstelle und Status.

Das ist der wichtigste Befund aus Stufe 0. Falscher Code fällt im Test auf,
falsche Fachlogik nicht.

**Was nicht funktionierte.** Zwei handwerkliche Fehler beim Schreiben der
Dateien:

- Mehrere Dateien entstanden mit Umschreibungen wie "fuer" statt "für". Die
  Korrektur per Wörterbuch ersetzte anschließend einen Java-Methodennamen mit,
  aus `pruefe` wurde `prüfe`. Gefunden beim Nachlesen, nicht durch eine Prüfung.
  Wäre es Produktivcode gewesen, hätte der Compiler es gefangen. In einer
  Markdown-Datei fängt es niemand.
- Eine Datei fiel bei der ersten Korrektur durchs Raster, weil die Dateiliste
  von Hand zusammengestellt war statt aus dem Verzeichnisbaum.

Beides sind Fehler derselben Art: Eine Massenoperation ohne Prüfung des
Ergebnisses. Genau der Fall, für den der Beleg-Check gedacht ist.

**Offen:** Java ist auf dieser Maschine noch nicht installiert. Vor Stufe 1
nachzuholen: ein JDK 21 und die Entscheidung zwischen Gradle und Maven.

---

## Vorlage für weitere Einträge

```
## JJJJ-MM-TT — Stufe N, Thema

**Was delegiert wurde:**
**Was gut lief:**
**Was nicht funktionierte:**
**Was die Testsuite abgefangen hat:**  (der wertvollste Abschnitt)
**Zeitschätzung:** delegiert gegen von Hand geschätzt
```

Der Abschnitt "was die Testsuite abgefangen hat" ist der, auf den es ankommt. Er
belegt, dass die CI tatsächlich als Guardrail für agentische Änderungen wirkt
und nicht nur so genannt wird.
