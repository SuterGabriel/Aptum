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

## Was bewusst schmal ist

Die Pseudonymisierung erkennt vier Arten von Angaben mit regulären
Ausdrücken. Eine echte Anwendung bräuchte eine geprüfte Erkennung; hier
zählt, dass der Schritt existiert, benannt ist und vor dem Aufruf liegt.
