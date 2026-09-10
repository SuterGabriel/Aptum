# Offene Punkte

Punkte, die dieses Repo nicht lösen kann, aber benennt. Bei einem Profil, das
vollständig auf Nachprüfbarkeit aufbaut, ist eine offen benannte Lücke stärker
als eine geschönte Zeile in der Mapping-Tabelle.

## 1. Abgeschlossenes Studium (consultingheads, Must-have C6)

Die Ausschreibung führt ein abgeschlossenes Studium der Informatik oder einer
vergleichbaren technischen Fachrichtung als Must-have. Das ist eine formale
Anforderung, die kein Repository belegen kann.

**Entscheidung:** Es wird beworben, unabhängig davon. Bei Schweizer Abschlüssen
gehört die deutsche Einordnung dazugeschrieben, weil HF und FH in Deutschland
nicht ohne Weiteres eingeordnet werden.

## 2. Fehlende Berufserfahrung im Java-Stack

**Stand:** Java und Spring sind neu beziehungsweise eingerostet. Java ist auf
der Entwicklungsmaschine noch nicht installiert.

Der Stufenplan sieht dafür einen Puffer von fünf bis acht Tagen vor. Wichtiger
als der Zeitplan ist die Arbeitsregel: Beim Domänenkern wird bewusst weniger an
Agenten delegiert. Was im Repo steht, muss im Gespräch zeilengenau erklärbar
sein. Ein Projekt mit unverstandenem generiertem Code beschädigt genau die
Glaubwürdigkeit, die es belegen soll.

**Formulierung nach außen:** Java-Erfahrung nicht größer machen, als sie ist.

## 3. Kubernetes-Kette

ArgoCD, Helm und Terraform kommen erst in Stufe 4. Bis dahin ist der Punkt in
der Mapping-Tabelle ehrlich offen.

**Formulierung nach außen:** "Bei Kubernetes und ArgoCD komme ich aus der
CI-Ecke und baue das gerade an einem Healthcare-Projekt auf."

## 4. Erfahrung mit gewachsenen Systemen

SOLCOM fordert Erfahrung in der Weiterentwicklung komplexer Anwendungen mit sehr
viel Business-Logik, consultingheads mehrjährige Senior-Erfahrung. Beides sind
Erfahrungsanforderungen, keine Werkzeugfragen. Ein Portfolio-Projekt zeigt, *wie*
Business-Logik modelliert wird. Es ersetzt nicht die Jahre.

## 5. Fachliche Unsicherheiten in der Domäne

Sechs Punkte aus der Quellenrecherche sind widersprüchlich oder ohne
Primärquelle. Sie stehen einzeln in
[`.claude/skills/heilmittel-domain/regeln.md`](../.claude/skills/heilmittel-domain/regeln.md)
unter "Widersprüche und offene Punkte".

Der wichtigste davon: Für das verbreitete Ausfallhonorar mit 24-Stunden-Frist
gibt es keine sozialrechtliche Grundlage. Es ist eine zivilrechtliche
Vereinbarung nach § 615 BGB. Deshalb wird die Ausfallregel als Praxiseinstellung
modelliert und nicht fest verdrahtet.

## 6. Nicht angefragte Vertragsdetails

Bei einer Beauftragung aus der Schweiz nach Deutschland zu klären: Reverse-Charge
bei B2B-Leistungen ins EU-Ausland, Abrechnungswährung, Scheinselbstständigkeit
bei 80 bis 100 Prozent über sechs Monate, und ob 100 Prozent Remote aus der
Schweiz akzeptiert wird.

Das berührt das Repo nicht, gehört aber vor eine Vertragsunterschrift.

## 7. Voraussetzung für Stufe 1

**Erledigt am 2026-09-09.** Die Werkzeugkette steht:

- Eclipse Temurin JDK 21.0.12 LTS
- Apache Maven 3.9.16

Beides ohne Administratorrechte ins Benutzerprofil installiert
(`%LOCALAPPDATA%\Programs`), Prüfsumme jeweils gegen die Herstellerangabe
geprüft. Maven 4 lag nur als Release Candidate vor und schied damit aus.

Verifiziert nicht durch `--version`, sondern durch einen vollständigen Build:
`mvn test` mit einem parametrisierten JUnit-5-Test über die Fristenregel, drei
Fälle grün. Anschließend ein absichtlich falscher Erwartungswert — Build rot,
Exit-Code 1. Ein Testlauf, von dem niemand gesehen hat, wie er fehlschlägt,
belegt nichts.

**Nachgeholt am 2026-09-10:** Die Begründung steht als
[ADR-008](adr/ADR-008-maven-statt-gradle.md), mit Vermerk, dass sie einen Tag zu
spät kommt. Die Verspätung selbst bleibt dort stehen, statt geglättet zu werden.
