from ai_assist.pseudonymisierung import pseudonymisiere

# Erkennbar synthetisch (DATENSCHUTZ.md): kein Name, den je jemand trug,
# Versichertennummer ohne echtes Format.
TEXT = (
    "Patient: Testfall Alpha, geb. 01.01.1900, Vers.-Nr. Z000000001. "
    "Dr. med. Beispiel Beta verordnet KG 6x, 2x/Woche, WS, ausgestellt 27.02.2026."
)


def test_ersetzt_name_geburtsdatum_versichertennummer_und_arzt() -> None:
    p = pseudonymisiere(TEXT)
    assert "Testfall Alpha" not in p.text
    assert "01.01.1900" not in p.text
    assert "Z000000001" not in p.text
    assert "Beispiel Beta" not in p.text
    assert p.anzahl == 4


def test_laesst_das_ausstellungsdatum_stehen() -> None:
    # Das Datum, das die Verordnung braucht, bleibt; nur das Geburtsdatum geht.
    p = pseudonymisiere(TEXT)
    assert "27.02.2026" in p.text
    assert "[GEBURTSDATUM-1]" in p.text


def test_derselbe_name_bekommt_denselben_platzhalter() -> None:
    p = pseudonymisiere("Patient: Testfall Alpha. Termin mit Patient: Testfall Alpha.")
    assert p.text.count("[PATIENT-1]") == 2
    assert p.anzahl == 1


def test_zurueck_setzt_platzhalter_in_freiem_text() -> None:
    p = pseudonymisiere("Patient: Testfall Alpha, geb. 01.01.1900")
    assert p.zurueck("Hinweis zu [PATIENT-1]") == "Hinweis zu Testfall Alpha"


def test_text_ohne_personenbezug_bleibt_unveraendert() -> None:
    p = pseudonymisiere("KG 6x, 2x/Woche, WS, 27.02.2026")
    assert p.text == "KG 6x, 2x/Woche, WS, 27.02.2026"
    assert p.anzahl == 0
