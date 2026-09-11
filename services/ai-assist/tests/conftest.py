"""Gemeinsame Schalter für die Testläufe."""

import pytest


def pytest_addoption(parser: pytest.Parser) -> None:
    parser.addoption(
        "--openapi-aktualisieren",
        action="store_true",
        default=False,
        help="Das OpenAPI-Dokument im Repo neu schreiben, statt es nur zu vergleichen.",
    )
