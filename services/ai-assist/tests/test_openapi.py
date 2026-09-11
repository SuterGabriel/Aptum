"""Das OpenAPI-Dokument des AI-Dienstes ist erzeugt, nicht geschrieben.

Dasselbe Muster wie im Scheduling-Dienst: Dieser Test vergleicht
`docs/api/ai-assist-openapi.json` mit dem, was die Anwendung liefert, und
wird rot, sobald sich die Schnittstelle ändert und die Datei nicht.
Aktualisieren: `uv run pytest tests/test_openapi.py --openapi-aktualisieren`.

Ohne diesen Test wäre die Datei die nächste, die still veraltet - und aus
ihr werden die TypeScript-Typen des Frontends erzeugt.
"""

import json
from pathlib import Path

import pytest

from ai_assist.api import app

DOKUMENT = Path(__file__).parents[3] / "docs" / "api" / "ai-assist-openapi.json"
BEFEHL = "uv run pytest tests/test_openapi.py --openapi-aktualisieren"


def test_dokument_im_repo_ist_aktuell(request: pytest.FixtureRequest) -> None:
    geliefert = app.openapi()

    if request.config.getoption("--openapi-aktualisieren"):
        DOKUMENT.parent.mkdir(parents=True, exist_ok=True)
        DOKUMENT.write_text(
            json.dumps(geliefert, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )

    assert DOKUMENT.exists(), f"Dokument fehlt - einmal erzeugen: {BEFEHL}"
    im_repo = json.loads(DOKUMENT.read_text(encoding="utf-8"))
    assert im_repo == geliefert, f"{DOKUMENT.name} ist veraltet. Aktualisieren: {BEFEHL}"
