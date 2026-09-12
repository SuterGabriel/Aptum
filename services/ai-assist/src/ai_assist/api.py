"""Die HTTP-Schnittstelle: ein Endpunkt, der vorschlägt.

Der Mandant kommt wie im Scheduling-Dienst aus X-Mandant - Platzhalter für
Authentifizierung - und wird nur protokolliert, nie an das Modell gegeben.
"""

import logging
from functools import lru_cache
from typing import Annotated

from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic import BaseModel, Field

from ai_assist.erfassung import erfasse
from ai_assist.protokoll import einrichten
from ai_assist.provider import Provider, ProviderFehler, aus_umgebung
from ai_assist.schema import Erfassung

# JSON auf stdout, eine Zeile je Ereignis, mit Mandant als Feld (DATENSCHUTZ.md, Regel 5).
einrichten()
log = logging.getLogger("ai_assist")

app = FastAPI(
    title="Aptum AI-Assist",
    version="0.1",
    description=(
        "Schlägt vor, entscheidet nicht. Liest den Freitext einer Heilmittelverordnung "
        "in Felder; ob daraus eine Verordnung wird, entscheidet der Scheduling-Dienst."
    ),
)


class Freitext(BaseModel):
    text: str = Field(min_length=1, max_length=4000)


@lru_cache
def provider() -> Provider:
    return aus_umgebung()


@app.get("/health")
def health() -> dict[str, str]:
    return {"status": "UP"}


@app.post("/erfassung", response_model=Erfassung)
def erfassung(
    freitext: Freitext,
    x_mandant: Annotated[str, Header(alias="X-Mandant")],
    p: Annotated[Provider, Depends(provider)],
) -> Erfassung:
    # Regel 5 aus DATENSCHUTZ.md: der Mandant als Feld, die Länge als Zahl -
    # und der Text selbst in keiner Zeile, denn er trägt den Personenbezug.
    log.info("erfassung", extra={"mandant": x_mandant, "zeichen": len(freitext.text)})
    try:
        return erfasse(freitext.text, p)
    except ProviderFehler as fehler:
        raise HTTPException(status_code=502, detail=str(fehler)) from fehler
