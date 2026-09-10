"""Pseudonymisierung vor jedem LLM-Aufruf - Regel 2 aus DATENSCHUTZ.md.

Ein benannter Schritt, keine Zeile in einer langen Methode. Direkt
identifizierende Angaben werden durch Platzhalter ersetzt, bevor der Text
das System verlässt, und in der Antwort zurückgesetzt. Für die Extraktion
einer Verordnung braucht das Modell keinen Namen: Heilmittel, Menge und
Frequenz stehen nicht im Namen des Patienten.

Was hier erkannt wird, ist bewusst schmal und benannt. Eine echte Anwendung
bräuchte eine geprüfte Erkennung; dieses Projekt zeigt den Schritt und
seinen Platz in der Pipeline.
"""

import re
from dataclasses import dataclass, field

# Reihenfolge ist Absicht: erst das Spezifische (Versichertennummer,
# Geburtsdatum), dann Namen - damit "geb." nicht schon als Name gelesen wurde.
_MUSTER: list[tuple[str, re.Pattern[str]]] = [
    ("VERSICHERTENNUMMER", re.compile(r"\b[A-Z]\d{9}\b")),
    (
        "GEBURTSDATUM",
        re.compile(
            r"(?<=\bgeb\.\s)\d{1,2}\.\d{1,2}\.\d{4}|(?<=\bgeboren am\s)\d{1,2}\.\d{1,2}\.\d{4}"
        ),
    ),
    ("ARZT", re.compile(r"\bDr\.(?:\s*med\.)?\s+[A-ZÄÖÜ][a-zäöüß]+(?:\s+[A-ZÄÖÜ][a-zäöüß]+)?")),
    (
        "PATIENT",
        re.compile(
            r"(?<=\bPatient:\s)[A-ZÄÖÜ][a-zäöüß]+(?:\s+[A-ZÄÖÜ][a-zäöüß]+)+|(?<=\bPatientin:\s)[A-ZÄÖÜ][a-zäöüß]+(?:\s+[A-ZÄÖÜ][a-zäöüß]+)+"
        ),
    ),
]


@dataclass
class Pseudonymisiert:
    """Der Text ohne Personenbezug und die Tabelle, um ihn zurückzusetzen."""

    text: str
    ersetzungen: dict[str, str] = field(default_factory=dict)

    @property
    def anzahl(self) -> int:
        return len(self.ersetzungen)

    def zurueck(self, text: str) -> str:
        """Setzt Platzhalter in einer Antwort zurück - falls das Modell einen zitiert."""
        for platzhalter, original in self.ersetzungen.items():
            text = text.replace(platzhalter, original)
        return text


def pseudonymisiere(text: str) -> Pseudonymisiert:
    ersetzungen: dict[str, str] = {}
    laufend: dict[str, int] = {}

    def ersetze(art: str, treffer: re.Match[str]) -> str:
        original = treffer.group(0)
        # Dieselbe Angabe bekommt denselben Platzhalter - sonst zählt das
        # Modell zwei Patienten, wo einer zweimal genannt wird.
        for platzhalter, bekannt in ersetzungen.items():
            if bekannt == original:
                return platzhalter
        laufend[art] = laufend.get(art, 0) + 1
        platzhalter = f"[{art}-{laufend[art]}]"
        ersetzungen[platzhalter] = original
        return platzhalter

    for art, muster in _MUSTER:
        text = muster.sub(lambda m, art=art: ersetze(art, m), text)  # type: ignore[misc]
    return Pseudonymisiert(text=text, ersetzungen=ersetzungen)
