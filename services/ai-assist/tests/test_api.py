from fastapi.testclient import TestClient

from ai_assist import api
from ai_assist.provider import AufgezeichneterProvider

TEXT = "KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026"


def client() -> TestClient:
    api.app.dependency_overrides[api.provider] = lambda: AufgezeichneterProvider(
        {
            TEXT: {
                "heilmittel": "KG_EINZEL",
                "diagnosegruppe": "WS",
                "verordnete_einheiten": 6,
                "frequenz": {"min_pro_woche": 2, "max_pro_woche": 2},
                "ausstellungsdatum": "2026-02-27",
            }
        }
    )
    return TestClient(api.app)


def test_erfassung_liefert_vorschlag_und_nennt_den_provider() -> None:
    antwort = client().post("/erfassung", json={"text": TEXT}, headers={"X-Mandant": "praxis-a"})
    assert antwort.status_code == 200
    koerper = antwort.json()
    assert koerper["vorschlag"]["heilmittel"] == "KG_EINZEL"
    assert koerper["vorschlag"]["nicht_extrahierbar"] == ["dringlicher_bedarf", "hausbesuch"]
    assert koerper["provider"] == "aufzeichnung"
    assert koerper["pseudonymisiert"] == 0


def test_ohne_mandant_keine_antwort() -> None:
    assert client().post("/erfassung", json={"text": TEXT}).status_code == 422


def test_unbekannter_text_ist_ein_provider_fehler_502() -> None:
    antwort = client().post(
        "/erfassung", json={"text": "anderer Text"}, headers={"X-Mandant": "praxis-a"}
    )
    assert antwort.status_code == 502


def test_health() -> None:
    assert client().get("/health").json() == {"status": "UP"}
