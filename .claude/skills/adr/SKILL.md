---
name: adr
description: Format und Ablauf für Architecture Decision Records in diesem Projekt - Kontext, Optionen, Entscheidung, Konsequenzen und der Abschnitt wann wir anders entscheiden würden. Nutze diesen Skill immer wenn eine Architekturentscheidung getroffen wird, wenn nach einer ADR gefragt wird, beim Command /adr und wenn eine bestehende Entscheidung revidiert werden soll.
---

# Architecture Decision Records

## Der Zeitpunkt ist die halbe Sache

Eine ADR entsteht **im Moment der Entscheidung**, nicht am Ende des Projekts.
Wer sie rückwirkend schreibt, rekonstruiert Begründungen, die er nie hatte,
und das merkt man ihnen an: Die Optionen, die nicht gewählt wurden, klingen
dann alle offensichtlich schlecht. In einer ehrlichen ADR klingt mindestens
eine verworfene Option gut.

## Das Format

Datei: `docs/adr/ADR-NNN-kurzer-titel.md`. Nummern werden nie neu vergeben.

```markdown
# ADR-NNN: Titel

- **Status:** vorgeschlagen | angenommen | abgelöst durch ADR-MMM
- **Datum:** JJJJ-MM-TT
- **Stufe:** in welcher Projektstufe

## Kontext

Was ist die Situation, die eine Entscheidung erzwingt. Welche Kräfte wirken
gegeneinander. Ohne Lösung, nur das Problem.

## Optionen

Mindestens zwei, realistisch dargestellt. Jede mit ihrem echten Vorteil, nicht
als Strohmann.

## Entscheidung

Welche Option, und woran sie im Repo erkennbar ist.

## Konsequenzen

Getrennt nach positiv und negativ. Der negative Teil ist der wichtigere.
Eine ADR ohne Nachteile ist keine Entscheidung, sondern eine Werbung.

## Wann wir anders entscheiden würden

Unter welchen geänderten Bedingungen die Entscheidung falsch wäre.
```

## Der letzte Abschnitt

"Wann wir anders entscheiden würden" ist der Abschnitt, den Interviewer lesen.
Er zeigt, ob jemand eine Entscheidung als Abwägung unter Bedingungen versteht
oder als Glaubenssatz. Er ist nie leer. Wenn dir nichts einfällt, war es keine
Entscheidung, sondern eine Selbstverständlichkeit, und dann braucht sie keine
ADR.

## Nach dem Schreiben

Die Tabelle in `DECISIONS.md` ergänzen. Der Beleg-Check in der CI prüft, dass
jede ADR die vier Pflichtabschnitte hat.

## Was keine ADR braucht

Bibliotheksversionen, Formatierungsregeln, Benennungskonventionen. Die gehören
in `CLAUDE.md` oder in einen Skill. Eine ADR beschreibt eine Entscheidung, die
teuer rückgängig zu machen ist.
