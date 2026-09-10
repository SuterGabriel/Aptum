# Die Pipeline

Drei Stufen laufen zwischen einer Änderung und `main`. Sie sind bewusst
unabhängig voneinander: Die eine ist schnell und umgehbar, die andere langsamer
und nicht umgehbar. Eine vierte Stufe — der Betrieb — existiert noch nicht und
kommt mit Stufe 4 des Stufenplans.

| Stufe | Auslöser | Umfang | Umgehbar |
|---|---|---|---|
| `.githooks/pre-commit` | `git commit` | vorgemerkte Dateien, teils der ganze Baum | ja, `--no-verify` |
| `.claude/hooks/` | jede Dateiänderung durch einen Agenten | die geschriebene Datei | nein, aber abschaltbar |
| `.github/workflows/ci.yml` | Push auf `main`, Pull Request | der ganze Baum | nein |

Die Skript-Gates sind abhängigkeitsfreie Skripte in `bash` oder Node; die
Java-Gates laufen über Maven, die Frontend-Gates über `npm run` in
`frontend/`. An der Wurzel gibt es keinen Paketmanager; der Aufruf der
Skripte ist der direkte.

## Einrichten

```bash
bash scripts/hooks-installieren.sh
```

Setzt `core.hooksPath` auf `.githooks/`. Das ist nach jedem frischen Klon
einmal nötig — ohne `npm install` gibt es keinen Schritt, der es nebenbei
erledigt. Ohne diesen Aufruf sucht Git seine Hooks weiter in `.git/hooks/`,
und das Verzeichnis liegt außerhalb der Versionierung.

**Für die Backend-Tests läuft Docker.** Die Infrastruktur-Tests starten ein
echtes Postgres über Testcontainers; ohne laufenden Daemon fallen sie nach
einer halben Sekunde mit „Could not find a valid Docker environment". Auf
Windows kommt eine Falle dazu: Docker 29 verlangt mindestens API 1.40, und
docker-java handelt ohne Vorgabe eine ältere aus — der Daemon antwortet dann
mit 400, und die Meldung sieht aus, als gäbe es kein Docker. Das Pom des
Infrastruktur-Moduls setzt deshalb `api.version` in der Test-JVM.

## Stufe 1: der Git-Hook

`.githooks/pre-commit` führt vier Gates immer aus und drei nur, wenn passende
Dateien vorgemerkt sind. Der Zuschnitt ist ungleich, und zwar aus einem Grund
je Zeile:

| Gate | Umfang | Warum dieser Umfang |
|---|---|---|
| `scripts/prosa-check.mjs` | vorgemerkte Dateien | Schnell, und er meldet nichts über Dateien, die man nicht angefasst hat |
| `scripts/link-check.mjs` | ganzer Baum | Ein Verweis bricht durch eine Umbenennung; die kaputte Stelle steht dann in einer Datei, die man nicht bearbeitet hat |
| `scripts/beleg-check.sh` | ganzer Baum | Arbeitet von Natur aus so |
| `scripts/regel-check.mjs` | ganzer Baum | Der Katalog und der Code, der ihn zitiert, liegen selten im selben Commit |
| `scripts/kontrast-check.mjs` | nur bei geänderter Token-Datei | Prüft ausschließlich `tokens.css` |
| `mvn spotless:check` | nur bei vorgemerktem Java | Der JVM-Start kostet Sekunden; ein Hook, der bei jedem Markdown-Commit wartet, wird umgangen |

Die Skript-Gates zusammen unter zwei Sekunden; der Format-Check über Maven
kostet den JVM-Start und läuft deshalb nur bei Java-Änderungen.

**Der Hook ist mit `--no-verify` umgehbar, und das bleibt so.** Ein Hook, der
eine halbe Minute braucht, wird umgangen; ein Hook, der routinemäßig umgangen
wird, erzwingt nichts. Deshalb laufen dieselben Gates in der CI noch einmal,
und dort ist es keine Option. Aus demselben Grund werden Tests hier nie laufen:
Sie gehören in die CI, wo Warten nichts kostet.

## Stufe 2: der Agenten-Hook

`.claude/settings.json` verdrahtet `.claude/hooks/prosa-nach-schreiben.mjs` als
`PostToolUse` auf `Write` und `Edit`. Nach jeder Dateiänderung durch einen
Agenten läuft der Prosa-Check auf genau diese Datei; bei einem Befund bekommt
der Agent ihn zurück und bessert nach, bevor er weiterarbeitet.

Der Grund steht im [Entwicklungslog](ENTWICKLUNGSLOG.md): Der einzige
handwerkliche Fehler, den dieses Projekt bisher protokolliert hat, war ein
Agenten-Fehler, und er wurde beim Nachlesen gefunden, nicht durch eine Prüfung.
Zwischen dem Schreiben einer Datei und dem Commit liegen oft zwanzig weitere
Änderungen — bis der Git-Hook anschlägt, ist die Ursache aus dem Blick.

Dass diese Verdrahtung im Repo liegt und nicht in einer globalen Konfiguration,
ist dieselbe Zusage wie bei den Skills: Alles Agentenbezogene ist Teil des
Ergebnisses, nicht nur Werkzeug.

## Stufe 3: GitHub Actions

`.github/workflows/ci.yml`, sechs Jobs, jeder ein Gate:

| Job | Prüft |
|---|---|
| `belege` | Jede Behauptung über die eigene Arbeitsweise hat einen Beleg im Repo |
| `dokumente` | Ausgeschriebene Umlaute in Prosa · tote Verweise zwischen Markdown-Dateien |
| `regeln` | Der Regelkatalog ist vollständig ausgezeichnet, und kein Domänencode nennt eine Zahl ohne belegte Fundstelle |
| `kontrast` | Jedes geforderte Farbpaar aus `tokens.css` hält seine WCAG-Schwelle |
| `backend` | Formatierung (Spotless), Tests aller drei Module, ArchUnit gegen ADR-001, und ein echtes Postgres über Testcontainers für den Isolationstest aus ADR-002 — der einzige Job mit JVM und Docker |
| `frontend` | Prettier, ESLint mit den Template-Regeln zur Barrierefreiheit, API-Typen gegen `docs/api/openapi.json`, Unit-Tests, Build mit Budget |

Was noch fehlt — Lint und Bundle-Budget fürs Frontend, Playwright mit axe-core
über das Kalender-Grid, Dependency-Scan, die Eval-Suite — steht als
auskommentiertes Gerüst in der Datei, nicht als leere Jobs: Ein Job, der
nichts prüft und trotzdem grün meldet, ist schlimmer als kein Job.

## Die fünf Gates im Einzelnen

Alle fünf existieren aus demselben Grund: Dieses Repo behauptet an mehreren
Stellen etwas über die eigene Arbeitsweise, und eine Behauptung, die niemand
prüft, ist eine Behauptung.

**`scripts/beleg-check.sh`** prüft die Belegspalte in
[ANFORDERUNGEN.md](ANFORDERUNGEN.md), die Pflichtabschnitte jeder ADR, dass die
Skills im Repo liegen und dass der KI-Einsatz protokolliert wird. Die Feinheit
steckt in der Belegspalte: Zeilen mit Status `offen`, `zu klären`, `teilweise
belegbar` oder `nicht belegbar` dürfen auf etwas zeigen, das es noch nicht gibt
— dort ist der Eintrag ein Ziel, kein Beleg. Alles andere muss existieren.

**`scripts/kontrast-check.mjs`** liest die `@kontrast`-Kommentare aus
`frontend/src/styles/tokens.css` und rechnet die Verhältnisse nach. Die
Anforderung steht neben der Farbe, nicht in einer zweiten Konfiguration: Wer
eine Farbe ändert, sieht sie in derselben Zeile.

**`scripts/prosa-check.mjs`** verbietet ausgeschriebene Umlaute in Prosa. Die
verbotenen Formen leitet er aus dem Repo selbst ab — jedes Wort, das irgendwo
mit Umlaut steht, darf nirgends in ASCII-Umschrift auftauchen. In Markdown wird
alles außer Code geprüft, in allen anderen Dateien nur reine Kommentarzeilen.
Diese Trennung ist der Punkt: `pruefe()` in `beleg-check.sh`,
`--hitflaeche-min` in `tokens.css` und `Pruefergebnis` aus dem Glossar sind
Bezeichner und Absicht, kein Fehler.

**`scripts/regel-check.mjs`** ist das Gate, das dieses Projekt eigentlich
braucht. Der wichtigste Befund aus Stufe 0 lautet: Falscher Code fällt im Test
auf, falsche Fachlogik nicht — ein Test über eine erfundene Frist ist grün.
Deshalb trägt jede der 65 Regeln in `regeln.md` eine ID, und jede fachliche
Konstante im Domänenkern nennt sie. Geprüft wird beides: der Katalog
(eindeutige IDs, gültiger Status) und der Code (keine nackte Zahl, jede
Fundstelle existiert, trägt `BELEGT` statt `UNSICHER` — und die Zahl kommt in
der genannten Zeile auch vor). Grundlage ist
[ADR-007](adr/ADR-007-fundstellen-id-im-domaenenmodell.md).

**Dieses Gate hat eine eigene Testsuite**, `scripts/regel-check.test.mjs`, mit
einem Fixture je Fall unter `scripts/fixtures/regel-check/`. Der Grund steht
im [Entwicklungslog](ENTWICKLUNGSLOG.md): Zwei Lücken in diesem Skript haben
nichts gemeldet — eine Konstante borgte sich die Fundstelle ihres Nachbarn,
eine andere die aus dem Klassenkommentar — und beide fielen nur durch
Gegenproben von Hand auf. Ein Gate, das nichts meldet, sieht aus wie ein
Gate, das zufrieden ist. Die beiden Lücken sind jetzt Fixtures, die bei jedem
Lauf mitprüfen, dass sie geschlossen bleiben.

**`scripts/link-check.mjs`** prüft Markdown-Verweise der Form `[Text](Ziel)`.
Pfade in Backticks prüft er bewusst nicht: Die Skills und Commands nennen dort
absichtlich Pfade, die es noch nicht gibt. Ein Verweis in Klammern ist eine
Zusage für jetzt, ein Pfad in Backticks kann eine Ansage für später sein.

## Lokal ausführen

```bash
node scripts/prosa-check.mjs              # ganzer Baum
node scripts/prosa-check.mjs datei.md     # einzelne Dateien
node scripts/link-check.mjs
node scripts/regel-check.mjs
node scripts/kontrast-check.mjs
bash  scripts/beleg-check.sh
node --test scripts/regel-check.test.mjs         # das Gate selbst

cd services/scheduling
mvn spotless:check test                          # Format, Tests, ArchUnit, Testcontainers
mvn spotless:apply                               # formatieren
```

## Was die Pipeline nicht abfängt

Der ehrliche Teil. Jeder Punkt ist entweder schon passiert oder eine Änderung
davon entfernt:

- **Ob die Zahl im Code die *richtige* Zahl aus der Tabelle ist.** Der
  Wertabgleich ist grob: Die Zahl muss in der genannten Zeile vorkommen. Eine
  Zeile mit mehreren Zahlen — `10 · 20` — deckt beide, auch wenn nur eine
  gemeint war. Der Zahlendreher und der Verweis auf die falsche Zeile fallen
  auf; die Verwechslung innerhalb einer Zeile nicht. Näher kommt man ohne
  maschinenlesbare Werte nicht heran, siehe
  [ADR-007](adr/ADR-007-fundstellen-id-im-domaenenmodell.md).
- **Fachliche Nullen, Einsen und Zweien.** Der Regel-Check lässt sie durch,
  weil sie in jedem Code vorkommen. Eine fachliche Zwei — „2× wöchentlich" —
  rutscht dadurch hindurch.
- **Zahlen im Fließtext, die aus einem Skript stammen.** Die Anzahl der
  geprüften Kontrastpaare steht handgeschrieben an drei Stellen. Sie stimmt
  heute. Wer eine `@kontrast`-Zeile ergänzt, lässt drei Dokumente falsch
  aussehen, und das Skript, das die Wahrheit kennt, wird nicht gefragt.
- **Anführungszeichen.** Das Repo mischt gerade und deutsche. Eine Regel dazu
  ist nicht getroffen, also prüft auch nichts.
- **Alles ab Stufe 1.** Kein Java, kein ArchUnit, keine Tests, kein axe-Lauf,
  keine Evals. Die Jobs stehen als Gerüst bereit.
- **Der Rechtsstand der Domäne.** `regeln.md` trägt Recherchedatum und
  Rechtsstände. Fristen ändern sich; nichts warnt, wenn die Recherche alt wird.

## Gegenprobe

Jedes Gate wurde einmal absichtlich rot gemacht und danach wieder grün. Ein
Guardrail, von dem niemand gesehen hat, wie er ausschlägt, ist eine Behauptung
wie jede andere. Was dabei herauskam, steht im
[Entwicklungslog](ENTWICKLUNGSLOG.md) unter dem Eintrag vom 10.09.2026.
