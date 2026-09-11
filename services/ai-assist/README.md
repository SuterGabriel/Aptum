# Aptum AI-Assist

Der AI-Layer. Er schlägt vor und entscheidet nicht: Was hier herauskommt, ist
ein Vorschlag, der im Scheduling-Dienst durch dieselbe Regelprüfung geht wie
eine Eingabe von Hand. Dieser Dienst kennt keine Fachregel.

Feature 1: die **Verordnungserfassung aus Freitext** — der abgetippte oder
eingescannte Text einer Heilmittelverordnung wird zu Feldern (Heilmittel,
Diagnosegruppe, Menge, Frequenz, Datum). Was fehlt oder sich widerspricht,
heißt „nicht extrahierbar", nie ein geratener Wert.

## Die Pipeline

```
Freitext
  -> pseudonymisieren        benannter Schritt, DATENSCHUTZ.md Regel 2
  -> Provider fragen         Anthropic oder OpenAI-kompatibel, Antwort nur über das Werkzeug
  -> Schema prüfen           Pydantic; unbekannte Codes scheitern hier, nicht in der Domäne
  -> Platzhalter zurück      nur in freiem Text; ein Heilmittel hat keinen Namen
  -> Vorschlag
```

Der Prompt liegt in `src/ai_assist/prompts/erfassung.md`. Jede Änderung daran
braucht einen Eval-Lauf (Skill `llm-evals`).

## Starten

```bash
uv sync
uv run pytest                      # 15 Tests, ohne Schlüssel, ohne Netz
uv run ruff format --check . && uv run ruff check . && uv run mypy

ANTHROPIC_API_KEY=... uv run uvicorn ai_assist.api:app --port 8090
curl -H 'Content-Type: application/json' -H 'X-Mandant: praxis-a' \
     -d '{"text":"KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026"}' localhost:8090/erfassung
```

`AI_ASSIST_PROVIDER` wählt `anthropic` (Standard), `openai` oder
`aufzeichnung`; für OpenAI/Azure `OPENAI_API_KEY` und bei Azure zusätzlich
`AI_ASSIST_OPENAI_BASE_URL`. Schlüssel stehen nie im Repo.

## Der MCP-Server

Vier Werkzeuge auf der REST-Schnittstelle des Scheduling-Dienstes, damit ein
Sprachmodell die Praxis bedienen kann: `termine_suchen`, `termin_pruefen`,
`termin_buchen`, `woche_anzeigen`.

```bash
APTUM_SCHEDULING_URL=http://localhost:8080 APTUM_MANDANT=praxis-a uv run aptum-mcp
```

Für einen Client (etwa Claude Desktop) in dessen Konfiguration:

```json
{
  "mcpServers": {
    "aptum": {
      "command": "uv",
      "args": ["run", "--project", "C:/Aptum/services/ai-assist", "aptum-mcp"],
      "env": {
        "APTUM_SCHEDULING_URL": "http://localhost:8080",
        "APTUM_MANDANT": "praxis-a"
      }
    }
  }
}
```

**Kein zweiter Weg in die Domäne.** Was hier gebucht wird, läuft durch dasselbe
Regelwerk wie eine Buchung von Hand — weil es dieselbe Schnittstelle ist. Der
Server fügt drei Einschränkungen an der Grenze hinzu:

- **Der Mandant ist kein Parameter.** Er kommt aus der Umgebung; ein Modell
  kann die Praxis nicht wählen. Ein Test prüft, dass das Wort in keinem
  Werkzeugschema vorkommt.
- **Die Übersteuerung braucht eine Begründung** von mindestens zehn Zeichen —
  als `minLength` im Schema, damit das Modell die Bedingung sieht, bevor es
  aufruft (ADR-009).
- **Fehler sind lesbar.** Ein `ToolError` trägt seine Meldung zum Modell; jede
  andere Ausnahme käme dort als „Error executing tool" an.

## Was bewusst schmal ist

Die Pseudonymisierung erkennt vier Arten von Angaben mit regulären
Ausdrücken. Eine echte Anwendung bräuchte eine geprüfte Erkennung; hier
zählt, dass der Schritt existiert, benannt ist und vor dem Aufruf liegt.
