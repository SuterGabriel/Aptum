"""Die Pipeline: pseudonymisieren, fragen, prüfen, zurückgeben.

Vier benannte Schritte. Was das Modell liefert, ist ein Vorschlag; ob die
Verordnung angelegt wird, entscheidet der Scheduling-Dienst, wenn der
Vorschlag - bestätigt von einem Menschen - dort ankommt.
"""

from ai_assist.provider.basis import Provider
from ai_assist.pseudonymisierung import pseudonymisiere
from ai_assist.schema import Erfassung, VerordnungVorschlag


def erfasse(text: str, provider: Provider) -> Erfassung:
    if not text.strip():
        raise ValueError("Kein Text")

    # 1. Personenbezug raus, bevor der Text das System verlässt.
    ohne_bezug = pseudonymisiere(text)

    # 2. Das Modell fragen - es sieht nur den pseudonymisierten Text.
    vorschlag = provider.extrahiere(ohne_bezug.text)

    # 3. Nachprüfen, was das Schema allein nicht prüft.
    vorschlag = _konsistent(vorschlag)

    # 4. Platzhalter in freiem Text zurücksetzen. Die Felder brauchen das
    #    nicht: Ein Heilmittel hat keinen Namen.
    vorschlag.hinweise = [ohne_bezug.zurueck(h) for h in vorschlag.hinweise]

    return Erfassung(vorschlag=vorschlag, pseudonymisiert=ohne_bezug.anzahl, provider=provider.name)


def _konsistent(v: VerordnungVorschlag) -> VerordnungVorschlag:
    """Ein leeres Feld steht in nicht_extrahierbar, und umgekehrt - ohne Ausnahme."""
    leer = [
        name
        for name in VerordnungVorschlag.model_fields
        if _ist_feld(name) and getattr(v, name) is None
    ]
    for name in leer:
        if name not in v.nicht_extrahierbar:
            v.nicht_extrahierbar.append(name)  # type: ignore[arg-type]
    v.nicht_extrahierbar = [n for n in v.nicht_extrahierbar if getattr(v, n) is None]
    if v.frequenz and v.frequenz.min_pro_woche > v.frequenz.max_pro_woche:
        v.frequenz = None
        v.nicht_extrahierbar.append("frequenz")
        v.hinweise.append("Frequenz: Untergrenze über der Obergrenze.")
    return v


def _ist_feld(name: str) -> bool:
    return name not in ("nicht_extrahierbar", "hinweise")
