# ADR-008: Maven statt Gradle

- **Status:** angenommen
- **Datum:** 2026-09-09, geschrieben am 2026-09-10
- **Stufe:** 1 (Voraussetzung)

> **Zur Verspätung:** Diese ADR ist einen Tag zu spät. Die Entscheidung fiel am
> 09.09. mit der Installation, die Begründung steht erst jetzt. Das widerspricht
> der eigenen Regel „ADRs entstehen im Moment der Entscheidung" und war in
> [OFFENE-PUNKTE.md](../OFFENE-PUNKTE.md) als Schuld geführt. Der Vermerk bleibt
> stehen, weil eine rückwirkende ADR, die sich als zeitnah ausgibt, genau das
> Vertrauen beschädigt, das dieses Repo aufbauen soll.

## Kontext

Java und Spring sind in diesem Projekt neuer beziehungsweise eingerosteter
Stack ([OFFENE-PUNKTE.md](../OFFENE-PUNKTE.md), Punkt 2). ADR-001 verlangt
getrennte Module, damit das Domain-Modul in seiner Build-Datei keine
Spring-Abhängigkeit haben *kann* — die Architekturzusage hängt also am Build,
nicht nur an Disziplin.

## Optionen

**1. Gradle mit Kotlin-DSL**

Deutlich kürzere Build-Dateien, schnellere inkrementelle Builds, und die
Konfiguration ist typgeprüft mit Autovervollständigung in der IDE. Bei
Multi-Modul-Projekten ist das ein echter Vorteil: Was in Maven vier
verschachtelte XML-Blöcke braucht, sind in Gradle drei Zeilen.

**2. Maven**

Geschwätziges XML, langsamer, kein inkrementelles Bauen. Dafür deklarativ: Eine
`pom.xml` beschreibt einen Zustand, sie führt nichts aus.

## Entscheidung

**Option 2, Maven.** Konkret Apache Maven 3.9.16 mit Eclipse Temurin JDK 21 LTS,
beide ohne Administratorrechte ins Benutzerprofil installiert und mit einem
vollständigen `mvn test` verifiziert, nicht nur mit `--version`.

Maven 4 lag nur als Release Candidate vor und schied damit aus.

Modulschnitt: ein Parent-Pom je Service, darunter `domain`, `application`,
`infrastructure`. Das Domain-Modul deklariert keine Spring-Abhängigkeit, damit
der Verstoß aus ADR-001 gar nicht kompilieren kann.

**Begründung — als Annahme markiert, weil sie nachträglich formuliert ist:**
Der Grund ist derselbe wie in ADR-001 für den hexagonalen Schnitt. Dort steht,
Framework-Magie beim Erlernen der Fachlogik gleichzeitig mitzudenken sei der
sicherste Weg, beides halb zu verstehen. Eine Build-DSL, die eine
Programmiersprache ist, fällt in dieselbe Kategorie: Ein Gradle-Build, der sich
unerwartet verhält, ist beim Erlernen von Java eine Fehlerquelle mehr. Mavens
XML ist unangenehm zu lesen, aber es überrascht nicht.

Dazu die Verbreitung: Beide Ausschreibungen zielen auf Spring-Boot-Umgebungen,
und dort ist Maven der Normalfall.

## Konsequenzen

**Positiv**

- Der Build ist erklärbar. Was in der `pom.xml` steht, passiert, und nichts
  weiter.
- Die Modulgrenze aus ADR-001 ist ab dem ersten Modul erzwungen, nicht erst
  durch den ArchUnit-Test.
- Reichlich Beispielcode und Fehlermeldungen mit Fundstellen im Netz — bei
  einem neuen Stack zählt das mehr als Eleganz.

**Negativ**

- Deutlich mehr Zeilen Build-Konfiguration, besonders bei mehreren Modulen.
- Kein inkrementelles Bauen. Der CI-Job wird länger als nötig.
- Wer aus einem Gradle-Projekt kommt, findet die XML-Verschachtelung mühsam.

## Wann wir anders entscheiden würden

- **Wenn der Build selbst Logik bräuchte** — Codegenerierung, mehrere
  Ziel-Plattformen, verzweigte Varianten. Dann ist Mavens Deklarativität eine
  Fessel und Gradle richtig.
- **Wenn die Build-Zeit weh täte.** Bei einem Monorepo mit vielen Modulen ist
  inkrementelles Bauen kein Komfort, sondern der Unterschied zwischen zwei und
  zwanzig Minuten.
- **Wenn Java kein neuer Stack wäre.** Das Hauptargument ist ein
  Lernargument. Es verfällt, sobald es nicht mehr zutrifft — und dann wäre ein
  Wechsel trotzdem teuer, weil er alle Module berührt. Das ist der Preis dieser
  Entscheidung.
