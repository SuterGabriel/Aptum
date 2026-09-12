"""Das Protokoll trägt den Mandanten und nicht den Text - Regel 5 aus DATENSCHUTZ.md."""

import json
import logging

from fastapi.testclient import TestClient

from ai_assist import api
from ai_assist.protokoll import JsonFormatter
from ai_assist.provider import AufgezeichneterProvider

TEXT = "Patient: Testfall Alpha, geb. 01.01.1900. KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026"
OHNE_BEZUG = (
    "Patient: [PATIENT-1], geb. [GEBURTSDATUM-1]. KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026"
)


def test_eine_zeile_ist_json_mit_mandant_als_feld() -> None:
    record = logging.LogRecord("ai_assist", logging.INFO, "", 0, "erfassung", (), None)
    record.mandant = "praxis-a"  # type: ignore[attr-defined]
    record.zeichen = 42  # type: ignore[attr-defined]
    zeile = json.loads(JsonFormatter().format(record))
    assert zeile["meldung"] == "erfassung"
    assert zeile["mandant"] == "praxis-a"
    assert zeile["zeichen"] == 42
    assert zeile["stufe"] == "INFO"
    assert "zeit" in zeile


def test_der_freitext_steht_in_keiner_zeile(caplog) -> None:  # type: ignore[no-untyped-def]
    # Der Dienst protokolliert Mandant und Länge. Der Text selbst - mit dem
    # Namen darin - darf nirgends im Log auftauchen, auch nicht pseudonymisiert.
    api.app.dependency_overrides[api.provider] = lambda: AufgezeichneterProvider(
        {OHNE_BEZUG: {"heilmittel": "KG_EINZEL"}}
    )
    with caplog.at_level(logging.INFO, logger="ai_assist"):
        antwort = TestClient(api.app).post(
            "/erfassung", json={"text": TEXT}, headers={"X-Mandant": "praxis-a"}
        )
    assert antwort.status_code == 200
    zeilen = [r for r in caplog.records if r.name == "ai_assist"]
    assert zeilen, "der Dienst protokolliert die Erfassung"
    assert zeilen[0].mandant == "praxis-a"  # type: ignore[attr-defined]
    alles = " ".join(r.getMessage() + json.dumps(r.__dict__, default=str) for r in zeilen)
    assert "Testfall Alpha" not in alles
    assert "01.01.1900" not in alles
    assert "KG 6x" not in alles
