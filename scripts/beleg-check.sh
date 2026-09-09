#!/usr/bin/env bash
# Beleg-Check
#
# Dieses Repo behauptet an mehreren Stellen etwas über die eigene Arbeitsweise.
# Dieses Skript macht aus jeder Behauptung eine Zusage, die kaputtgehen kann.
#
# Es prüft absichtlich nur Existenz und einfache Struktur — keine Qualität.
# Ein Beleg, der fehlt, fällt hier auf. Ein Beleg, der schlecht ist, fällt im
# Review auf. Beides ist besser als eine Behauptung, die niemand prueft.

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

datei() { [ -f "$1" ]; }
ordner_nicht_leer() { [ -d "$1" ] && [ -n "$(ls -A "$1" 2>/dev/null)" ]; }
enthaelt() { grep -q "$2" "$1" 2>/dev/null; }
hoechstens_zeilen() { [ "$(wc -l < "$1")" -le "$2" ]; }

echo
echo "Beleg-Check"
echo "==========="
echo

echo "Schritt 1 — Anforderungen liegen vor dem Code"
pruefe "docs/ANFORDERUNGEN.md existiert" datei docs/ANFORDERUNGEN.md
pruefe "beide Ausschreibungen sind erfasst (SOLCOM)" enthaelt docs/ANFORDERUNGEN.md "SOLCOM"
pruefe "beide Ausschreibungen sind erfasst (consultingheads)" enthaelt docs/ANFORDERUNGEN.md "consultingheads"
pruefe "die nicht belegbaren Punkte sind benannt" enthaelt docs/ANFORDERUNGEN.md "nicht \*belegt\|nicht belegbar\|nicht \*\*nicht\*\*"

echo
echo "Schritt 3 — Entscheidungen sind dokumentiert"
pruefe "DECISIONS.md existiert" datei DECISIONS.md
pruefe "mindestens eine ADR existiert" ordner_nicht_leer docs/adr
for adr in docs/adr/ADR-*.md; do
  [ -e "$adr" ] || continue
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

if [ "$fehler" -gt 0 ]; then
  cat <<'HINWEIS'
Ein Beleg fehlt. Das ist der Sinn dieses Jobs: Dieses Repo behauptet eine
Arbeitsweise, und die Behauptung soll kaputtgehen, wenn sie nicht mehr stimmt.

Entweder den Beleg nachliefern — oder die Behauptung streichen.
HINWEIS
  exit 1
fi

echo "Alle Belege vorhanden."
