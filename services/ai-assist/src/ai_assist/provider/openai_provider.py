import json
import os

from openai import OpenAI

from ai_assist.provider.basis import (
    PROMPT,
    WERKZEUG_BESCHREIBUNG,
    WERKZEUG_NAME,
    WERKZEUG_SCHEMA,
    ProviderFehler,
)
from ai_assist.schema import VerordnungVorschlag


class OpenAIProvider:
    """GPT über das OpenAI SDK - auch gegen Azure, über base_url und Schlüssel.

    Dasselbe Werkzeug, dasselbe Schema, erzwungener Aufruf. Azure OpenAI
    spricht dieselbe Schnittstelle; der Unterschied ist die Adresse
    (AI_ASSIST_OPENAI_BASE_URL) und welcher Name das Deployment trägt.
    """

    def __init__(self, modell: str | None = None, client: OpenAI | None = None) -> None:
        self.modell = modell or os.environ.get("AI_ASSIST_OPENAI_MODELL", "gpt-4o-mini")
        self._client = client or OpenAI(base_url=os.environ.get("AI_ASSIST_OPENAI_BASE_URL"))

    @property
    def name(self) -> str:
        return f"openai:{self.modell}"

    def extrahiere(self, text: str) -> VerordnungVorschlag:
        antwort = self._client.chat.completions.create(
            model=self.modell,
            messages=[
                {"role": "system", "content": PROMPT},
                {"role": "user", "content": text},
            ],
            tools=[
                {
                    "type": "function",
                    "function": {
                        "name": WERKZEUG_NAME,
                        "description": WERKZEUG_BESCHREIBUNG,
                        "parameters": WERKZEUG_SCHEMA,
                    },
                }
            ],
            tool_choice={"type": "function", "function": {"name": WERKZEUG_NAME}},
        )
        aufrufe = antwort.choices[0].message.tool_calls or []
        for aufruf in aufrufe:
            if aufruf.type == "function" and aufruf.function.name == WERKZEUG_NAME:
                return VerordnungVorschlag.model_validate(json.loads(aufruf.function.arguments))
        raise ProviderFehler("OpenAI hat nicht über das Werkzeug geantwortet")
