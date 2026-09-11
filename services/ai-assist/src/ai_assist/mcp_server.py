"""Der MCP-Server: Aptum als Werkzeug für ein Sprachmodell.

Vier Werkzeuge auf der REST-Schnittstelle des Scheduling-Dienstes. Kein
eigener Weg in die Domäne: Was hier gebucht wird, läuft durch dasselbe
Regelwerk wie eine Buchung von Hand, weil es *dieselbe* Schnittstelle ist.
Regel 3 aus CLAUDE.md braucht hier keine zweite Durchsetzung - sie ist
schon da, und das ist der Punkt.

Was dieser Server dazu beiträgt, sind drei Einschränkungen an der Grenze:

1. **Der Mandant ist kein Parameter.** Er kommt aus der Umgebung. Ein
   Modell kann nicht sagen „buche in praxis-b" - es kennt den Mandanten
   nicht und kann ihn nicht wählen.
2. **Die Übersteuerung braucht eine echte Begründung.** Mindestens zehn
   Zeichen, wie im Buchungsdialog (ADR-009). Ein leeres Feld kommt nicht
   durch.
3. **Jede Antwort trägt ihren Prüfbericht.** Auch die abgelehnte. Ein
   Modell, das nicht buchen darf, erfährt warum - Regel für Regel.
"""

import os
from typing import Annotated, Any, Literal

import httpx
from mcp.server.mcpserver import MCPServer
from mcp.server.mcpserver.exceptions import ToolError
from mcp.types import ToolAnnotations
from pydantic import Field

Wochentag = Literal["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY"]

MINDESTBEGRUENDUNG = 10

server = MCPServer(
    name="aptum-scheduling",
    version="0.1",
    instructions=(
        "Terminplanung für eine Physio- oder Ergotherapiepraxis. Ein freier Termin ist "
        "die Schnittmenge aus vier Dimensionen: Verordnung, Therapeut, Raum, Wunschfenster. "
        "Suche zuerst oder prüfe einen konkreten Slot, bevor du buchst - die Antwort nennt "
        "je Regel den Ausgang und die Begründung. Du entscheidest nicht, ob eine Regel gilt; "
        "das tut die Domäne. Ist eine Regel verletzt, blockiert die Buchung. Übersteuern "
        "geht nur mit einer Begründung, die ein Mensch dir gegeben hat - erfinde keine."
    ),
)


def _basis() -> str:
    return os.environ.get("APTUM_SCHEDULING_URL", "http://localhost:8080")


def _mandant() -> str:
    """Der Mandant kommt aus der Umgebung, nie aus einem Werkzeugaufruf."""
    return os.environ.get("APTUM_MANDANT", "praxis-a")


def client() -> httpx.AsyncClient:
    """Der Zugang zum Scheduling-Dienst. Als Funktion, damit ein Test ihn ersetzt."""
    return httpx.AsyncClient(base_url=_basis(), timeout=20.0)


async def _ruf(methode: str, pfad: str, koerper: dict[str, Any] | None = None) -> dict[str, Any]:
    """Ein Aufruf an den Scheduling-Dienst. 409 ist eine Antwort, kein Fehler."""
    async with client() as http:
        antwort = await http.request(methode, pfad, json=koerper, headers={"X-Mandant": _mandant()})
    if antwort.status_code >= 400 and antwort.status_code != 409:
        # ToolError statt einer beliebigen Ausnahme: Nur so liest das Modell,
        # was schiefging. Alles andere kommt bei ihm als "Error executing
        # tool" an, und damit kann es nichts anfangen.
        raise ToolError(
            f"Der Scheduling-Dienst antwortete {antwort.status_code}: {antwort.text[:300]}"
        )
    daten: dict[str, Any] = antwort.json()
    return daten


@server.tool(
    title="Termine suchen",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
async def termine_suchen(
    verordnung: Annotated[str, Field(description="Kennung der Verordnung (UUID)")],
    heilmittel: Annotated[str, Field(description="Code des Heilmittels, etwa KG_EINZEL")],
    von: Annotated[str, Field(description="Erster Tag des Wunschfensters, JJJJ-MM-TT")],
    bis: Annotated[str, Field(description="Letzter Tag, JJJJ-MM-TT")],
    fruehestens: Annotated[str, Field(description="Früheste Uhrzeit, HH:MM")] = "08:00",
    spaetestens: Annotated[str, Field(description="Späteste Uhrzeit, HH:MM")] = "18:00",
    wochentage: Annotated[
        list[Wochentag] | None, Field(description="Welche Wochentage in Frage kommen")
    ] = None,
) -> dict[str, Any]:
    """Sucht freie Termine für eine Verordnung.

    Liefert Vorschläge mit Datum, Uhrzeit, Person und Raum - und dazu, was
    aus welchem Grund weggelassen wurde. Der Abschnitt `ausgeschlossen`
    nennt je Regel die Anzahl; das ist oft die wichtigere Information.
    """
    return await _ruf(
        "POST",
        "/termine/suche",
        {
            "verordnung": verordnung,
            "heilmittel": heilmittel,
            "von": von,
            "bis": bis,
            "fruehestens": fruehestens,
            "spaetestens": spaetestens,
            "wochentage": wochentage or ["MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY"],
        },
    )


@server.tool(
    title="Termin prüfen",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
async def termin_pruefen(
    verordnung: Annotated[str, Field(description="Kennung der Verordnung (UUID)")],
    heilmittel: Annotated[str, Field(description="Code des Heilmittels")],
    therapeut: Annotated[str, Field(description="Kürzel der Person")],
    raum: Annotated[str, Field(description="Bezeichnung des Raums")],
    beginn: Annotated[
        str, Field(description="Beginn mit Zeitzone, etwa 2026-03-02T09:00:00+01:00")
    ],
) -> dict[str, Any]:
    """Prüft einen konkreten Slot, ohne zu buchen.

    Dieselbe Prüfung, die eine Buchung durchläuft. Die Antwort nennt jede
    Regel mit Ausgang (ERFUELLT, WARNUNG, VERLETZT) und Begründung, und
    unter `ausgang` das Ergebnis: FREI oder BLOCKIERT.
    """
    return await _ruf(
        "POST",
        "/termine/pruefung",
        {
            "verordnung": verordnung,
            "heilmittel": heilmittel,
            "therapeut": therapeut,
            "raum": raum,
            "beginn": beginn,
        },
    )


@server.tool(
    title="Termin buchen",
    annotations=ToolAnnotations(read_only_hint=False, idempotent_hint=False, open_world_hint=False),
)
async def termin_buchen(
    verordnung: Annotated[str, Field(description="Kennung der Verordnung (UUID)")],
    heilmittel: Annotated[str, Field(description="Code des Heilmittels")],
    therapeut: Annotated[str, Field(description="Kürzel der Person")],
    raum: Annotated[str, Field(description="Bezeichnung des Raums")],
    beginn: Annotated[str, Field(description="Beginn mit Zeitzone")],
    begruendung: Annotated[
        str | None,
        Field(
            # Die Mindestlänge steht im Schema, nicht nur im Code: Das Modell
            # sieht die Bedingung, bevor es aufruft, statt danach einen Fehler.
            min_length=MINDESTBEGRUENDUNG,
            description=(
                "Nur wenn eine verletzte Regel übersteuert werden soll: die Begründung "
                "eines Menschen, mindestens zehn Zeichen. Wird mit dem Termin gespeichert. "
                "Erfinde sie nicht."
            ),
        ),
    ] = None,
) -> dict[str, Any]:
    """Bucht einen Termin - durch dieselbe Regelprüfung wie jede Buchung.

    Ist eine Regel verletzt, wird nicht gebucht: Die Antwort trägt dann
    `ausgang` BLOCKIERT und die verletzte Regel mit Begründung, aber keine
    Kennung. Wer trotzdem buchen will, braucht eine Begründung von einem
    Menschen (ADR-009).
    """
    koerper: dict[str, Any] = {
        "verordnung": verordnung,
        "heilmittel": heilmittel,
        "therapeut": therapeut,
        "raum": raum,
        "beginn": beginn,
    }
    if begruendung is not None:
        # Das Schema zählt Zeichen, auch Leerzeichen. Zehn Leerzeichen sind
        # keine Begründung - der Buchungsdialog prüft an derselben Stelle.
        if len(begruendung.strip()) < MINDESTBEGRUENDUNG:
            raise ToolError(
                f"Eine Übersteuerung braucht eine Begründung von mindestens "
                f"{MINDESTBEGRUENDUNG} Zeichen. Frag den Menschen, der sie verantwortet."
            )
        koerper["uebersteuerung"] = {"begruendung": begruendung.strip(), "von": "MCP"}
    return await _ruf("POST", "/termine", koerper)


@server.tool(
    title="Woche anzeigen",
    annotations=ToolAnnotations(read_only_hint=True, open_world_hint=False),
)
async def woche_anzeigen(
    tag: Annotated[
        str | None, Field(description="Ein Tag in der gesuchten Woche, JJJJ-MM-TT")
    ] = None,
) -> dict[str, Any]:
    """Zeigt die Woche je Person: was belegt ist, und wodurch.

    Je Block eine Art: BELEGT (Behandlung), RUESTZEIT (Vor- und
    Nachbereitung), NACHRUHE (bindet den Raum, nicht die Person),
    ABWESENHEIT, GESPERRT. Frei ist, wo kein Block liegt.
    """
    pfad = "/kalender/woche" + (f"?tag={tag}" if tag else "")
    return await _ruf("GET", pfad)


def main() -> None:
    """Startet den Server über stdio - so bindet ein MCP-Client ihn ein."""
    server.run()


if __name__ == "__main__":
    main()
