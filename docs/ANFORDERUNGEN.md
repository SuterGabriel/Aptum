# Anforderungs-Mapping

Dieses Dokument ist der Auftraggeber dieses Projekts. Es entsteht **vor** dem Code
(Schritt 1 der Arbeitsmethodik: Abklärung und Anforderungen).

Jede Zeile stammt wörtlich aus einer der beiden Ausschreibungen. Die Spalte
*Beleg im Repo* verweist auf die Stelle, an der die Anforderung nachprüfbar
eingelöst ist. Leere Belege sind ehrlich leer.

Aktuell gehalten per `/anforderungs-mapping`.

**Legende Status:** `offen` = noch nichts im Repo · `in Arbeit` = angefangen ·
`belegt` = im Repo nachprüfbar · `nicht belegbar` = durch ein Portfolio-Projekt
grundsätzlich nicht nachweisbar, wird offen angesprochen.

---

## A. SOLCOM — Full-Stack-Entwickler, Fokus Frontend, Java/Angular

Projekt-ID 3038770 · Projekt-Nr. a1WSZ0000088BoH2AU · Start 09.09.2026 ·
4 Monate · 100 % Remote · Stand der Ausschreibung: 09.09.2026

### A.1 Must-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| S1 | Sehr gute Kenntnisse in dem Frontend-Framework Angular | offen | `frontend/` |
| S2 | Sehr gutes Verständnis von rxjs-Bibliothek | offen | `frontend/` — kanonischer Suchflow, kommentiert |
| S3 | Erfahrung mit Spring Boot, REST-APIs sowie Microservices-Architekturen | offen | `services/` |
| S4 | Erfahrung in Java (Java 11 oder höher) | offen | Java 21, `services/` |
| S5 | Erfahrung in der Weiterentwicklung von komplexen Anwendungen mit sehr viel Business-Logik | teilweise belegbar | Domänenregeln + parametrisierte Tests. Siehe Hinweis unten. |

> **Zu S5, offen benannt:** Das ist eine Erfahrungs-, keine Werkzeuganforderung.
> Ein Portfolio-Projekt kann zeigen, *wie* Business-Logik modelliert wird
> (benannte Domänenregeln statt `if` im Service, parametrisierte Tests für
> Grenzfälle). Es kann nicht ersetzen, mehrere Jahre an einem gewachsenen
> System gearbeitet zu haben. Das wird im Gespräch so gesagt.

### A.2 Nice-to-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| S6 | Erfahrung mit ag-grid von Vorteil | offen | Abrechnungsübersicht in `frontend/` |
| S7 | Kenntnisse in barrierefreier Software-Entwicklung (WCAG) von Vorteil | offen | axe-core in CI, Kontrastberechnung aus Farbtoken |

### A.3 Aufgaben aus der Ausschreibung (was das Projekt abbildet)

| # | Aufgabe (wörtlich) | Abbildung im Projekt |
|---|---|---|
| SA1 | Weiterentwicklung der Praxissoftware für Physio- und Ergotherapien | Domäne des gesamten Projekts |
| SA2 | Harmonisierung mehrerer Systeminstanzen | nicht abgebildet — Bestandsaufgabe, offen benannt |
| SA3 | Ausbau der Multi-Mandanten-Architektur | ADR-002, Isolationstest in CI |
| SA4 | Weiterentwicklung von Kalender- und Verfügbarkeitsfunktionen | Slot-Berechnung + Kalender-Grid |
| SA5 | Berücksichtigung fachlicher und regulatorischer Anforderungen der Branche | Heilmittel-Regeln als Domänenmodell |
| SA6 | Entwicklung von Java-Backends und REST-APIs | `services/`, OpenAPI |
| SA7 | Umsetzung und Optimierung von Microservices | drei Services, bewusst wenige |
| SA8 | Analyse und Umsetzung fachlicher Anforderungen aus dem Praxisumfeld | `docs/PRODUKT.md` |
| SA9 | Erstellung technischer Konzepte und Architekturentscheidungen | `docs/adr/` |
| SA10 | Code Reviews und Sicherstellung von Entwicklungsstandards | `arch-reviewer`-Subagent, CI-Guardrails |

---

## B. consultingheads — Senior AI Software Developer Full Stack (m/w/d)

Projekt-ID 3044945 · München, 100 % Remote · Start 10/2026 · 6 Monate,
Verlängerung möglich · 80–100 % · AI-native Healthcare SaaS

### B.1 Must-have

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| C1 | Mehrjährige Senior-Erfahrung als Full-Stack Engineer mit modernen Webanwendungen | teilweise belegbar | Portfolio + dieses Projekt |
| C2 | Tiefe in mindestens einem Frontend-Stack (React oder Angular) | offen | `frontend/` (Angular) |
| C3 | Tiefe in einem Backend-Stack (Java/Spring Boot oder Python) | offen | `services/` (Java 21 / Spring Boot 3) |
| C4 | Solide Kenntnisse der relevanten Cloud- und DevOps-Praktiken (u. a. Kubernetes, Helm, ArgoCD, Terraform) | offen | `infra/` |
| C5 | Nachweisbarer, effektiver Einsatz von KI in der Softwareentwicklung | in Arbeit | `.claude/` im Repo, `docs/ENTWICKLUNGSLOG.md`, CI-Guardrails |
| C6 | Abgeschlossenes Studium der Informatik, Software Engineering oder vergleichbarer technischer Fachrichtung | zu klären | nicht durch das Repo belegbar — siehe `docs/OFFENE-PUNKTE.md` |
| C7 | Ausgeprägte Problemlösungskompetenz, Innovationsfreude und Proaktivität mit Bezug zum Gesundheitswesen | offen | Domänenmodell, ADRs |

### B.2 Wünschenswert

| # | Anforderung (wörtlich) | Status | Beleg im Repo |
|---|---|---|---|
| C8 | Praxiserfahrung mit KI-Coding-Agenten sowie Aufbau von Agenten, Skills und Workflows | in Arbeit | `.claude/skills/`, `.claude/commands/`, `.claude/agents/`, `.claude/hooks/` |
| C9 | Erfahrung mit AI-Pipelines, Harnesses und Context Engineering | offen | `docs/AI-PIPELINE.md` |
| C10 | Kenntnisse in LLM-Tooling (Azure OpenAI, Anthropic SDK, MCP) | offen | Provider-Interface mit zwei Implementierungen, eigener MCP-Server |
| C11 | Verständnis von Product-Management-Prozessen | offen | `docs/PRODUKT.md` |

### B.3 Aufgaben aus der Ausschreibung

| # | Aufgabe (wörtlich) | Abbildung im Projekt |
|---|---|---|
| CA1 | Design, Entwicklung und Betrieb moderner Webanwendungen über Frontend, Backend und DevOps | Gesamtprojekt |
| CA2 | Integration KI-nativer Funktionen und agentischer Erfahrungen, LLMs, Retrieval, Tool-Use, strukturierte Workflows | `services/ai-assist/` |
| CA3 | Konzeption und Betrieb cloud-nativer Services, Zuverlässigkeit, Sicherheit, Entwicklerproduktivität | `infra/`, Observability mit Tenant-ID |
| CA4 | Mitgestaltung gemeinsam genutzter Agenten, Skills, Pipelines und Harnesses im Team | `.claude/` versioniert im Repo, nicht global |
| CA5 | Zusammenarbeit mit Product Management, UX, Architekten | `docs/PRODUKT.md`, `docs/adr/`, Mockups |
| CA6 | Verständnis von Healthcare-Workflows | Heilmittel-Domäne |

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

---

## D. Was dieses Projekt bewusst *nicht* belegt

Ehrlichkeit ist hier Teil des Arguments. Ein Profil, das vollständig auf
Nachprüfbarkeit aufbaut, verliert mehr durch eine unhaltbare Behauptung als
durch eine offen benannte Lücke.

- **S5 / C1 — Jahre an einem gewachsenen System.** Nicht simulierbar.
- **SA2 — Harmonisierung bestehender Systeminstanzen.** Setzt Bestandssysteme voraus.
- **C6 — Abgeschlossenes Studium.** Formale Anforderung, unabhängig vom Repo.
- **Betrieb unter echter Last.** Das Projekt läuft, es trägt keinen Produktionsverkehr.
