# Die AI-Pipeline

Wie ein Sprachmodell in Aptum eingesetzt wird — und wo es nicht das letzte
Wort hat. Der Dienst dazu ist `services/ai-assist/` (Python); die Regel, die
alles bestimmt, steht in `CLAUDE.md`:

> Der AI-Layer schlägt vor, die Domäne entscheidet.

## Feature 1: Verordnungserfassung aus Freitext

Der abgetippte oder eingescannte Text einer Heilmittelverordnung wird zu
Feldern: Heilmittel, Diagnosegruppe, Menge, Frequenz, Ausstellungsdatum,
dringlicher Bedarf, Hausbesuch. Vier benannte Schritte, in dieser Reihenfolge
(`src/ai_assist/erfassung.py`):

| Schritt | Was passiert | Warum hier |
|---|---|---|
| 1. Pseudonymisieren | Name, Geburtsdatum, Versichertennummer, Arzt werden durch Platzhalter ersetzt | Regel 2 aus `DATENSCHUTZ.md`: nichts Identifizierendes verlässt das System. Ein Heilmittel steht nicht im Namen des Patienten. |
| 2. Fragen | Der Provider bekommt Prompt und pseudonymisierten Text; die Antwort ist erzwungen über ein Werkzeug mit dem Schema | Structured Output statt Freitext-Parsing. Ein Schema aus einem Pydantic-Modell, für beide Provider. |
| 3. Prüfen | Schema-Validierung; ein leeres Feld steht in `nicht_extrahierbar`, eine unmögliche Frequenz wird geleert | Was das Modell vergisst, setzt die Pipeline durch. Unbekannte Codes scheitern hier, nicht in der Domäne. |
| 4. Zurückgeben | Platzhalter in freiem Text zurücksetzen, Vorschlag mit Zähler und Provider-Name | Der Aufrufer sieht, was auf dem Weg geschah. |

Was das Modell **nicht** tut: entscheiden, ob die Verordnung gültig ist. Der
Vorschlag geht — bestätigt von einem Menschen — an `POST /verordnungen` im
Scheduling-Dienst, und dort prüft dieselbe Domäne wie bei einer Eingabe von
Hand. Für Terminvorschläge gilt dasselbe über `POST /termine/pruefung`: Kein
Vorschlag wird angezeigt, der nicht durch das Regelwerk gelaufen ist.

## Die Regel, die alles trägt: „nicht extrahierbar" statt raten

Ein Feld, das im Text fehlt oder sich widerspricht, bleibt leer und wird
benannt. Ein Modell, das bei fehlender Diagnosegruppe „WS" einträgt, weil WS
die häufigste ist, hat eine fachliche Entscheidung getroffen, die ihm nicht
zusteht — und die im Kalender später als falsche Frist auftaucht. Der Prompt
(`src/ai_assist/prompts/erfassung.md`) sagt das in acht Regeln; die Evals
prüfen es in einem Drittel der Fälle.

## Provider

Ein Interface, drei Implementierungen (`src/ai_assist/provider/`):

- **Anthropic** über das Anthropic SDK, Tool-Use mit `tool_choice` erzwungen.
- **OpenAI-kompatibel**, auch Azure OpenAI über `base_url` — dasselbe
  Werkzeug, dasselbe Schema.
- **Aufzeichnung** für Tests ohne Schlüssel und ohne Netz. Sie prüft die
  Pipeline, nicht das Modell.

Der Vergleich zwischen den beiden echten ist selbst ein Ergebnis: Wo sie sich
unterscheiden, ist der Prompt zu schwach, nicht das Modell zu schlecht.

## Evals

`evals/` — 55 Fälle mit Pflichtfeld `warum`, absichtlich unausgewogen:
mehr Grenzfälle als Normalfälle, weil Normalfälle ohnehin funktionieren.
`evals/run.py` misst feldweise, nicht als Gesamtnote, vergleicht mit dem
letzten Lauf und nennt neu gescheiterte Fälle einzeln. Der CI-Job `evals`
läuft nur bei Änderungen an Prompt, Schema, Provider oder Fällen; ohne
Schlüssel wird er sichtbar übersprungen, nie stumm grün. Details im Skill
`.claude/skills/llm-evals/SKILL.md`.

## Der erste Lauf

CI-Lauf #30 am 11. September 2026, `claude-sonnet-5`, 55 Fälle: 43 grün.
Feldweise: Ausstellungsdatum 100 %, Diagnosegruppe 98 %, dringlicher Bedarf
98 %, Hausbesuch 98 %, Frequenz 96 %, Menge 95 %, **Heilmittel 89 %**. Fünf
der zwölf roten Fälle liegen im Heilmittel an derselben Stelle: MLD nach
Dauer (30/45/60), KGG, ZNS ohne Alter — Zuordnungen, die der Prompt nie
genannt hatte. Das ist die Aussage, für die man feldweise misst. Die
Basislinie liegt unter `evals/ergebnisse/anthropic.json`; jede
Prompt-Änderung wird dagegen verglichen.

Zweiter Lauf nach vier ergänzten Prompt-Regeln: 53 von 55, Heilmittel 100 %,
kein Feld unter 98 %. Ein Fall wurde dabei rot, der vorher grün war — und
die Regression stand nicht im Bericht, weil die Basislinie durch einen
Ignore-Eintrag nie im Repo lag. Der Runner sagt es jetzt laut, wenn ihm die
Basislinie fehlt.

## Der MCP-Server

`services/ai-assist/src/ai_assist/mcp_server.py` — vier Werkzeuge auf der
REST-Schnittstelle: `termine_suchen`, `termin_pruefen`, `termin_buchen`,
`woche_anzeigen`. Ein Sprachmodell kann damit eine Praxis bedienen, ohne dass
es einen zweiten Weg in die Domäne gäbe: Es ist dieselbe Schnittstelle, also
dasselbe Regelwerk.

Drei Einschränkungen kommen an dieser Grenze dazu:

| Einschränkung | Warum |
|---|---|
| Der Mandant ist kein Parameter, sondern kommt aus der Umgebung | Ein Modell soll die Praxis nicht wählen können. Ein Test prüft, dass das Wort in keinem Werkzeugschema steht. |
| Die Übersteuerung verlangt zehn Zeichen Begründung, als `minLength` im Schema | Das Modell sieht die Bedingung, bevor es aufruft — und die Begründung kommt von einem Menschen (ADR-009). |
| Fehler werden als `ToolError` geworfen | Nur dann liest das Modell den Grund. Jede andere Ausnahme erreicht es als „Error executing tool". |

Gegenprobe gegen das laufende Backend: suchen (1170 Slots geprüft, 462 wegen
der Therapeutin ausgeschlossen, 236 wegen der Raumausstattung), prüfen (11
Regeln, FREI), buchen, denselben Slot noch einmal buchen — BLOCKIERT mit
beiden verletzten Regeln samt Uhrzeiten einschließlich Rüstzeit —, mit
Begründung übersteuern, und eine Begründung „ok" wird vom Schema abgelehnt.

## Die Seite „Verordnung erfassen“

`frontend/src/app/erfassung/` — hier wird die Regel sichtbar. Freitext
einfügen, das Modell liest ihn in Felder und sagt, was es *nicht* lesen
konnte; diese Felder bleiben leer und sind markiert, mit Farbe **und** Wort.
Ein Mensch ergänzt und bestätigt, und erst dann geht die Verordnung an
`POST /verordnungen`, wo die Domäne prüft. Lehnt sie ab, steht die Ablehnung
als Alert da, nicht als Erfolg.

Zwei Angaben liest das Modell, die nicht zur Verordnung gehören: das
Heilmittel (das wird bei der Terminsuche gewählt) und der Hausbesuch (den
führt die Domäne nicht). Beide werden gezeigt, aber nicht als Lücke
gezählt — sonst fordert die Seite dazu auf, etwas zu ergänzen, wofür es
kein Feld gibt. Die Seite nennt außerdem, welches Modell geantwortet hat
und wie viele Angaben vor dem Aufruf ersetzt wurden: Die Pseudonymisierung
ist damit nicht nur ein Schritt im Code, sondern eine Zahl auf dem Schirm.

Der Ende-zu-Ende-Test fährt die ganze Kette mit aufgezeichneten Antworten
(`services/ai-assist/demo-aufzeichnung.json`) — ohne Schlüssel, ohne Netz.

## Was es noch nicht gibt

- **Retrieval.** Der Regelkatalog (`regeln.md`) wäre der naheliegende Korpus,
  etwa um dem Modell die Fundstelle zu einer Frist zu geben. Nicht gebaut,
  weil die Domäne die Fristen kennt und das Modell sie nicht braucht.
- **Eine geprüfte Pseudonymisierung.** Vier reguläre Ausdrücke zeigen den
  Schritt und seinen Platz; eine echte Anwendung bräuchte mehr.
