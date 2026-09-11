"""Die echten Provider ohne Netz und ohne Schlüssel.

Beide nehmen ihren Client im Konstruktor - genau dafür. Geprüft wird, was
dieser Code selbst tut: das Werkzeug erzwingen, die Eingabe zum Vorschlag
machen, und laut werden, wenn das Modell am Werkzeug vorbei antwortet. Ob
das Modell gut extrahiert, misst der Eval, nicht dieser Test.
"""

from types import SimpleNamespace
from typing import Any

import pytest

from ai_assist.provider.anthropic_provider import AnthropicProvider
from ai_assist.provider.basis import WERKZEUG_NAME, ProviderFehler
from ai_assist.provider.openai_provider import OpenAIProvider

FELDER = {"heilmittel": "KG_EINZEL", "verordnete_einheiten": 6}


class AnthropicStub:
    """So wenig vom SDK wie nötig: eine Antwort mit Blöcken."""

    def __init__(self, bloecke: list[Any]) -> None:
        self.bloecke = bloecke
        self.aufrufe: list[dict[str, Any]] = []
        self.messages = SimpleNamespace(create=self._create)

    def _create(self, **kwargs: Any) -> Any:
        self.aufrufe.append(kwargs)
        return SimpleNamespace(content=self.bloecke)


def anthropic_block(name: str = WERKZEUG_NAME, eingabe: dict[str, Any] | None = None) -> Any:
    return SimpleNamespace(
        type="tool_use", name=name, input=eingabe if eingabe is not None else FELDER
    )


class OpenAIStub:
    def __init__(self, aufrufe_zurueck: list[Any]) -> None:
        self.gesendet: list[dict[str, Any]] = []
        nachricht = SimpleNamespace(tool_calls=aufrufe_zurueck)
        self._antwort = SimpleNamespace(choices=[SimpleNamespace(message=nachricht)])
        self.chat = SimpleNamespace(completions=SimpleNamespace(create=self._create))

    def _create(self, **kwargs: Any) -> Any:
        self.gesendet.append(kwargs)
        return self._antwort


def openai_aufruf(name: str = WERKZEUG_NAME, argumente: str = '{"heilmittel": "KG_EINZEL"}') -> Any:
    return SimpleNamespace(
        type="function", function=SimpleNamespace(name=name, arguments=argumente)
    )


def test_anthropic_erzwingt_das_werkzeug_und_liest_seine_eingabe() -> None:
    stub = AnthropicStub([anthropic_block()])
    provider = AnthropicProvider(modell="testmodell", client=stub)  # type: ignore[arg-type]

    vorschlag = provider.extrahiere("KG 6x")

    assert vorschlag.heilmittel == "KG_EINZEL"
    assert vorschlag.verordnete_einheiten == 6
    assert provider.name == "anthropic:testmodell"
    # Das Modell darf nicht wählen, ob es antwortet: tool_choice zeigt auf das Werkzeug.
    gesendet = stub.aufrufe[0]
    assert gesendet["tool_choice"] == {"type": "tool", "name": WERKZEUG_NAME}
    assert gesendet["messages"] == [{"role": "user", "content": "KG 6x"}]


def test_anthropic_meldet_eine_antwort_am_werkzeug_vorbei() -> None:
    nur_text = SimpleNamespace(type="text", text="Ich denke, es ist Krankengymnastik.")
    provider = AnthropicProvider(client=AnthropicStub([nur_text]))  # type: ignore[arg-type]
    with pytest.raises(ProviderFehler, match="Werkzeug"):
        provider.extrahiere("KG 6x")


def test_anthropic_wirft_ein_erfundenes_feld_heraus() -> None:
    stub = AnthropicStub([anthropic_block(eingabe={**FELDER, "erfunden": "n/a"})])
    vorschlag = AnthropicProvider(client=stub).extrahiere("KG 6x")  # type: ignore[arg-type]
    assert vorschlag.heilmittel == "KG_EINZEL"
    assert any("erfunden" in h for h in vorschlag.hinweise)


def test_openai_erzwingt_dieselbe_funktion() -> None:
    stub = OpenAIStub([openai_aufruf()])
    provider = OpenAIProvider(modell="testmodell", client=stub)  # type: ignore[arg-type]

    vorschlag = provider.extrahiere("KG 6x")

    assert vorschlag.heilmittel == "KG_EINZEL"
    assert provider.name == "openai:testmodell"
    gesendet = stub.gesendet[0]
    assert gesendet["tool_choice"] == {"type": "function", "function": {"name": WERKZEUG_NAME}}
    assert gesendet["messages"][1] == {"role": "user", "content": "KG 6x"}


def test_openai_ignoriert_einen_fremden_werkzeugaufruf() -> None:
    provider = OpenAIProvider(client=OpenAIStub([openai_aufruf(name="etwas_anderes")]))  # type: ignore[arg-type]
    with pytest.raises(ProviderFehler, match="Werkzeug"):
        provider.extrahiere("KG 6x")


def test_openai_ohne_werkzeugaufruf() -> None:
    provider = OpenAIProvider(client=OpenAIStub([]))  # type: ignore[arg-type]
    with pytest.raises(ProviderFehler, match="Werkzeug"):
        provider.extrahiere("KG 6x")
