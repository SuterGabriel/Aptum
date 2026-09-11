# Der AI-Dienst: Python 3.12, FastAPI, uv.
#
# Das Basisbild bringt uv mit; die Abhängigkeiten kommen aus dem Lock, nicht
# aus einer Auflösung zur Bauzeit - dasselbe Bild heute und in einem Jahr.
# Ohne Entwicklungswerkzeuge (kein pytest, kein mypy) und nicht editierbar
# installiert, damit der Code im Bild liegt und nicht auf einen Pfad zeigt.

FROM ghcr.io/astral-sh/uv:python3.12-bookworm-slim
WORKDIR /app

# Erst die Abhängigkeiten, dann der Code - die erste Schicht bleibt im Cache.
COPY pyproject.toml uv.lock README.md ./
RUN uv sync --frozen --no-dev --no-install-project

COPY src ./src
COPY demo-aufzeichnung.json ./
RUN uv sync --frozen --no-dev --no-editable

# Kein Root im Container.
RUN useradd --system --no-create-home aptum && chown -R aptum:aptum /app
USER aptum

EXPOSE 8090

# Das schlanke Bild hat kein curl; Python ist da.
HEALTHCHECK --interval=10s --timeout=3s --start-period=10s --retries=5 \
  CMD ["/app/.venv/bin/python", "-c", "import urllib.request; urllib.request.urlopen('http://localhost:8090/health')"]

# Ohne Schlüssel antwortet der Dienst aus der Aufzeichnung; mit Schlüssel
# setzt compose den Provider um. Der Dienst selbst hat keine Meinung dazu.
ENV AI_ASSIST_PROVIDER=aufzeichnung \
    AI_ASSIST_AUFZEICHNUNG=/app/demo-aufzeichnung.json

CMD ["/app/.venv/bin/uvicorn", "ai_assist.api:app", "--host", "0.0.0.0", "--port", "8090"]
