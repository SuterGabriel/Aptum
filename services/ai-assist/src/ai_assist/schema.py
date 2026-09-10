"""Was das Modell liefern darf - und nur das.

Das Schema ist die Grenze zwischen Freitext und Domäne. Jedes Feld ist
optional, weil die richtige Antwort auf eine fehlende oder widersprüchliche
Angabe "nicht extrahierbar" ist, nicht ein geratener Wert. Die Codes sind
die des Scheduling-Dienstes; was hier nicht steht, kann dort nicht angelegt
werden.
"""

from datetime import date
from typing import Literal

from pydantic import BaseModel, ConfigDict, Field

Heilmittel = Literal[
    "KG_EINZEL",
    "KG_GRUPPE",
    "MANUELLE_THERAPIE",
    "KLASSISCHE_MASSAGE",
    "MLD_TEILBEHANDLUNG",
    "MLD_GROSSBEHANDLUNG",
    "MLD_GANZBEHANDLUNG",
    "KG_ZNS_ERWACHSENE",
    "KG_ZNS_KINDER",
    "KG_GERAET",
    "KG_BEWEGUNGSBAD",
    "WARMPACKUNG",
    "ERGO_MOTORISCH_FUNKTIONELL",
    "ERGO_SENSOMOTORISCH",
    "ERGO_HIRNLEISTUNGSTRAINING",
    "ERGO_PSYCHISCH_FUNKTIONELL",
    "ERGO_BERATUNG_UMFELD",
]

Diagnosegruppe = Literal["WS", "EX", "CS", "AT", "GE", "SO", "LY", "ZN", "PN"]

Feldname = Literal[
    "heilmittel",
    "diagnosegruppe",
    "verordnete_einheiten",
    "frequenz",
    "ausstellungsdatum",
    "dringlicher_bedarf",
    "hausbesuch",
]


class Frequenz(BaseModel):
    """Behandlungen je Woche als Spanne. Eine feste Zahl ist eine Spanne mit min gleich max."""

    model_config = ConfigDict(extra="forbid")

    min_pro_woche: int = Field(ge=1, le=7)
    max_pro_woche: int = Field(ge=1, le=7)


class VerordnungVorschlag(BaseModel):
    """Die strukturierte Verordnung, wie das Modell sie aus dem Freitext liest."""

    model_config = ConfigDict(extra="forbid")

    heilmittel: Heilmittel | None = None
    diagnosegruppe: Diagnosegruppe | None = None
    verordnete_einheiten: int | None = Field(default=None, ge=1, le=99)
    frequenz: Frequenz | None = None
    ausstellungsdatum: date | None = None
    dringlicher_bedarf: bool | None = None
    hausbesuch: bool | None = None
    nicht_extrahierbar: list[Feldname] = Field(
        default_factory=list,
        description="Felder, die im Text fehlen oder sich widersprechen. Nie raten.",
    )
    hinweise: list[str] = Field(
        default_factory=list,
        description="Was am Text auffällt: Widersprüche, Unleserliches, Ungewöhnliches.",
    )


class Erfassung(BaseModel):
    """Die Antwort des Dienstes: der Vorschlag und was auf dem Weg dorthin geschah."""

    vorschlag: VerordnungVorschlag
    pseudonymisiert: int = Field(description="Wie viele Angaben vor dem Aufruf ersetzt wurden.")
    provider: str
