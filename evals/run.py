"""Die Eval-Suite: feldweise, gegen einen Provider, mit Vergleich zum letzten Lauf.

Aufruf aus dem Repo-Stamm:

    uv run --project services/ai-assist python evals/run.py --provider anthropic
    uv run --project services/ai-assist python evals/run.py --provider openai

Gemessen wird je Feld, nicht als Gesamtnote (Skill llm-evals): Eine
Gesamtgenauigkeit von 91 Prozent sagt nichts; "Frequenz 98, Diagnosegruppe 74"
sagt, wo gearbeitet werden muss. Wichtiger als die Zahl sind die Fälle, die
vorher grün waren und jetzt rot sind - sie stehen einzeln mit ihrem "warum".

Der letzte Lauf je Provider liegt unter evals/ergebnisse/ und wird
eingecheckt: So entsteht eine Kurve, nicht eine Momentaufnahme.
"""

from __future__ import annotations

import argparse
import json
import sys
from dataclasses import dataclass, field
from datetime import UTC, datetime
from pathlib import Path
from typing import Any

from ai_assist.erfassung import erfasse
from ai_assist.provider import AufgezeichneterProvider, Provider
from ai_assist.schema import VerordnungVorschlag

HIER = Path(__file__).parent
FAELLE = HIER / "cases"
ERGEBNISSE = HIER / "ergebnisse"

FELDER = [
    "heilmittel",
    "diagnosegruppe",
    "verordnete_einheiten",
    "frequenz",
    "ausstellungsdatum",
    "dringlicher_bedarf",
    "hausbesuch",
]


@dataclass
class Fall:
    id: str
    eingabe: str
    erwartet: dict[str, Any]
    warum: str

    @classmethod
    def lade(cls, pfad: Path) -> Fall:
        d = json.loads(pfad.read_text(encoding="utf-8"))
        if not d.get("warum"):
            raise ValueError(f"{pfad.name}: 'warum' ist Pflicht")
        fehlend = [f for f in FELDER if f not in d["erwartet"]]
        if fehlend:
            raise ValueError(f"{pfad.name}: erwartet nennt nicht {fehlend}")
        return cls(d["id"], d["eingabe"], d["erwartet"], d["warum"])


@dataclass
class Ergebnis:
    fall: str
    felder: dict[str, bool]
    tatsaechlich: dict[str, Any]
    fehler: str | None = None

    @property
    def gruen(self) -> bool:
        return self.fehler is None and all(self.felder.values())


@dataclass
class Lauf:
    provider: str
    zeitpunkt: str
    ergebnisse: list[Ergebnis] = field(default_factory=list)

    def genauigkeit(self) -> dict[str, float]:
        """Anteil richtiger Fälle je Feld - das ist die Zahl, die zählt."""
        return {
            f: round(sum(e.felder.get(f, False) for e in self.ergebnisse) / len(self.ergebnisse), 3)
            for f in FELDER
        }

    def als_dict(self) -> dict[str, Any]:
        return {
            "provider": self.provider,
            "zeitpunkt": self.zeitpunkt,
            "genauigkeit": self.genauigkeit(),
            "faelle": {
                e.fall: {"gruen": e.gruen, "felder": e.felder, "fehler": e.fehler}
                for e in self.ergebnisse
            },
        }


def _normalisiert(feld: str, wert: Any) -> Any:
    """Vergleichbar machen: Datum als Text, Frequenz als Paar."""
    if wert is None:
        return None
    if feld == "frequenz":
        if isinstance(wert, dict):
            return (wert.get("min_pro_woche"), wert.get("max_pro_woche"))
        return (wert.min_pro_woche, wert.max_pro_woche)
    if feld == "ausstellungsdatum":
        return str(wert)
    return wert


def vergleiche(fall: Fall, v: VerordnungVorschlag) -> Ergebnis:
    felder = {}
    tatsaechlich = {}
    for f in FELDER:
        ist = _normalisiert(f, getattr(v, f))
        soll = _normalisiert(f, fall.erwartet[f])
        felder[f] = ist == soll
        tatsaechlich[f] = ist if not isinstance(ist, tuple) else list(ist)
    return Ergebnis(fall.id, felder, tatsaechlich)


def fuehre_aus(faelle: list[Fall], provider: Provider) -> Lauf:
    lauf = Lauf(provider.name, datetime.now(UTC).isoformat(timespec="seconds"))
    for fall in faelle:
        try:
            lauf.ergebnisse.append(vergleiche(fall, erfasse(fall.eingabe, provider).vorschlag))
        except Exception as e:  # noqa: BLE001 - ein kaputter Fall darf die Suite nicht beenden
            lauf.ergebnisse.append(
                Ergebnis(fall.id, dict.fromkeys(FELDER, False), {}, f"{type(e).__name__}: {e}")
            )
    return lauf


def lade_faelle(verzeichnis: Path = FAELLE) -> list[Fall]:
    return [Fall.lade(p) for p in sorted(verzeichnis.glob("*.json"))]


def regressionen(neu: Lauf, alt: dict[str, Any] | None) -> list[str]:
    """Fälle, die im letzten Lauf grün waren und jetzt rot sind."""
    if not alt:
        return []
    return [
        e.fall for e in neu.ergebnisse if alt["faelle"].get(e.fall, {}).get("gruen") and not e.gruen
    ]


def bericht(lauf: Lauf, faelle: list[Fall], alt: dict[str, Any] | None) -> str:
    zeilen = [f"Eval gegen {lauf.provider}, {len(faelle)} Fälle", ""]
    if alt is None:
        # Ohne Basislinie gibt es keine Regressionen - und das muss dastehen,
        # sonst sieht ein Lauf ohne Vergleich aus wie einer ohne Befund.
        zeilen.append("  KEINE BASISLINIE - Regressionen können nicht erkannt werden.")
        zeilen.append("")
    for f, g in lauf.genauigkeit().items():
        vorher = alt["genauigkeit"].get(f) if alt else None
        delta = "" if vorher is None else f"  (vorher {vorher:.1%})"
        zeilen.append(f"  {f:22s} {g:6.1%}{delta}")
    rot = [e for e in lauf.ergebnisse if not e.gruen]
    zeilen += ["", f"{len(faelle) - len(rot)} von {len(faelle)} Fällen grün."]
    warum = {f.id: f.warum for f in faelle}
    soll = {f.id: f.erwartet for f in faelle}
    for e in rot:
        falsch = [f for f, ok in e.felder.items() if not ok]
        zeilen.append(
            f"  rot  {e.fall}: {', '.join(falsch)}" + (f" - {e.fehler}" if e.fehler else "")
        )
        for f in falsch:
            # Was das Modell sagte, neben dem, was der Fall erwartet - sonst
            # ist "rot" nur ein Wort.
            ist = e.tatsaechlich.get(f)
            zeilen.append(f"       {f}: ist {_kurz(ist)}, soll {_kurz(soll[e.fall][f])}")
        zeilen.append(f"       warum: {warum[e.fall]}")
    neu_rot = regressionen(lauf, alt)
    if neu_rot:
        zeilen += ["", "NEU GESCHEITERT gegenüber dem letzten Lauf:"]
        for id_ in neu_rot:
            zeilen.append(f"  {id_}: {warum[id_]}")
    return "\n".join(zeilen)


def _kurz(wert: Any) -> str:
    if wert is None:
        return "nicht extrahierbar"
    if isinstance(wert, dict):
        return f"{wert.get('min_pro_woche')}-{wert.get('max_pro_woche')}/Woche"
    if isinstance(wert, list | tuple):
        return f"{wert[0]}-{wert[1]}/Woche"
    return str(wert)


def provider_aus(name: str) -> Provider:
    if name.startswith("aufzeichnung:"):
        return AufgezeichneterProvider.aus_datei(name.split(":", 1)[1])
    import os

    os.environ["AI_ASSIST_PROVIDER"] = name
    from ai_assist.provider import aus_umgebung

    return aus_umgebung()


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(
        description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter
    )
    parser.add_argument(
        "--provider", default="anthropic", help="anthropic, openai oder aufzeichnung:<datei>"
    )
    parser.add_argument("--faelle", type=Path, default=FAELLE)
    parser.add_argument("--ergebnisse", type=Path, default=ERGEBNISSE)
    parser.add_argument(
        "--nicht-speichern", action="store_true", help="letzten Lauf nicht überschreiben"
    )
    args = parser.parse_args(argv)

    faelle = lade_faelle(args.faelle)
    provider = provider_aus(args.provider)
    ziel = args.ergebnisse / (provider.name.split(":")[0] + ".json")
    alt = json.loads(ziel.read_text(encoding="utf-8")) if ziel.exists() else None

    lauf = fuehre_aus(faelle, provider)
    print(bericht(lauf, faelle, alt))

    if not args.nicht_speichern:
        args.ergebnisse.mkdir(parents=True, exist_ok=True)
        ziel.write_text(
            json.dumps(lauf.als_dict(), ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
        )
    # Rot ist ein Befund, keine Katastrophe. Neu gescheitert ist eine.
    return 1 if regressionen(lauf, alt) else 0


if __name__ == "__main__":
    sys.exit(main())
