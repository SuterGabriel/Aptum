"""Der Eval-Runner, ohne Modell: Er misst richtig, findet Regressionen, prüft die Fälle."""

import importlib.util
import json
import sys
from pathlib import Path

import pytest

from ai_assist.provider import AufgezeichneterProvider

RUN_PY = Path(__file__).parents[3] / "evals" / "run.py"
FAELLE = Path(__file__).parents[3] / "evals" / "cases"

spec = importlib.util.spec_from_file_location("evals_run", RUN_PY)
assert spec and spec.loader
run = importlib.util.module_from_spec(spec)
# dataclasses mit future-annotations suchen ihr Modul in sys.modules - vor exec eintragen.
sys.modules[spec.name] = run
spec.loader.exec_module(run)


def test_alle_faelle_sind_vollstaendig_und_begruendet() -> None:
    faelle = run.lade_faelle(FAELLE)
    assert len(faelle) >= 40, "die Suite braucht vierzig bis fünfzig Fälle"
    assert all(f.warum for f in faelle)
    # Was der Skill verlangt: unleserlich, fehlend, widersprüchlich.
    ids = {f.id for f in faelle}
    assert {"unleserlich-01", "diagnosegruppe-fehlt", "menge-widerspruch"} <= ids


def test_ein_fall_ohne_warum_wird_abgelehnt(tmp_path: Path) -> None:
    (tmp_path / "x.json").write_text(
        json.dumps(
            {"id": "x", "eingabe": "KG", "erwartet": dict.fromkeys(run.FELDER), "warum": ""}
        ),
        encoding="utf-8",
    )
    with pytest.raises(ValueError, match="warum"):
        run.lade_faelle(tmp_path)


def test_misst_feldweise_und_nennt_den_falschen() -> None:
    faelle = [f for f in run.lade_faelle(FAELLE) if f.id == "frequenz-spanne-01"]
    provider = AufgezeichneterProvider(
        {
            faelle[0].eingabe: {
                "heilmittel": "KG_EINZEL",
                "diagnosegruppe": "WS",
                "verordnete_einheiten": 6,
                "frequenz": {"min_pro_woche": 2, "max_pro_woche": 2},  # falsch: Spanne verloren
                "ausstellungsdatum": "2026-02-27",
            }
        }
    )
    lauf = run.fuehre_aus(faelle, provider)
    assert lauf.genauigkeit()["heilmittel"] == 1.0
    assert lauf.genauigkeit()["frequenz"] == 0.0
    assert not lauf.ergebnisse[0].gruen
    text = run.bericht(lauf, faelle, None)
    assert "rot  frequenz-spanne-01: frequenz" in text
    assert "Frequenzspanne statt fester Zahl" in text


def test_regression_ist_ein_fall_der_vorher_gruen_war() -> None:
    faelle = [f for f in run.lade_faelle(FAELLE) if f.id == "datum-fehlt"]
    richtig = {
        "heilmittel": "KG_EINZEL",
        "diagnosegruppe": "WS",
        "verordnete_einheiten": 6,
        "frequenz": {"min_pro_woche": 2, "max_pro_woche": 2},
    }
    vorher = run.fuehre_aus(faelle, AufgezeichneterProvider({faelle[0].eingabe: richtig}))
    jetzt = run.fuehre_aus(
        faelle,
        AufgezeichneterProvider(
            {faelle[0].eingabe: {**richtig, "ausstellungsdatum": "2026-09-11"}}
        ),
    )
    assert run.regressionen(jetzt, vorher.als_dict()) == ["datum-fehlt"]
    assert run.regressionen(vorher, jetzt.als_dict()) == []


def test_ohne_basislinie_sagt_der_bericht_das_laut() -> None:
    faelle = run.lade_faelle(FAELLE)[:1]
    lauf = run.fuehre_aus(faelle, AufgezeichneterProvider({}))
    assert "KEINE BASISLINIE" in run.bericht(lauf, faelle, None)
    assert "KEINE BASISLINIE" not in run.bericht(lauf, faelle, lauf.als_dict())


def test_ein_kaputter_fall_beendet_die_suite_nicht() -> None:
    faelle = run.lade_faelle(FAELLE)[:2]
    lauf = run.fuehre_aus(faelle, AufgezeichneterProvider({}))
    assert len(lauf.ergebnisse) == 2
    assert all(e.fehler and "ProviderFehler" in e.fehler for e in lauf.ergebnisse)
