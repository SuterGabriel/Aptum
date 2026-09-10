import os
from typing import Any

from anthropic import Anthropic

from ai_assist.provider.basis import (
    PROMPT,
    WERKZEUG_BESCHREIBUNG,
    WERKZEUG_NAME,
    WERKZEUG_SCHEMA,
    ProviderFehler,
)
from ai_assist.schema import VerordnungVorschlag


class AnthropicProvider:
    """Claude über das Anthropic SDK, Antwort erzwungen über das Werkzeug.

    Structured Output per Tool-Use: Das Modell muss das Werkzeug aufrufen,
    und dessen Eingabe ist das Schema. Freitext in der Antwort gibt es nicht.
    """

    def __init__(self, modell: str | None = None, client: Anthropic | None = None) -> None:
        self.modell = modell or os.environ.get("AI_ASSIST_ANTHROPIC_MODELL", "claude-sonnet-5")
        self._client = client or Anthropic()

    @property
    def name(self) -> str:
        return f"anthropic:{self.modell}"

    def extrahiere(self, text: str) -> VerordnungVorschlag:
        antwort = self._client.messages.create(
            model=self.modell,
            max_tokens=1024,
            system=PROMPT,
            tools=[
                {
                    "name": WERKZEUG_NAME,
                    "description": WERKZEUG_BESCHREIBUNG,
                    "input_schema": WERKZEUG_SCHEMA,
                }
            ],
            tool_choice={"type": "tool", "name": WERKZEUG_NAME},
            messages=[{"role": "user", "content": text}],
        )
        for block in antwort.content:
            if block.type == "tool_use" and block.name == WERKZEUG_NAME:
                eingabe: Any = block.input
                return VerordnungVorschlag.model_validate(eingabe)
        raise ProviderFehler("Anthropic hat nicht über das Werkzeug geantwortet")
