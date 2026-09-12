# Anforderungs-Mapping

Dieses Dokument ist der Auftraggeber dieses Projekts. Es entsteht **vor** dem Code
(Schritt 1 der Arbeitsmethodik: Abklärung und Anforderungen).

Jede Zeile stammt wörtlich aus einer der beiden Ausschreibungen. Die Spalte
*Beleg im Repo* verweist auf die Stelle, an der die Anforderung nachprüfbar
eingelöst ist. Leere Belege sind ehrlich leer.

Aktuell gehalten per `/anforderungs-mapping`.

**Legende Status:** `offen` = noch nichts im Repo · `in Arbeit` = angefangen ·
`belegt` = im Repo nachprüfbar · `teilweise belegbar` = das Repo zeigt die
Arbeitsweise, nicht die Jahre — mit Hinweis, was fehlt · `nicht belegbar` = durch
ein Portfolio-Projekt grundsätzlich nicht nachweisbar, wird offen angesprochen ·
`zu klären` = formale Anforderung außerhalb des Repos.

---

## A. SOLCOM — Full-Stack-Entwickler, Fokus Frontend, Java/Angular

Projekt-ID 3038770 · Projekt-Nr. a1WSZ0000088BoH2AU · Start 09.09.2026 ·
4 Monate · 100 % Remote · Stand der Ausschreibung: 09.09.2026

Dieselbe Ausschreibung, wortgleich, erneut veröffentlicht: Projekt 1040413,
Start 17.09.2026, 4 Monate bis Jahresende, freiberuflich, 100 % Remote —
Aufgabenbereich des Frontends dort ausdrücklich „Heilmittel, Kalender und
Abrechnung“. Die Zeilen unten gelten für beide.

### A.1 Must-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| S1 | Sehr gute Kenntnisse in dem Frontend-Framework Angular | belegt | Angular 20 in `frontend/`, Standalone, OnPush, Signals, CDK ohne Komponenten-Set (ADR-006); Terminsuche in `frontend/src/app/suche/`, Kalender-Grid in `frontend/src/app/kalender/`, Buchungsdialog auf dem CDK-Dialog in `frontend/src/app/buchung/` |
| S2 | Sehr gutes Verständnis von rxjs-Bibliothek | belegt | Kanonischer Suchflow in `frontend/src/app/suche/termin-suche.service.ts` (debounce, switchMap, inneres catchError, retry nur bei 5xx); jede Zusage einzeln getestet in `termin-suche.service.spec.ts` |
| S3 | Erfahrung mit Spring Boot, REST-APIs sowie Microservices-Architekturen | belegt | Spring Boot 3.5 mit JPA-Adapter hinter Ports (ADR-001); REST in `services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/rest/` mit drei Controllern, Ende-zu-Ende-Test über HTTP gegen Postgres; zwei Dienste in zwei Sprachen mit eigenem Bild, Deployment und Vertrag (`docs/api/openapi.json`, `docs/api/ai-assist-openapi.json`), verbunden über den Reverse Proxy (`frontend/nginx.conf`) — bewusst wenige, siehe SA7 |
| S4 | Erfahrung in Java (Java 11 oder höher) | belegt | Java 21 in drei Modulen: Domänenkern mit elf Regelklassen und Slot-Suche in `services/scheduling/domain/`, Anwendungsfälle in `services/scheduling/application/`, Spring Boot in `services/scheduling/infrastructure/` |
| S5 | Erfahrung in der Weiterentwicklung von komplexen Anwendungen mit sehr viel Business-Logik | teilweise belegbar | `services/scheduling/domain/` — benannte Regel mit Fundstelle und parametrisierten Grenzfalltests. Siehe Hinweis unten. |

> **Zu S5, offen benannt:** Das ist eine Erfahrungs-, keine Werkzeuganforderung.
> Ein Portfolio-Projekt kann zeigen, *wie* Business-Logik modelliert wird
> (benannte Domänenregeln statt `if` im Service, parametrisierte Tests für
> Grenzfälle). Es kann nicht ersetzen, mehrere Jahre an einem gewachsenen
> System gearbeitet zu haben. Das wird im Gespräch so gesagt.

### A.2 Nice-to-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| S6 | Erfahrung mit ag-grid von Vorteil | belegt | Abrechnungsübersicht in `frontend/src/app/abrechnung/` auf ag-grid 36: Thema aus den Token statt aus der Bibliothek, nur die benötigten Module registriert, Route lazy geladen (ADR-011); Statuszelle mit Wort und Farbe; axe im Unit-Test und im Browser (`frontend/e2e/abrechnung.spec.ts`), Sortieren und Tastatur ebenfalls im Browser geprüft |
| S7 | Kenntnisse in barrierefreier Software-Entwicklung (WCAG) von Vorteil | belegt | Kontrastprüfung läuft in CI (Job `kontrast`, jedes `@kontrast`-Paar aus `tokens.css`); axe-core im Unit-Test jeder Seite und des Grids (`frontend/src/app/kalender/kalender-grid.spec.ts`: Rollen, Roving Tabindex, Tastatur, Namen je Zelle), ESLint mit Template-Regeln; Playwright mit axe im echten Chromium über Suche, Grid und Erfassung (`frontend/e2e/`, CI-Job `e2e`); Roving Tabindex, ARIA-Grid und Focus Trap nach Skill `a11y-grid` (ADR-003); ein Audit durch `.claude/agents/a11y-auditor.md` hat drei Befunde gefunden, die axe nicht sieht — im Log |

### A.3 Aufgaben aus der Ausschreibung (was das Projekt abbildet)

| # | Aufgabe (wörtlich) | Status | Abbildung im Projekt |
|---|---|---|---|
| SA1 | Weiterentwicklung der Praxissoftware für Physio- und Ergotherapien | belegt | Die Anwendung selbst: Terminsuche (`frontend/src/app/suche/`), Kalender-Grid (`frontend/src/app/kalender/`), Buchungsdialog mit Regelprüfung (`frontend/src/app/buchung/`), Verordnungserfassung (`frontend/src/app/erfassung/`), darunter elf benannte Fachregeln in `services/scheduling/domain/src/main/java/de/aptum/scheduling/domain/regel/`; was zuerst kommt und was bewusst nicht gebaut wird: `docs/PRODUKT.md`. Offen: Verordnungsübersicht und Abrechnung |
| SA2 | Harmonisierung mehrerer Systeminstanzen | teilweise belegbar | Bestandsaufgabe, setzt gewachsene Systeminstanzen voraus. Was ein Portfolio leisten kann, steht in `docs/HARMONISIERUNG.md`: das Vorgehen — Regel oder Einstellung je Eigenheit, jede Instanz ein Mandant (ADR-002), Stammdaten auf den Katalog, Kennungen neu, fachliche Abnahme mit denselben Regeln (ADR-011) — und eine Tabelle, was im Repo dafür schon geschnitten ist und was nur mit echten Instanzen entsteht. Siehe Abschnitt D |
| SA3 | Ausbau der Multi-Mandanten-Architektur | belegt | `docs/adr/ADR-002-multi-tenancy.md`; Row Level Security in `services/scheduling/infrastructure/src/main/resources/db/migration/V1__mandantentrennung.sql`; Isolationstest `services/scheduling/infrastructure/src/test/java/de/aptum/scheduling/infrastructure/mandant/MandantIsolationTest.java` gegen echtes Postgres |
| SA4 | Weiterentwicklung von Kalender- und Verfügbarkeitsfunktionen | belegt | Slot-Suche über vier Dimensionen in `services/scheduling/domain/src/main/java/de/aptum/scheduling/domain/suche/`; Wochenansicht in `services/scheduling/domain/src/main/java/de/aptum/scheduling/domain/kalender/Wochenansicht.java`, Endpunkt `services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/rest/KalenderController.java`; Grid mit sechs Zuständen und Tastaturbedienung in `frontend/src/app/kalender/`; Buchung durch dasselbe Regelwerk mit Übersteuerung (ADR-009) in `frontend/src/app/buchung/`. Offen: Serientermine, Verordnungskontext auf der Suchseite |
| SA5 | Berücksichtigung fachlicher und regulatorischer Anforderungen der Branche | belegt | `.claude/skills/heilmittel-domain/regeln.md` — 65 Regeln, jede mit Wert, Fundstelle (Heilmittel-Richtlinie, Verträge nach § 125 SGB V) und Beleg-Status; `scripts/regel-check.mjs` lässt keine Fachzahl ohne belegte Fundstelle in den Domänencode (ADR-007) |
| SA6 | Entwicklung von Java-Backends und REST-APIs | belegt | REST in `services/scheduling/infrastructure/src/main/java/de/aptum/scheduling/infrastructure/rest/`, OpenAPI erzeugt und gegen die laufende Anwendung geprüft: `docs/api/openapi.json` |
| SA7 | Umsetzung und Optimierung von Microservices | belegt | Zwei Dienste in zwei Sprachen, jeder mit eigenem Bild, eigenem Deployment und eigenen Probes, verbunden nur über den OpenAPI-Vertrag (`docs/api/`) und im Cluster über Service-Namen (`deploy/helm/aptum/`); bewusst wenige — die Abrechnung ist ein Lesemodell im Scheduling-Dienst, kein dritter Dienst; die Bedingung, unter der `services/billing/` Code bekommt, steht in ADR-011 |
| SA8 | Analyse und Umsetzung fachlicher Anforderungen aus dem Praxisumfeld | belegt | `docs/PRODUKT.md` |
| SA9 | Erstellung technischer Konzepte und Architekturentscheidungen | belegt | `docs/adr/`, `DECISIONS.md` |
| SA10 | Code Reviews und Sicherstellung von Entwicklungsstandards | belegt | SonarCloud mit Quality Gate als Merge-Bedingung (Job `sonar`, `sonar-project.properties`), Abdeckung 85,5 Prozent aus JaCoCo, Karma und pytest-cov; dazu `.claude/agents/arch-reviewer.md`, `.claude/agents/a11y-auditor.md`, Guardrails in `.github/workflows/ci.yml` |

---

## B. consultingheads — Senior AI Software Developer Full Stack (m/w/d)

Projekt-ID 3044945 · München, 100 % Remote · Start 10/2026 · 6 Monate,
Verlängerung möglich · 80–100 % · AI-native Healthcare SaaS

### B.1 Must-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| C1 | Mehrjährige Senior-Erfahrung als Full-Stack Engineer mit modernen Webanwendungen | teilweise belegbar | Portfolio + dieses Projekt |
| C2 | Tiefe in mindestens einem Frontend-Stack (React oder Angular) | belegt | Angular 20 in `frontend/`: Terminsuche mit kanonischem Suchflow, selbst gebautes Kalender-Grid mit Roving Tabindex (ADR-003) |
| C3 | Tiefe in einem Backend-Stack (Java/Spring Boot oder Python) | belegt | Beides: Java 21 und Spring Boot 3.5 in `services/scheduling/`, Python 3.12 mit FastAPI, Pydantic und mypy strict in `services/ai-assist/` |
| C4 | Solide Kenntnisse der relevanten Cloud- und DevOps-Praktiken (u. a. Kubernetes, Helm, ArgoCD, Terraform) | teilweise belegbar | Drei Dienste als mehrstufige Container ohne Root (`services/scheduling/Dockerfile`, `services/ai-assist/Dockerfile`, `frontend/Dockerfile`), `compose.yml` mit Healthchecks und Startbedingungen; Helm-Chart in `deploy/helm/aptum/` mit getrennten Liveness- und Readiness-Probes; Terraform in zwei Ständen (`deploy/terraform/plattform/`, `deploy/terraform/anwendung/`) stellt ArgoCD und meldet die Anwendung an; ArgoCD rollt das Chart aus dem geprüften Commit aus; die CI (Job `cluster`) fährt das bei jedem Push in einem `kind`-Cluster mit Bildern aus der GitHub Container Registry (ADR-010). Was ein Portfolio-Projekt nicht ersetzt: einen Cluster betrieben zu haben — siehe Hinweis zu S5 |

> **Zu C4, offen benannt:** Alle vier Werkzeuge sind angewendet, nicht abgelegt — die CI fährt bei jedem Push Terraform in zwei Ständen, ArgoCD rollt das Helm-Chart in einen `kind`-Cluster aus, ein Pod im Cluster prüft die Kette (ADR-010, `docs/BETRIEB.md`). Was ein Portfolio-Projekt nicht zeigen kann, ist ein Cluster, der Monate läuft: Upgrades unter Last, Backups, die man zurückgespielt hat, ein Vorfall um drei Uhr nachts. Das ist eine Erfahrungsanforderung wie S5 und wird im Gespräch so gesagt.
| C5 | Nachweisbarer, effektiver Einsatz von KI in der Softwareentwicklung | belegt | `docs/ENTWICKLUNGSLOG.md` — je Schritt, was delegiert wurde, was die Gates abgefangen haben und was nicht funktionierte; `.claude/` versioniert; `docs/PIPELINE.md` |
| C6 | Abgeschlossenes Studium der Informatik, Software Engineering oder vergleichbarer technischer Fachrichtung | zu klären | nicht durch das Repo belegbar — siehe `docs/OFFENE-PUNKTE.md` |
| C7 | Ausgeprägte Problemlösungskompetenz, Innovationsfreude und Proaktivität mit Bezug zum Gesundheitswesen | teilweise belegbar | `.claude/skills/heilmittel-domain/regeln.md` — Fristen, Unterbrechung, Frequenz, Mengen und Qualifikation je mit Fundstelle und Beleg-Status (`BELEGT`, `BELEGT als Nichtfund`, `UNSICHER`) — darunter sechs Widersprüche zwischen den Quellen, die beim Recherchieren auffielen und im Log stehen; ADR-009 (Blockieren mit Übersteuerung statt Warnen) als fachliche Entscheidung; `scripts/beleg-check.sh` hält das Mapping ehrlich. Eine Eigenschaft lässt sich nicht belegen, nur das Verhalten in einem Projekt — siehe Abschnitt D |

### B.2 Wünschenswert

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| C8 | Praxiserfahrung mit KI-Coding-Agenten sowie Aufbau von Agenten, Skills und Workflows | belegt | `.claude/skills/`, `.claude/commands/`, `.claude/agents/`, `.claude/hooks/prosa-nach-schreiben.mjs` als PostToolUse-Hook in `.claude/settings.json`; die Hooks haben im Log dokumentiert mehrfach den Agenten selbst korrigiert |
| C9 | Erfahrung mit AI-Pipelines, Harnesses und Context Engineering | belegt | Pipeline in vier benannten Schritten in `services/ai-assist/src/ai_assist/erfassung.py` (Pseudonymisierung vor dem Aufruf); Eval-Suite mit 55 begründeten Fällen in `evals/cases/`, feldweise Messung und Regressionsvergleich in `evals/run.py`, drei echte Läufe gegen Claude mit einer Prompt-Iteration von 89 auf 100 Prozent beim Heilmittel (Basislinie `evals/ergebnisse/anthropic.json`, Verlauf im Log), CI-Job `evals`; Prompt als Datei `services/ai-assist/src/ai_assist/prompts/erfassung.md`; Context Engineering für die Agenten in `CLAUDE.md`, `.claude/skills/`, `.claude/hooks/`; Beschreibung in `docs/AI-PIPELINE.md` |
| C10 | Kenntnisse in LLM-Tooling (Azure OpenAI, Anthropic SDK, MCP) | belegt | Provider-Interface mit Anthropic SDK (Tool-Use als Structured Output) und OpenAI-kompatiblem Provider für Azure in `services/ai-assist/src/ai_assist/provider/`; MCP-Server mit vier Werkzeugen in `services/ai-assist/src/ai_assist/mcp_server.py` |
| C11 | Verständnis von Product-Management-Prozessen | belegt | `docs/PRODUKT.md` |

### B.3 Aufgaben aus der Ausschreibung

| # | Aufgabe (wörtlich) | Status | Abbildung im Projekt |
|---|---|---|---|
| CA1 | Design, Entwicklung und Betrieb moderner Webanwendungen über Frontend, Backend und DevOps | belegt | Frontend, zwei Backends und Datenbank als ein Stapel (`compose.yml`, `frontend/nginx.conf`) und als GitOps-Deployment, das die CI bei jedem Push fährt (`deploy/`, ADR-010); was ein Betrieb erfährt und was nicht gebaut ist, steht in `docs/BETRIEB.md` |
| CA2 | Integration KI-nativer Funktionen und agentischer Erfahrungen, LLMs, Retrieval, Tool-Use, strukturierte Workflows | belegt | Verordnungserfassung aus Freitext in `services/ai-assist/src/ai_assist/erfassung.py`: Pseudonymisierung, Tool-Use, Schema, nicht extrahierbar statt geraten; Eval-Suite in `evals/`; MCP-Server als Tool-Use-Schnittstelle in `services/ai-assist/src/ai_assist/mcp_server.py`; die Seite `frontend/src/app/erfassung/` zeigt Vorschlag, Lücken und Bestätigung durch einen Menschen. Retrieval bewusst nicht gebaut, mit Grund in `docs/AI-PIPELINE.md`: Die Domäne kennt die Fristen, das Modell braucht sie nicht |
| CA3 | Konzeption und Betrieb cloud-nativer Services, Zuverlässigkeit, Sicherheit, Entwicklerproduktivität | teilweise belegbar | Getrennte Liveness-, Readiness- und Startprobes; Container ohne Root; strukturiertes JSON-Protokoll mit Mandanten-ID als Feld und ohne Personenbezug in beiden Diensten (`services/ai-assist/src/ai_assist/protokoll.py`, `MandantKontextHalter`), mit Test; Geheimnisse außerhalb des Repos; ein Befehl zum Starten. Nicht gebaut und in `docs/BETRIEB.md` benannt: Ingress mit TLS, Metriken mit Sammler, Backups, Netzwerkrichtlinien, ein Cluster, der bleibt |
| CA4 | Mitgestaltung gemeinsam genutzter Agenten, Skills, Pipelines und Harnesses im Team | belegt | `.claude/skills/`, `.claude/agents/`, `.claude/commands/`, `.claude/hooks/` — versioniert im Repo, nicht global |
| CA5 | Zusammenarbeit mit Product Management, UX, Architekten | teilweise belegbar | Die Artefakte, über die diese Zusammenarbeit läuft, sind da: Produktsicht mit Personas und bewussten Nicht-Zielen (`docs/PRODUKT.md`), acht ADRs mit Alternativen und „wann wir anders entscheiden würden“ (`docs/adr/`), ein ausformulierter Gestaltungsauftrag mit Wireframe- und Mockup-Brief (`docs/mockups/`), ein Audit-Agent (`.claude/agents/a11y-auditor.md`). Die Zusammenarbeit selbst zeigt ein Einzelprojekt nicht — siehe Abschnitt D |
| CA6 | Verständnis von Healthcare-Workflows | belegt | `.claude/skills/heilmittel-domain/regeln.md` — Regeltabelle mit Fundstelle und Beleg-Status je Regel |

---

## C. Überschneidung — was doppelt zählt

Diese Punkte bedienen beide Ausschreibungen und haben deshalb Vorrang:

| Thema | SOLCOM | consultingheads |
|---|---|---|
| Angular-Frontend | S1, S2 | C2 |
| Java / Spring Boot | S3, S4 | C3 |
| Multi-Mandanten-Architektur | SA3 | implizit (SaaS) |
| Healthcare-Domäne | SA5 | C7, CA6 |
| Kalender und Verfügbarkeit | SA4 | — |
| Barrierefreiheit | S7 | implizit (Healthcare SaaS) |
| Entwicklungsstandards und Reviews | SA10 | CA3 |
| Betrieb als Container und im Cluster | S3, SA7 | C4, CA1, CA3 |

---

## D. Was dieses Projekt bewusst *nicht* belegt

Ehrlichkeit ist hier Teil des Arguments. Ein Profil, das vollständig auf
Nachprüfbarkeit aufbaut, verliert mehr durch eine unhaltbare Behauptung als
durch eine offen benannte Lücke.

- **S5 / C1 — Jahre an einem gewachsenen System.** Nicht simulierbar.
- **SA2 — Harmonisierung bestehender Systeminstanzen.** Setzt Bestandssysteme voraus. Was ein Portfolio dazu leisten kann, ist ein Konzept: `docs/HARMONISIERUNG.md`, mit einer Tabelle, was fehlt. Das Vorgehen ist da, die Jahre nicht.
- **C4 / CA3 — Ein Cluster, der Monate läuft.** Alle Werkzeuge sind angewendet, in der CI, bei jedem Push (ADR-010). Upgrades unter Last, Backups, die man zurückgespielt hat, ein Vorfall nachts: nicht. `docs/BETRIEB.md` zieht die Grenze.
- **C7 / CA5 — Eigenschaften und Zusammenarbeit.** Ein Repo zeigt Verhalten und Artefakte, keine Charakterzüge und keine Teamarbeit. Was da ist, ist da; der Rest ist Gespräch.
- **C6 — Abgeschlossenes Studium.** Formale Anforderung, unabhängig vom Repo.
- **Betrieb unter echter Last.** Das Projekt läuft, es trägt keinen Produktionsverkehr.
