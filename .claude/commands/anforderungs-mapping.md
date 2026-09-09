---
description: Gleicht docs/ANFORDERUNGEN.md mit dem tatsächlichen Stand des Repos ab
---

Gleiche `docs/ANFORDERUNGEN.md` mit dem tatsächlichen Stand des Repos ab.

Diese Tabelle ist das Dokument, das ein Recruiter tatsächlich liest. Sie
aktuell zu halten ist wichtiger als jede Zeile Produktivcode.

Gehe Zeile für Zeile durch beide Ausschreibungstabellen:

1. **Prüfe jeden Beleg.** Existiert die genannte Datei oder der genannte
   Ordner? Enthält er tatsächlich, was die Zeile behauptet? Ein leerer Ordner
   belegt nichts.
2. **Aktualisiere den Status** auf offen, in Arbeit, belegt oder nicht belegbar.
3. **Präzisiere die Belegspalte** auf die konkrete Datei, wenn inzwischen eine
   existiert. `services/` ist ein schwächerer Beleg als der Pfad zur
   Regelklasse mit ihren Tests.

**Die wichtigste Regel dieses Commands:** Stufe niemals etwas auf "belegt" hoch,
was du nicht im Repo gesehen hast. Lieber eine Zeile zu vorsichtig als eine
Behauptung, die im Gespräch auffliegt. Das ganze Projekt baut auf
Nachprüfbarkeit auf, eine geschönte Zeile beschädigt genau das Argument, das
es tragen soll.

Berichte am Ende drei Listen:

- Zeilen, die du hochgestuft hast, mit dem Beleg
- Zeilen, die du herabgestuft hast, weil der Beleg nicht trägt
- Belege im Repo, die zu keiner Anforderung gehören. Das ist entweder eine
  fehlende Zeile in der Tabelle oder Arbeit, die niemand angefordert hat.
