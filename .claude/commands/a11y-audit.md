---
description: Prüft eine Komponente auf Barrierefreiheit mit Playwright, axe und Tastaturnavigation
argument-hint: <Komponente>
---

Prüfe die Barrierefreiheit von: $ARGUMENTS

1. **Lies den Skill `a11y-grid`.**

2. **Automatisierter Teil:** Schreibe oder erweitere einen Playwright-Test, der
   axe-core gegen die Komponente laufen lässt. Verstöße werden nicht
   unterdrückt. Wenn ein Verstoß bewusst akzeptiert wird, gehört die
   Begründung als Kommentar direkt daneben.

3. **Tastaturteil, den axe nicht abdeckt** - jeder Punkt wird tatsächlich
   durchgespielt, nicht nur behauptet:
   - Erreicht man jedes Bedienelement mit der Tabulatortaste?
   - Ist die Fokusreihenfolge die visuelle Reihenfolge?
   - Ist der Fokus jederzeit sichtbar, auch auf farbigem Grund?
   - Bei einem Grid: genau eine Zelle mit tabindex 0, Pfeilnavigation, Home und
     End, Steuerungstaste mit Home und End?
   - Bei einem Dialog: Focus Trap, Fokus beim Öffnen gesetzt, beim Schließen
     zurück auf den Auslöser?
   - Gibt es eine Tastaturfalle, aus der man nicht herauskommt?

4. **Semantik:** Hat jedes interaktive Element einen zugänglichen Namen, der
   Zustand und Bedeutung nennt? Wird nach Aktionen eine Live-Region aktualisiert?

5. **Farbe:** Ist irgendeine Information ausschließlich über Farbe kodiert?
   Rechne die Kontrastwerte aus den Farbtoken nach, schätze sie nicht.

Berichte die Befunde als Liste, die schwerwiegendsten zuerst, jeweils mit der
konkreten Datei und Zeile. Behebe sie erst, wenn ich es sage.
