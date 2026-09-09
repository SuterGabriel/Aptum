# Datenschutz

In dieser Domäne ist Datenschutz kein Anhang. Dass die Frage von selbst
gestellt wird, zählt in Healthcare mehr als jedes Feature.

## Regeln für dieses Repository

1. **Ausschließlich klar erkennbare Testdaten.** Keine realistisch aussehenden
   Patientennamen. Namen sind erkennbar synthetisch, Geburtsdaten liegen
   außerhalb plausibler Bereiche, Versichertennummern folgen keinem echten
   Format.
2. **Pseudonymisierung vor jedem LLM-Aufruf.** Direkt identifizierende Angaben
   werden ersetzt, bevor die Anfrage das System verlässt, und nach der Antwort
   zurückgesetzt. Das ist ein benannter Schritt in der AI-Pipeline, keine
   Zeile in einer langen Methode.
3. **Keine echten Verordnungen in den Eval-Fällen.**
4. **Mandanten-ID gehört nie in eine API-Antwort.** Sie steht im
   Sicherheitskontext, nicht im Nutzdatenteil.
5. **Logging mit Tenant-ID, ohne Personenbezug.** Die Mandanten-ID ist für die
   Fehlersuche nötig. Namen und Diagnosen sind es nicht.

## Was in einer echten Anwendung zu klären wäre

Dieses Projekt ist ein Portfolio-Stück. Die folgenden Punkte werden hier
bewusst nicht gelöst, aber benannt, weil ihr Fehlen sonst wie Unkenntnis
aussieht.

- **Rechtsgrundlage und Datenminimierung.** Welche Felder braucht die
  Terminplanung wirklich? Eine Diagnose ist für die Slot-Berechnung nur über
  die Diagnosegruppe relevant, nicht im Klartext.
- **Verschlüsselung.** Ruhende Daten und Transportweg. Bei mandantengetrennten
  Schemata die Frage, ob je Mandant getrennte Schlüssel nötig sind.
- **Auftragsverarbeitung.** Jeder externe Dienst, insbesondere jeder
  LLM-Anbieter, ist ein Auftragsverarbeiter. Vertrag, Serverstandort,
  Unterauftragnehmer, Speicherdauer der Anfragen.
- **Ob ein LLM-Aufruf mit Patientenbezug überhaupt zulässig ist.** Die
  ehrliche Antwort lautet: nur unter Bedingungen, die vorher geklärt sein
  müssen. Deshalb ist die Architekturregel "der AI-Layer schlägt vor, die
  Domäne entscheidet" auch eine Datenschutzentscheidung. Sie hält die Menge
  der Daten klein, die das System überhaupt an ein Modell gibt.
- **Löschfristen und Aufbewahrungspflichten.** Die beiden widersprechen sich in
  dieser Domäne regelmäßig.
- **Protokollierung von Zugriffen.** Wer hat welchen Patientendatensatz gesehen.

## Warum das hier steht

Ein Bewerbungsprojekt in Healthcare, das diese Fragen nicht stellt, zeigt an,
dass sie im Projekt auch nicht gestellt würden.
