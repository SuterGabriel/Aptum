#!/usr/bin/env bash
# Beleg-Check
#
# Dieses Repo behauptet an mehreren Stellen etwas über die eigene Arbeitsweise.
# Dieses Skript macht aus jeder Behauptung eine Zusage, die kaputtgehen kann.
#
# Es prüft absichtlich nur Existenz und einfache Struktur — keine Qualität.
# Ein Beleg, der fehlt, fällt hier auf. Ein Beleg, der schlecht ist, fällt im
# Review auf. Beides ist besser als eine Behauptung, die niemand prüft.

set -uo pipefail
cd "$(dirname "$0")/.."

fehler=0
ok=0

pruefe() {
  local beschreibung="$1"; shift
  if "$@" >/dev/null 2>&1; then
    printf '  ok    %s\n' "$beschreibung"
    ok=$((ok + 1))
  else
    printf '  FEHLT %s\n' "$beschreibung"
    fehler=$((fehler + 1))
  fi
}

datei() { [[ -f "$1" ]]; }
ordner_nicht_leer() { [[ -d "$1" ]] && [[ -n "$(ls -A "$1" 2>/dev/null)" ]]; }
enthaelt() { grep -q "$2" "$1" 2>/dev/null; }
hoechstens_zeilen() { [[ "$(wc -l < "$1")" -le "$2" ]]; }
existiert() { [[ -e "$1" ]]; }

# Liefert alle Repo-Pfade aus der Belegspalte von docs/ANFORDERUNGEN.md, die
# unbedingt behauptet werden.
#
# Unbedingt heißt: die Zeile trägt keinen Status, der die Sache als noch nicht
# eingelöst kennzeichnet. Zeilen mit `offen`, `zu klären`, `teilweise belegbar`
# oder `nicht belegbar` nennen in der Belegspalte ein Ziel, keinen Beleg — die
# dürfen auf etwas zeigen, das es noch nicht gibt.
#
# Die Aufgaben-Tabellen (SA*, CA*) trugen anfangs keine Statusspalte. Damit galt
# dort jede Zeile als Ist-Behauptung, und `services/` und `infra/` meldeten grün,
# obwohl beide nur `.gitkeep` enthielten — ein falsch positiver Beleg, also genau
# das, was dieses Skript verhindern soll. Seit die beiden Tabellen dieselbe
# Statusspalte tragen wie die Must-have-Tabellen, greift die Filterung auch dort.
#
# Offen bleibt die zweite Hälfte: `existiert` ist `[ -e ]` und ist bei einem
# Ordner mit nur `.gitkeep` zufrieden. Sobald der erste Service steht, wird
# daraus eine Prüfung auf echten Inhalt — vorher wäre sie nur rot.
behauptete_pfade() {
  grep '^|' docs/ANFORDERUNGEN.md \
    | grep -v '| offen |\|| zu klären |\|| teilweise belegbar |\|| nicht belegbar |' \
    | grep -oE '`[^`]+`' \
    | tr -d '`' \
    | grep -E '/|\.md$' \
    | grep -v '^/' \
    | sort -u
}

echo
echo "Beleg-Check"
echo "==========="
echo

echo "Schritt 1 — Anforderungen liegen vor dem Code"
pruefe "docs/ANFORDERUNGEN.md existiert" datei docs/ANFORDERUNGEN.md
pruefe "beide Ausschreibungen sind erfasst (SOLCOM)" enthaelt docs/ANFORDERUNGEN.md "SOLCOM"
pruefe "beide Ausschreibungen sind erfasst (consultingheads)" enthaelt docs/ANFORDERUNGEN.md "consultingheads"
pruefe "die nicht belegbaren Punkte sind benannt" enthaelt docs/ANFORDERUNGEN.md "nicht \*belegt\|nicht belegbar\|nicht \*\*nicht\*\*"
pruefe "docs/PRODUKT.md existiert" datei docs/PRODUKT.md

# Die Belegspalte ist der Ort, an dem sich das Mapping selbst überholen kann:
# Ein Pfad wird als Beleg eingetragen, die Datei entsteht nie. Von Hand fällt
# das niemandem auf, weil niemand eine Tabelle gegen den Verzeichnisbaum liest.
while read -r pfad; do
  [[ -n "$pfad" ]] || continue
  pruefe "Beleg $pfad existiert" existiert "$pfad"
done <<< "$(behauptete_pfade)"

echo
echo "Schritt 3 — Entscheidungen sind dokumentiert"
pruefe "DECISIONS.md existiert" datei DECISIONS.md
pruefe "mindestens eine ADR existiert" ordner_nicht_leer docs/adr
for adr in docs/adr/ADR-*.md; do
  [[ -e "$adr" ]] || continue
  pruefe "$(basename "$adr"): Abschnitt 'Wann wir anders entscheiden würden'" \
    enthaelt "$adr" "Wann wir anders entscheiden"
  pruefe "$(basename "$adr"): Abschnitt 'Optionen'" enthaelt "$adr" "## Optionen"
  pruefe "$(basename "$adr"): Abschnitt 'Konsequenzen'" enthaelt "$adr" "## Konsequenzen"
done

echo
echo "Schritt 4 — Regeln und Skills liegen im Repo, nicht global"
pruefe "CLAUDE.md existiert" datei CLAUDE.md
pruefe "CLAUDE.md bleibt auf einer Bildschirmseite (max. 60 Zeilen)" hoechstens_zeilen CLAUDE.md 60
for skill in heilmittel-domain java-spring-hexagonal angular-rxjs a11y-grid adr llm-evals; do
  pruefe "Skill $skill hat eine SKILL.md" datei ".claude/skills/$skill/SKILL.md"
done
pruefe "Commands liegen im Repo" ordner_nicht_leer .claude/commands
pruefe "Subagents liegen im Repo" ordner_nicht_leer .claude/agents
pruefe "Claude-Hook ist verdrahtet" datei .claude/settings.json
pruefe "Claude-Hook liegt im Repo" datei .claude/hooks/prosa-nach-schreiben.mjs

echo
echo "Stage 1 — die Gates laufen auch vor dem Commit"
# Ohne Git-Hook läuft die erste Prüfung erst auf GitHub, also nach dem Commit.
# Dass die Hooks versioniert im Repo liegen statt in .git/hooks/, ist Teil
# derselben Zusage wie bei den Skills: nichts Wichtiges liegt außerhalb.
pruefe "Git-Hook liegt im Repo" datei .githooks/pre-commit
pruefe "Einrichtung ist mitgeliefert" datei scripts/hooks-installieren.sh
pruefe "Prosa-Check liegt im Repo" datei scripts/prosa-check.mjs
pruefe "Verweis-Check liegt im Repo" datei scripts/link-check.mjs
pruefe "Regel-Check liegt im Repo" datei scripts/regel-check.mjs
pruefe "docs/PIPELINE.md existiert" datei docs/PIPELINE.md

echo
echo "Fachregeln — keine Zahl ohne Fundstelle"
# Die Zusage des Skills heilmittel-domain lautet: Was nicht mit Quelle und
# Status BELEGT in regeln.md steht, gehört nicht in den Code. Der Regel-Check
# prüft sie; hier steht nur, dass es ihn und seinen Katalog gibt.
pruefe "Regelkatalog existiert" datei .claude/skills/heilmittel-domain/regeln.md
pruefe "Regelzeilen tragen IDs" enthaelt .claude/skills/heilmittel-domain/regeln.md "HM-FRIST-01"
pruefe "ADR-007 begründet die Fundstellen-ID" datei docs/adr/ADR-007-fundstellen-id-im-domaenenmodell.md
# Ein Gate, das nichts meldet, sieht aus wie ein Gate, das zufrieden ist.
# Deshalb hat der Regel-Check Tests, und die beiden stummen Lücken sind Fixtures.
pruefe "Regel-Check hat eine Testsuite" datei scripts/regel-check.test.mjs

echo
echo "Was CLAUDE.md verlangt, gibt es auch"
# "ArchUnit prüft das bei jedem Lauf" und "Formatiert" standen in CLAUDE.md,
# bevor es beides gab. Zwei Behauptungen ohne Gate in der Datei, die jeder
# Agent zuerst liest. Jetzt sind sie geprüft.
pruefe "ArchUnit prüft ADR-001" datei services/scheduling/domain/src/test/java/de/aptum/scheduling/domain/ArchitekturTest.java
pruefe "Formatter ist im Build" enthaelt services/scheduling/pom.xml "spotless"
pruefe "ADR-009 entscheidet Blockieren gegen Warnen" datei docs/adr/ADR-009-blockieren-mit-uebersteuerung.md
# ADR-008 verspricht drei Module je Service, ADR-001 die Richtung dazwischen.
pruefe "Modul application existiert" datei services/scheduling/application/pom.xml
pruefe "Modul infrastructure existiert" datei services/scheduling/infrastructure/pom.xml
pruefe "ArchUnit prüft die Modulgrenzen" datei services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/ModulgrenzenTest.java

echo
echo "ADR-002 — Mandantentrennung ist geprüft, nicht behauptet"
pruefe "Migration mit Row Level Security" enthaelt services/scheduling/infrastructure/src/main/resources/db/migration/V1__mandantentrennung.sql "row level security"
pruefe "Isolationstest existiert" datei services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/mandant/MandantIsolationTest.java
pruefe "Mandant wird an genau einer Stelle gesetzt" datei services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/mandant/MandantTransactionManager.java

echo
echo "ADR-001 — Ports innen, Adapter außen"
pruefe "Repository-Port liegt im Domain-Modul" datei services/scheduling/domain/src/main/java/de/aptum/scheduling/domain/port/VerordnungRepository.java
pruefe "JPA-Adapter liegt in infrastructure" datei services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/persistenz/VerordnungRepositoryAdapter.java
pruefe "ArchUnit: keine Entity verlässt den Adapter" enthaelt services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/ModulgrenzenTest.java "keineEntityVerlaesstDenAdapter"
pruefe "Anwendungsfälle ohne Framework" datei services/scheduling/application/src/main/java/de/aptum/scheduling/application/anwendungsfall/TerminBuchen.java
pruefe "REST über dasselbe Regelwerk" datei services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/rest/TerminController.java
pruefe "Ende-zu-Ende-Test über HTTP" datei services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/rest/RestApiTest.java
pruefe "Header-Stub ist als offen geführt" enthaelt docs/OFFENE-PUNKTE.md "X-Mandant"
# Die Schnittstellenbeschreibung ist erzeugt, und ein Test vergleicht sie mit
# der laufenden Anwendung. Ohne den Test wäre sie die nächste Datei, die
# still veraltet.
pruefe "OpenAPI-Dokument liegt im Repo" datei docs/api/openapi.json
pruefe "OpenAPI wird gegen die Anwendung geprüft" datei services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/rest/OpenApiTest.java

echo
echo "Stufe 2 — das Frontend"
pruefe "ADR-003 entscheidet das Grid" datei docs/adr/ADR-003-kalender-grid-selbst-gebaut.md
pruefe "Angular-Gerüst existiert" datei frontend/angular.json
pruefe "Token liegen im Frontend" datei frontend/src/styles/tokens.css
pruefe "API-Typen sind aus OpenAPI erzeugt" datei frontend/src/app/api/schema.d.ts
pruefe "Suchflow ist der aus dem Skill" enthaelt frontend/src/app/suche/termin-suche.service.ts "switchMap"
pruefe "Suchflow wird einzeln getestet" datei frontend/src/app/suche/termin-suche.service.spec.ts
pruefe "Terminsuche hat einen axe-Test" enthaelt frontend/src/app/suche/termin-suche.spec.ts "axe.run"
pruefe "Grid ist ein Grid-Widget" enthaelt frontend/src/app/kalender/kalender-grid.html 'role="gridcell"'
pruefe "Grid hat Roving Tabindex und einen axe-Test" enthaelt frontend/src/app/kalender/kalender-grid.spec.ts "axe.run"
pruefe "Ende-zu-Ende fährt per Tastatur durch das Grid" enthaelt frontend/e2e/kalender.spec.ts "ArrowDown"
pruefe "CI hat den Job e2e" enthaelt .github/workflows/ci.yml "  e2e:"
pruefe "Dialog prüft, bevor er bucht" enthaelt frontend/src/app/buchung/buchungs-dialog.ts "pruefen"

echo
echo "Stufe 3 — der AI-Layer"
pruefe "Pseudonymisierung ist ein benannter Schritt" datei services/ai-assist/src/ai_assist/pseudonymisierung.py
pruefe "Pipeline pseudonymisiert vor dem Aufruf" enthaelt services/ai-assist/src/ai_assist/erfassung.py "pseudonymisiere(text)"
pruefe "Prompt ist eine Datei, keine Zeichenkette" datei services/ai-assist/src/ai_assist/prompts/erfassung.md
pruefe "zwei echte Provider" datei services/ai-assist/src/ai_assist/provider/openai_provider.py
pruefe "Eval-Suite misst feldweise" enthaelt evals/run.py "genauigkeit"
pruefe "Eval-Fälle: der unleserliche" datei evals/cases/unleserlich-01.json
pruefe "Eval-Fälle: die fehlende Angabe" datei evals/cases/diagnosegruppe-fehlt.json
pruefe "Eval-Fälle: der Widerspruch" datei evals/cases/menge-widerspruch.json
pruefe "CI hat den Job evals" enthaelt .github/workflows/ci.yml "  evals:"
pruefe "SonarCloud ist konfiguriert" datei sonar-project.properties
pruefe "CI hat den Job sonar" enthaelt .github/workflows/ci.yml "  sonar:"
pruefe "MCP-Server existiert" datei services/ai-assist/src/ai_assist/mcp_server.py
pruefe "MCP-Werkzeug bucht über dieselbe Schnittstelle" enthaelt services/ai-assist/src/ai_assist/mcp_server.py "/termine/pruefung"
pruefe "MCP-Server ist getestet" datei services/ai-assist/tests/test_mcp_server.py
pruefe "Seite zeigt den Vorschlag" datei frontend/src/app/erfassung/verordnung-erfassen.ts
pruefe "Der Mensch bestätigt, die Domäne prüft" enthaelt frontend/e2e/erfassung.spec.ts "Verordnung anlegen"
pruefe "AI-Schnittstelle ist erzeugt" datei docs/api/ai-assist-openapi.json
pruefe "AI-Dokument wird gegen die Anwendung geprüft" datei services/ai-assist/tests/test_openapi.py

echo
echo "Stufe 4 — der Betrieb"
pruefe "Scheduling als Container" datei services/scheduling/Dockerfile
pruefe "AI-Dienst als Container" datei services/ai-assist/Dockerfile
pruefe "Frontend als Container mit Reverse Proxy" datei frontend/nginx.conf
pruefe "Ein Befehl für alles" datei compose.yml
pruefe "Kein Root im Container, numerisch" enthaelt services/scheduling/Dockerfile "USER 10001"
pruefe "Anwendungsrolle vor Flyway" datei deploy/postgres/01-rolle.sql
pruefe "Helm-Chart existiert" datei deploy/helm/aptum/Chart.yaml
pruefe "Chart hat einen Rauchtest, der die Kette prüft" enthaelt deploy/helm/aptum/templates/tests/rauchtest.yaml "helm.sh/hook"
pruefe "Liveness und Readiness getrennt" enthaelt deploy/helm/aptum/templates/scheduling.yaml "health/readiness"
pruefe "Chart wird in der CI angewendet" enthaelt .github/workflows/ci.yml "helm test"
pruefe "kind-Cluster ist konfiguriert" datei deploy/kind/cluster.yaml
pruefe "Prüfung ohne Buchung im Backend" enthaelt services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/rest/TerminController.java "/pruefung"
pruefe "Wochenansicht nimmt die Grenzen aus Termin" enthaelt services/scheduling/domain/src/main/java/de/aptum/scheduling/domain/kalender/Wochenansicht.java "belegtRaum"
pruefe "stumme Lücke 1 ist Fixture" datei scripts/fixtures/regel-check/domain/NachbarBorgt.java
pruefe "stumme Lücke 2 ist Fixture" datei scripts/fixtures/regel-check/domain/KlassenkommentarBorgt.java
pruefe "Wertabgleich ist Fixture" datei scripts/fixtures/regel-check/domain/FalscherWert.java

echo
echo "Schritt 6 — der KI-Einsatz wird protokolliert"
pruefe "docs/ENTWICKLUNGSLOG.md existiert" datei docs/ENTWICKLUNGSLOG.md

echo
echo "Datenschutz"
pruefe "docs/DATENSCHUTZ.md existiert" datei docs/DATENSCHUTZ.md
pruefe "keine realistischen Patientennamen in Testdaten" test ! -d testdaten

echo
echo "-----------"
printf '%d Belege vorhanden, %d fehlen.\n' "$ok" "$fehler"
echo

if [[ "$fehler" -gt 0 ]]; then
  cat <<'HINWEIS'
Ein Beleg fehlt. Das ist der Sinn dieses Jobs: Dieses Repo behauptet eine
Arbeitsweise, und die Behauptung soll kaputtgehen, wenn sie nicht mehr stimmt.

Entweder den Beleg nachliefern — oder die Behauptung streichen.
HINWEIS
  exit 1
fi

echo "Alle Belege vorhanden."
