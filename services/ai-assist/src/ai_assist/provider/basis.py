from pathlib import Path
from typing import Protocol

from ai_assist.schema import VerordnungVorschlag

PROMPT = (Path(__file__).parent.parent / "prompts" / "erfassung.md").read_text(encoding="utf-8")

# Das Werkzeug, über das jedes Modell antwortet: Name, Beschreibung und das
# JSON-Schema aus dem Pydantic-Modell. So gibt es ein Schema, nicht drei.
WERKZEUG_NAME = "verordnung_erfassen"
WERKZEUG_BESCHREIBUNG = "Die aus dem Freitext gelesenen Felder der Verordnung."
WERKZEUG_SCHEMA = VerordnungVorschlag.model_json_schema()


class ProviderFehler(RuntimeError):
    """Der Provider hat nicht geantwortet oder nicht über das Werkzeug."""


class Provider(Protocol):
    """Ein Provider liest den pseudonymisierten Text und liefert den Vorschlag."""

    @property
    def name(self) -> str: ...

    def extrahiere(self, text: str) -> VerordnungVorschlag: ...
