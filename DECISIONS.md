# Entscheidungen

Übersicht über die Architekturentscheidungen dieses Projekts. Die
ausführliche Fassung liegt jeweils in `docs/adr/`.

Eine ADR entsteht im Moment der Entscheidung. Wer sie rückwirkend schreibt,
rekonstruiert Begründungen, die er nie hatte — und das merkt man ihnen an.

| Nr. | Thema | Status | Datum |
|---|---|---|---|
| [ADR-001](docs/adr/ADR-001-ports-and-adapters.md) | Ports and Adapters, Domäne ohne Framework | angenommen | 2026-09-09 |
| [ADR-002](docs/adr/ADR-002-multi-tenancy.md) | Mandantentrennung über eine Spalte, abgesichert durch Row Level Security | angenommen | 2026-09-10 |
| [ADR-003](docs/adr/ADR-003-kalender-grid-selbst-gebaut.md) | Kalender-Grid selbst gebaut auf dem CDK, nicht zugekauft — Kendo Scheduler verworfen | angenommen | 2026-09-10 |
| ADR-004 | AI schlägt vor, Domäne entscheidet | offen (Stufe 3) | — |
| ADR-005 | GitOps-Pull statt Pipeline-Push | offen (Stufe 4) | — |
| [ADR-006](docs/adr/ADR-006-frontend-fundament-und-token.md) | Angular CDK mit eigenen Komponenten, Token als CSS Custom Properties | angenommen | 2026-09-09 |
| [ADR-007](docs/adr/ADR-007-fundstellen-id-im-domaenenmodell.md) | Fachliche Zahlen tragen eine Fundstellen-ID aus `regeln.md` | angenommen | 2026-09-10 |
| [ADR-008](docs/adr/ADR-008-maven-statt-gradle.md) | Maven statt Gradle, Module je Service | angenommen | 2026-09-09 |
| [ADR-009](docs/adr/ADR-009-blockieren-mit-uebersteuerung.md) | Verletzte Regeln blockieren, Übersteuerung nur mit Begründung | angenommen | 2026-09-10 |

> **Zur Nummerierung:** ADR-006 entstand als zweite Entscheidung, trägt aber
> die sechste Nummer. Die Nummern 002 bis 005 waren im Voraus für Themen
> reserviert, über die noch nicht entschieden ist. Das steht in leichtem
> Widerspruch zur Regel oben — eine reservierte Nummer nimmt vorweg, dass eine
> Entscheidung fällt und wie sie heißen wird. Die Reservierungen bleiben
> stehen, weil ein Umnummerieren bestehende Verweise bricht; neue ADRs
> bekommen fortlaufend die nächste freie Nummer.

## Anforderungen

Der Auftraggeber dieses Projekts sind zwei Ausschreibungen. Beide sind wörtlich
erfasst in [docs/ANFORDERUNGEN.md](docs/ANFORDERUNGEN.md), inklusive der Punkte,
die ein Portfolio-Projekt grundsätzlich nicht belegen kann.
