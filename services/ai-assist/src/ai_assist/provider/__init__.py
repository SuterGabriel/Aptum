"""Die Provider: ein Interface, drei Implementierungen.

Anthropic und ein OpenAI-kompatibler Provider (auch Azure) für den Betrieb
und die Evals; die Aufzeichnung für Tests, die ohne Schlüssel und ohne Netz
laufen. Der Vergleich zwischen den beiden echten ist selbst ein Ergebnis:
Er zeigt, wo der Prompt schwach ist statt das Modell.
"""

from ai_assist.provider.aufzeichnung import AufgezeichneterProvider
from ai_assist.provider.basis import Provider, ProviderFehler

__all__ = ["AufgezeichneterProvider", "Provider", "ProviderFehler", "aus_umgebung"]


def aus_umgebung() -> Provider:
    """Wählt den Provider über AI_ASSIST_PROVIDER: anthropic, openai oder aufzeichnung."""
    import os

    name = os.environ.get("AI_ASSIST_PROVIDER", "anthropic")
    if name == "anthropic":
        from ai_assist.provider.anthropic_provider import AnthropicProvider

        return AnthropicProvider()
    if name == "openai":
        from ai_assist.provider.openai_provider import OpenAIProvider

        return OpenAIProvider()
    if name == "aufzeichnung":
        return AufgezeichneterProvider.aus_datei(os.environ["AI_ASSIST_AUFZEICHNUNG"])
    raise ProviderFehler(f"Unbekannter Provider: {name}")
