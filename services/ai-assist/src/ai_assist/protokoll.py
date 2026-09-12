"""Strukturiertes Protokoll: eine JSON-Zeile je Ereignis, mit Mandant, ohne Personenbezug.

Regel 5 aus DATENSCHUTZ.md: Die Mandanten-ID ist für die Fehlersuche nötig,
Namen und Diagnosen sind es nicht. Deshalb steht der Mandant als eigenes
Feld in jeder Zeile - und der Freitext einer Verordnung in keiner. Ein
Betrieb, der Logs zentral sammelt, filtert nach `mandant`, nicht nach
Textfragmenten.

JSON statt Prosa, weil eine Maschine es liest: Der Sammler (Loki, Elastic,
CloudWatch) braucht Felder, keine Sätze. Ohne Abhängigkeit, weil zwanzig
Zeilen reichen.
"""

import json
import logging
import sys
from datetime import UTC, datetime
from typing import Any

# Felder, die jede Zeile trägt. Alles andere kommt aus `extra=`.
_STANDARD = {"zeit", "stufe", "logger", "meldung"}


class JsonFormatter(logging.Formatter):
    def format(self, record: logging.LogRecord) -> str:
        zeile: dict[str, Any] = {
            "zeit": datetime.fromtimestamp(record.created, UTC).isoformat(timespec="milliseconds"),
            "stufe": record.levelname,
            "logger": record.name,
            "meldung": record.getMessage(),
        }
        # Was ein Aufrufer per extra= mitgibt (etwa mandant), landet als Feld.
        for name, wert in record.__dict__.items():
            if name in _EIGENE or name.startswith("_"):
                continue
            zeile[name] = wert
        if record.exc_info:
            zeile["fehler"] = self.formatException(record.exc_info)
        return json.dumps(zeile, ensure_ascii=False, default=str)


# Die Attribute, die jeder LogRecord ohnehin hat - keine Felder für uns.
_EIGENE = set(vars(logging.LogRecord("", 0, "", 0, "", (), None))) | {"message", "asctime"}


def einrichten(stufe: int = logging.INFO) -> None:
    """Ein Handler auf stdout, JSON, für alles. Auch uvicorn schreibt dann so."""
    handler = logging.StreamHandler(sys.stdout)
    handler.setFormatter(JsonFormatter())
    wurzel = logging.getLogger()
    wurzel.handlers[:] = [handler]
    wurzel.setLevel(stufe)
    for name in ("uvicorn", "uvicorn.access", "uvicorn.error"):
        logging.getLogger(name).handlers[:] = []
        logging.getLogger(name).propagate = True
