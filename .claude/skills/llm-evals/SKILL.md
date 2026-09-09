---
name: llm-evals
description: Wie LLM-Qualität in diesem Projekt gemessen wird - Aufbau eines Eval-Falls, feldweise Genauigkeit statt Gesamtnote, Regressionslauf bei jeder Prompt-Änderung, Provider-Vergleich. Nutze diesen Skill bei jeder Änderung an Prompts, am AI-Layer unter services/ai-assist/, an evals/ und bei allen Fragen zu LLM-Qualität, Structured Outputs oder Modellwechsel.
---

# Evals

## Die harte Regel

> Jede Änderung an einem Prompt oder am Extraktionsschema braucht einen
> Eval-Lauf. Ohne Lauf kein Merge.

Der Grund ist unangenehm konkret: Eine Prompt-Änderung, die einen Fall
repariert, bricht typischerweise einen anderen. Ohne Suite merkt das niemand,
weil man immer denselben Beispielfall von Hand testet.

## Was die Suite misst

Feature 1 ist die Verordnungserfassung aus Freitext in strukturierte Felder.
Gemessen wird **feldweise**, nicht als Gesamtnote:

| Feld | Metrik |
|---|---|
| Heilmittel | exakte Übereinstimmung |
| Diagnosegruppe | exakte Übereinstimmung |
| Verordnungsmenge | exakte Übereinstimmung |
| Frequenz | normalisierter Vergleich, 2x pro Woche gleich 2/Woche |
| Ausstellungsdatum | exakte Übereinstimmung |
| Hausbesuch ja/nein | exakte Übereinstimmung |

Eine Gesamtgenauigkeit von 91 Prozent sagt nichts. "Frequenz 98 Prozent,
Diagnosegruppe 74 Prozent" sagt, wo gearbeitet werden muss.

## Aufbau eines Falls

Ein Fall ist eine JSON-Datei unter `evals/cases/`:

```json
{
  "id": "frequenz-spanne-01",
  "eingabe": "KG, 6x, 1-3x wöchentlich, Diagnosegruppe WS2",
  "erwartet": {
    "heilmittel": "KG",
    "menge": 6,
    "frequenz": { "min": 1, "max": 3, "einheit": "WOCHE" }
  },
  "warum": "Frequenzspanne statt fester Zahl - häufiger Fall, leicht zu verlieren"
}
```

Das Feld `warum` ist Pflicht. Ein Fall ohne Begründung wird beim nächsten
Aufräumen gelöscht, weil niemand weiß, was er absichert.

## Zusammensetzung der Suite

Vierzig bis fünfzig Fälle. Absichtlich unausgewogen: mehr Grenzfälle als
Normalfälle, weil Normalfälle ohnehin funktionieren. Enthalten sein müssen
mindestens ein unleserlicher Freitext, eine Angabe, die im Text fehlt, und eine
widersprüchliche Angabe. Bei den letzten beiden ist die richtige Antwort
"nicht extrahierbar", nicht ein geratener Wert.

## Testdaten

Ausschließlich klar erkennbare Testdaten. Keine realistischen Patientennamen.
Die Fälle enthalten keine echten Verordnungen.

## Provider-Vergleich

Der AI-Layer hat ein Provider-Interface mit zwei Implementierungen. Die Suite
läuft gegen beide. Der Vergleich ist selbst ein Ergebnis: Er zeigt, welche
Fälle modellabhängig sind und wo der Prompt zu schwach ist.

## In der CI

Der Eval-Job läuft nur bei Änderungen an Prompts oder AI-Code, weil er Geld
kostet. Ergebnis und Differenz zum letzten Lauf gehen als Kommentar an den Pull
Request. Zusätzlich läuft die Suite nächtlich, damit eine Kurve entsteht statt
einer Momentaufnahme.

## Die Grenze der Evals

Evals messen die Extraktion. Sie ersetzen nicht die Domänenprüfung. Auch ein
Ergebnis mit hundert Prozent Feldgenauigkeit durchläuft dieselbe
deterministische Regelprüfung wie eine manuelle Eingabe. Der AI-Layer schlägt
vor, die Domäne entscheidet.
