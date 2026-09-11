"""Der MCP-Server gegen einen nachgestellten Scheduling-Dienst.

Geprüft wird, was der Server selbst zusagt: dass der Mandant nicht aus einem
Werkzeugaufruf kommt, dass eine Übersteuerung eine echte Begründung braucht,
und dass eine blockierte Buchung als Antwort mit Regeln zurückkommt statt
als Fehler. Ob die Regeln stimmen, prüft der Scheduling-Dienst - hier steht
kein zweites Regelwerk.
"""

import json
from typing import Any

import httpx
import pytest
from mcp.server.mcpserver.exceptions import ToolError, UnexpectedToolError

from ai_assist import mcp_server

MANDANT = "praxis-test"
VERORDNUNG = "11111111-2222-4333-8444-555555555555"

FREI = {
    "termin": "abc-123",
    "ausgang": "FREI",
    "regeln": [{"regel": "Behandlungsbeginn", "ausgang": "ERFUELLT", "begruendung": "in Frist"}],
}
BLOCKIERT = {
    "termin": None,
    "ausgang": "BLOCKIERT",
    "regeln": [
        {"regel": "Therapeut verfügbar", "ausgang": "VERLETZT", "begruendung": "bereits belegt"}
    ],
}


class Dienst:
    """Nimmt jede Anfrage auf und antwortet mit dem, was ihm mitgegeben wurde."""

    def __init__(self, antwort: dict[str, Any], status: int = 200) -> None:
        self.antwort = antwort
        self.status = status
        self.anfragen: list[httpx.Request] = []

    def __call__(self, anfrage: httpx.Request) -> httpx.Response:
        self.anfragen.append(anfrage)
        return httpx.Response(self.status, json=self.antwort)

    @property
    def letzte(self) -> httpx.Request:
        return self.anfragen[-1]

    def koerper(self) -> dict[str, Any]:
        return json.loads(self.letzte.content)


@pytest.fixture
def dienst(monkeypatch: pytest.MonkeyPatch):  # type: ignore[no-untyped-def]
    monkeypatch.setenv("APTUM_MANDANT", MANDANT)
    monkeypatch.setenv("APTUM_SCHEDULING_URL", "http://scheduling.test")

    def einrichten(antwort: dict[str, Any], status: int = 200) -> Dienst:
        d = Dienst(antwort, status)
        monkeypatch.setattr(
            mcp_server,
            "client",
            lambda: httpx.AsyncClient(
                base_url="http://scheduling.test", transport=httpx.MockTransport(d)
            ),
        )
        return d

    return einrichten


async def rufe(werkzeug: str, **argumente: Any) -> Any:
    """Über den MCP-Weg aufrufen, nicht an der Funktion vorbei."""
    return await mcp_server.server.call_tool(werkzeug, argumente)


@pytest.mark.anyio
async def test_die_vier_werkzeuge_und_ihre_art() -> None:
    werkzeuge = {t.name: t for t in await mcp_server.server.list_tools()}
    assert set(werkzeuge) == {"termine_suchen", "termin_pruefen", "termin_buchen", "woche_anzeigen"}
    assert werkzeuge["termine_suchen"].annotations.read_only_hint is True
    assert werkzeuge["termin_buchen"].annotations.read_only_hint is False


@pytest.mark.anyio
async def test_kein_werkzeug_kennt_den_mandanten() -> None:
    # Die eine Zusage dieses Servers: Ein Modell kann die Praxis nicht wählen.
    for werkzeug in await mcp_server.server.list_tools():
        assert "mandant" not in json.dumps(werkzeug.input_schema).lower(), werkzeug.name


@pytest.mark.anyio
async def test_suchen_schickt_den_mandanten_aus_der_umgebung(dienst) -> None:  # type: ignore[no-untyped-def]
    d = dienst({"vorschlaege": [], "zusammenfassung": "nichts frei"})
    await rufe(
        "termine_suchen",
        verordnung=VERORDNUNG,
        heilmittel="KG_EINZEL",
        von="2026-03-02",
        bis="2026-03-06",
    )
    assert d.letzte.headers["X-Mandant"] == MANDANT
    assert str(d.letzte.url).endswith("/termine/suche")
    # Ohne Angabe: die Werktage, nicht das Wochenende.
    assert d.koerper()["wochentage"] == ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"]


@pytest.mark.anyio
async def test_buchen_ohne_begruendung_uebersteuert_nicht(dienst) -> None:  # type: ignore[no-untyped-def]
    d = dienst(FREI, status=201)
    await rufe(
        "termin_buchen",
        verordnung=VERORDNUNG,
        heilmittel="KG_EINZEL",
        therapeut="T. Alpha",
        raum="Raum 1",
        beginn="2026-03-02T09:00:00+01:00",
    )
    assert "uebersteuerung" not in d.koerper()


@pytest.mark.anyio
async def test_eine_duenne_begruendung_kommt_nicht_bis_zum_dienst(dienst) -> None:  # type: ignore[no-untyped-def]
    d = dienst(FREI, status=201)
    # Die Mindestlänge steht im Schema, also lehnt schon die Schema-Prüfung ab.
    with pytest.raises(ToolError, match="minLength|mindestens|10"):
        await rufe(
            "termin_buchen",
            verordnung=VERORDNUNG,
            heilmittel="KG_EINZEL",
            therapeut="T. Alpha",
            raum="Raum 1",
            beginn="2026-03-02T09:00:00+01:00",
            begruendung="ok",
        )
    assert d.anfragen == [], "der Dienst wurde gar nicht erst gefragt"


@pytest.mark.anyio
async def test_eine_echte_begruendung_geht_mit(dienst) -> None:  # type: ignore[no-untyped-def]
    d = dienst({**FREI, "ausgang": "UEBERSTEUERT"}, status=201)
    await rufe(
        "termin_buchen",
        verordnung=VERORDNUNG,
        heilmittel="KG_EINZEL",
        therapeut="T. Alpha",
        raum="Raum 1",
        beginn="2026-03-02T09:00:00+01:00",
        begruendung="Absprache mit der Praxisleitung",
    )
    assert d.koerper()["uebersteuerung"]["begruendung"] == "Absprache mit der Praxisleitung"


@pytest.mark.anyio
async def test_eine_blockierte_buchung_ist_eine_antwort_mit_regeln(dienst) -> None:  # type: ignore[no-untyped-def]
    dienst(BLOCKIERT, status=409)
    ergebnis = await rufe(
        "termin_buchen",
        verordnung=VERORDNUNG,
        heilmittel="KG_EINZEL",
        therapeut="T. Alpha",
        raum="Raum 1",
        beginn="2026-03-02T09:00:00+01:00",
    )
    text = json.dumps(ergebnis, default=str)
    assert "BLOCKIERT" in text
    assert "bereits belegt" in text


@pytest.mark.anyio
async def test_ein_kaputter_dienst_wird_gemeldet(dienst) -> None:  # type: ignore[no-untyped-def]
    dienst({"fehler": "kaputt"}, status=500)
    # ToolError, nicht irgendeine Ausnahme: Nur dann liest das Modell den Grund.
    with pytest.raises(ToolError, match="500"):
        await rufe("woche_anzeigen")


@pytest.mark.anyio
async def test_woche_mit_und_ohne_tag(dienst) -> None:  # type: ignore[no-untyped-def]
    d = dienst({"montag": "2026-03-02", "spalten": []})
    await rufe("woche_anzeigen")
    assert str(d.letzte.url).endswith("/kalender/woche")
    await rufe("woche_anzeigen", tag="2026-03-04")
    assert str(d.letzte.url).endswith("/kalender/woche?tag=2026-03-04")


@pytest.mark.anyio
async def test_ein_fehler_erreicht_das_modell_als_text(dienst) -> None:  # type: ignore[no-untyped-def]
    # Die Unterscheidung, die zählt: ToolError trägt seine Meldung zum
    # Modell, UnexpectedToolError nicht. Ein Absturz wäre dort nur
    # "Error executing tool" - unbrauchbar für den, der weiterarbeiten soll.
    dienst({"fehler": "kaputt"}, status=503)
    with pytest.raises(ToolError) as fund:
        await rufe("woche_anzeigen")
    assert not isinstance(fund.value, UnexpectedToolError)
    assert "503" in str(fund.value)
