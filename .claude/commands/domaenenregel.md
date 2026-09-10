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

   **Notiere die IDs**, auf die sich die Regel stützt — `HM-FRIST-01` und so
   fort. Jede fachliche Konstante nennt sie später als `@fundstelle`. Steht
   die Zeile auf `UNSICHER`, wird der Wert ein Parameter je Praxis und keine
   Konstante. Das ist ADR-007, und `scripts/regel-check.mjs` prüft es.

2. **Schreibe die Tests zuerst.** Eine parametrisierte Testmethode mit den
   Grenzfällen: exakt an der Grenze, knapp darunter, knapp darüber, und der
   leere beziehungsweise entartete Fall. Nenne jeden Fall so, dass der Name die
   fachliche Situation beschreibt, nicht die Zahlen.

3. **Dann die Regelklasse** unter `domain/regel/`. Reines Java: keine
   Spring-Annotation, keine JPA, keine Abhängigkeit außerhalb des
   Domain-Moduls. Rückgabe ist ein Pruefergebnis mit Begründung, kein
   nacktes boolean, damit die Oberfläche sagen kann, warum ein Slot nicht geht.

   Keine nackte Zahl in der Klasse. Jeder fachliche Wert ist eine benannte
   Konstante mit der Fundstelle darüber:

   ```java
   /** @fundstelle HM-FRIST-01 */
   private static final int BEGINN_FRIST_TAGE = 28;
   ```

4. **Registriere die Regel** an der Stelle, an der die Slot-Berechnung ihre
   Regeln einsammelt. Eine Regel, die niemand aufruft, ist schlimmer als keine.

5. **Ergänze `regeln.md`** um die Zeile mit ID, Wert, Quelle und Status. Die
   ID ist die nächste freie im jeweiligen Bereich und wird nie neu vergeben.

6. **Erkläre mir die Regel in zwei Sätzen.** Wenn das nicht geht, ist sie zu
   groß geschnitten und gehört aufgeteilt.

Fasse am Ende zusammen, welche Grenzfälle du getestet hast und welche du
bewusst nicht abgedeckt hast.
