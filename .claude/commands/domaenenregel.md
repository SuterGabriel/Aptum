---
description: Legt eine neue Fachregel als benannte Domänenklasse mit parametrisierten Tests an
argument-hint: <Beschreibung der Regel>
---

Lege die folgende Fachregel als benannte Domänenregel an: $ARGUMENTS

Gehe in dieser Reihenfolge vor:

1. **Lies zuerst den Skill `heilmittel-domain`**, insbesondere `regeln.md`.
   Wenn die Regel dort nicht mit Status BELEGT und Quelle steht, schreibe
   **keine Zahl** in den Code. Frage stattdessen nach oder trage die offene
   Frage in `regeln.md` ein.

2. **Schreibe die Tests zuerst.** Eine parametrisierte Testmethode mit den
   Grenzfällen: exakt an der Grenze, knapp darunter, knapp darüber, und der
   leere beziehungsweise entartete Fall. Nenne jeden Fall so, dass der Name die
   fachliche Situation beschreibt, nicht die Zahlen.

3. **Dann die Regelklasse** unter `domain/regel/`. Reines Java: keine
   Spring-Annotation, keine JPA, keine Abhängigkeit außerhalb des
   Domain-Moduls. Rückgabe ist ein Pruefergebnis mit Begründung, kein
   nacktes boolean, damit die Oberfläche sagen kann, warum ein Slot nicht geht.

4. **Registriere die Regel** an der Stelle, an der die Slot-Berechnung ihre
   Regeln einsammelt. Eine Regel, die niemand aufruft, ist schlimmer als keine.

5. **Ergänze `regeln.md`** um die Zeile mit Wert, Quelle und Status.

6. **Erkläre mir die Regel in zwei Sätzen.** Wenn das nicht geht, ist sie zu
   groß geschnitten und gehört aufgeteilt.

Fasse am Ende zusammen, welche Grenzfälle du getestet hast und welche du
bewusst nicht abgedeckt hast.
