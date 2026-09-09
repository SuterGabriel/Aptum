---
description: Erstellt einen ADR-Entwurf aus dem aktuellen Diff und der getroffenen Entscheidung
argument-hint: <Thema der Entscheidung>
---

Erstelle einen ADR-Entwurf zum Thema: $ARGUMENTS

1. **Lies den Skill `adr`** für Format und Ablauf.
2. **Sieh dir den aktuellen Diff an** (`git diff` und `git diff --staged`), um
   zu erkennen, was tatsächlich entschieden wurde.
3. **Vergib die nächste freie Nummer** aus `docs/adr/`. Nummern werden nie neu
   vergeben.
4. **Schreibe den Entwurf** nach `docs/adr/ADR-NNN-kurzer-titel.md`.

Zwei Dinge, die diesen Entwurf von einem generischen unterscheiden:

- **Mindestens eine verworfene Option muss gut klingen.** Wenn alle Alternativen
  offensichtlich schlecht wirken, war es keine Entscheidung, und die ADR ist
  wertlos. Suche den echten Vorteil der Alternative.
- **Der Abschnitt "Wann wir anders entscheiden würden" ist nie leer.** Er ist
  der Abschnitt, den Interviewer lesen.

Ergänze anschließend die Tabelle in `DECISIONS.md`.

Markiere alles, was du aus dem Diff nur vermutet hast, deutlich als Annahme.
Ich prüfe den Entwurf, bevor er angenommen wird. Setze den Status auf
"vorgeschlagen", nicht auf "angenommen".
