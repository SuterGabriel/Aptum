---
description: Lässt die Eval-Suite gegen die aktuellen Prompts laufen und vergleicht mit dem letzten Lauf
---

Führe die Eval-Suite aus und vergleiche das Ergebnis mit dem letzten Lauf.

1. **Lies den Skill `llm-evals`.**
2. **Führe `evals/run.py` aus** gegen die aktuellen Prompts.
3. **Berichte feldweise**, nicht als Gesamtnote. Eine Gesamtgenauigkeit von 91
   Prozent sagt nichts, "Frequenz 98 Prozent, Diagnosegruppe 74 Prozent" sagt,
   wo gearbeitet werden muss.
4. **Vergleiche mit dem letzten Lauf.** Wichtiger als die absolute Zahl sind
   die Fälle, die vorher grün waren und jetzt rot sind. Nenne sie einzeln mit
   ihrer ID und der Begründung aus dem Feld `warum`.
5. **Wenn ein Fall neu gescheitert ist:** Zeige die erwartete und die
   tatsächliche Ausgabe nebeneinander. Repariere nichts von selbst, eine
   Prompt-Änderung, die einen Fall repariert, bricht typischerweise einen
   anderen.

Falls die Suite gegen beide Provider läuft, weise die Fälle aus, bei denen
sich die Provider unterscheiden. Das sind die Stellen, an denen der Prompt zu
schwach ist statt das Modell zu schlecht.
