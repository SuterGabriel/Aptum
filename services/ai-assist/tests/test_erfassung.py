import pytest

from ai_assist.erfassung import erfasse
from ai_assist.provider import AufgezeichneterProvider

TEXT = "Patient: Testfall Alpha, geb. 01.01.1900. KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026."
OHNE_BEZUG = (
    "Patient: [PATIENT-1], geb. [GEBURTSDATUM-1]. KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026."
)


def provider(antwort: dict[str, object]) -> AufgezeichneterProvider:
    return AufgezeichneterProvider({OHNE_BEZUG: antwort})


def test_das_modell_sieht_nur_den_pseudonymisierten_text() -> None:
    # Der Provider kennt nur die pseudonymisierte Fassung. Bekäme er den
    # Originaltext, gäbe es keine Aufzeichnung dafür - und der Test wäre rot.
    e = erfasse(
        TEXT,
        provider(
            {
                "heilmittel": "KG_EINZEL",
                "diagnosegruppe": "WS",
                "verordnete_einheiten": 6,
                "frequenz": {"min_pro_woche": 2, "max_pro_woche": 2},
                "ausstellungsdatum": "2026-02-27",
            }
        ),
    )
    assert e.pseudonymisiert == 2
    assert e.vorschlag.heilmittel == "KG_EINZEL"
    assert e.vorschlag.verordnete_einheiten == 6


def test_leere_felder_stehen_in_nicht_extrahierbar_auch_wenn_das_modell_sie_vergisst() -> None:
    e = erfasse(TEXT, provider({"heilmittel": "KG_EINZEL"}))
    assert "diagnosegruppe" in e.vorschlag.nicht_extrahierbar
    assert "hausbesuch" in e.vorschlag.nicht_extrahierbar
    assert "heilmittel" not in e.vorschlag.nicht_extrahierbar


def test_widerspruechliche_frequenz_wird_geleert_statt_uebernommen() -> None:
    e = erfasse(TEXT, provider({"frequenz": {"min_pro_woche": 3, "max_pro_woche": 1}}))
    assert e.vorschlag.frequenz is None
    assert "frequenz" in e.vorschlag.nicht_extrahierbar
    assert any("Frequenz" in h for h in e.vorschlag.hinweise)


def test_platzhalter_in_hinweisen_werden_zurueckgesetzt() -> None:
    e = erfasse(TEXT, provider({"hinweise": ["Angabe zu [PATIENT-1] unklar"]}))
    assert e.vorschlag.hinweise == ["Angabe zu Testfall Alpha unklar"]


def test_ein_erfundenes_feld_kippt_nicht_den_vorschlag() -> None:
    # Im zweiten Eval-Lauf lieferte das Modell "verordnungsdatum_hinweis": "n/a",
    # und der Fall starb an extra_forbidden - alle sieben Felder rot, obwohl
    # sie stimmten. Das Feld fliegt raus und wird benannt.
    e = erfasse(TEXT, provider({"heilmittel": "KG_EINZEL", "verordnungsdatum_hinweis": "n/a"}))
    assert e.vorschlag.heilmittel == "KG_EINZEL"
    assert "Modell lieferte ein unbekanntes Feld: verordnungsdatum_hinweis" in e.vorschlag.hinweise


def test_unbekanntes_heilmittel_scheitert_am_schema() -> None:
    from pydantic import ValidationError

    with pytest.raises(ValidationError):
        erfasse(TEXT, provider({"heilmittel": "AKUPUNKTUR"}))


def test_leerer_text_wird_abgelehnt() -> None:
    with pytest.raises(ValueError):
        erfasse("   ", provider({}))
