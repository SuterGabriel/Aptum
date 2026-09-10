import json
from pathlib import Path
from typing import Any

from ai_assist.provider.basis import ProviderFehler
from ai_assist.schema import VerordnungVorschlag


class AufgezeichneterProvider:
    """Antwortet aus einer Aufzeichnung - für Tests ohne Schlüssel und ohne Netz.

    Kein Modell, kein Zufall, kein Geld. Was hier steht, hat einmal ein
    echtes Modell gesagt oder ein Mensch als Beispiel hingeschrieben. Die
    Pipeline drum herum - Pseudonymisierung, Schema, Rückgabe - ist damit
    vollständig testbar; die Qualität der Extraktion misst nur der Eval.
    """

    def __init__(self, antworten: dict[str, dict[str, Any]], name: str = "aufzeichnung") -> None:
        self._antworten = antworten
        self._name = name

    @classmethod
    def aus_datei(cls, pfad: str | Path) -> "AufgezeichneterProvider":
        daten = json.loads(Path(pfad).read_text(encoding="utf-8"))
        return cls(
            {f["eingabe"]: f["antwort"] for f in daten["faelle"]},
            name=f"aufzeichnung:{Path(pfad).name}",
        )

    @property
    def name(self) -> str:
        return self._name

    def extrahiere(self, text: str) -> VerordnungVorschlag:
        if text not in self._antworten:
            raise ProviderFehler(f"Keine Aufzeichnung für diesen Text: {text[:60]!r}")
        return VerordnungVorschlag.model_validate(self._antworten[text])
