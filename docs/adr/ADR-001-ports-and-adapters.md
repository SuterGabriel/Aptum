# ADR-001: Ports and Adapters, Domäne ohne Framework

- **Status:** angenommen
- **Datum:** 2026-09-09
- **Stufe:** 0

## Kontext

Der Kern dieses Projekts ist die Slot-Berechnung: die Schnittmenge aus
Therapeutenverfügbarkeit, Raumausstattung, Patientenwunsch und den Fristen und
Frequenzregeln einer Verordnung. Das ist ein Constraint-Problem mit vielen
Grenzfällen — überlappende Abwesenheiten, Termin exakt an der Frequenzgrenze,
Verordnung läuft mitten in der Serie ab, Sommerzeitumstellung innerhalb eines
Serientermins.

Diese Fälle müssen in Millisekunden und in großer Zahl testbar sein. Sie
hängen nicht an einer Datenbank und nicht an HTTP.

Erschwerend: Java und Spring sind für mich neuer Stack. Framework-Magie
(Proxying, Lazy Loading, Transaktionsgrenzen) beim Erlernen der Fachlogik
gleichzeitig mitzudenken ist der sicherste Weg, beides halb zu verstehen.

## Optionen

**1. Klassische Schichtung (Controller → Service → Repository, Spring durchgehend)**
Der verbreitete Spring-Zuschnitt. Schnell zu schreiben, viel Beispielcode
vorhanden. Die Fachlogik landet erfahrungsgemäß in `@Service`-Klassen, die
gegen JPA-Entities arbeiten. Tests brauchen dann einen Spring-Kontext oder
umfangreiches Mocking.

**2. Ports and Adapters (hexagonal)**
Ein Domain-Modul aus reinem Java: Records als Value Objects, `java.time`,
benannte Regelklassen, keine Annotationen. Ports als Interfaces im Domain-Modul,
Implementierungen im Infrastructure-Modul. Spring existiert nur außen.

**3. Vertical Slices**
Schnitt nach Feature statt nach Schicht. Gut bei vielen unabhängigen
Anwendungsfällen. Hier gibt es aber genau einen fachlichen Kern, den alle
Anwendungsfälle teilen — der Schnitt würde ihn zerlegen.

## Entscheidung

**Option 2.** Das Domain-Modul enthält keine Framework-Abhängigkeit. Ein
ArchUnit-Test prüft das bei jedem CI-Lauf, nicht nur die Disziplin.

Konkret:
- Regeln der Heilmittel-Richtlinie sind benannte Klassen mit
  `@ParameterizedTest`, nicht `if`-Blöcke in einem Service.
- Repository-Interfaces liegen im Domain-Modul, ihre Implementierungen im
  Infrastructure-Modul.
- Über Modulgrenzen wird kein `null` gereicht.
- JPA-Entities werden nie als API-DTO durchgereicht.

## Konsequenzen

**Positiv**
- Die Grenzfälle laufen als reine Unit-Tests in Sekunden. Genau die Liste, die
  dieses Projekt trägt, ist damit billig und oft ausführbar.
- Der Domänenkern ist der Einstiegspunkt in Java, ohne gleichzeitig Spring
  lernen zu müssen.
- Die Regel ist maschinell prüfbar. Ein Agent, der eine Spring-Annotation ins
  Domain-Modul schreibt, bekommt die CI rot zurück statt eines Review-Kommentars.

**Negativ**
- Mapping zwischen Domänenmodell und Persistenzmodell ist Handarbeit. Das
  kostet Zeilen, die bei Option 1 entfielen.
- Mehr Module, mehr Build-Konfiguration.
- Wer den Zuschnitt nicht kennt, sucht Code zunächst an der falschen Stelle.
  Gegenmittel: Skill `java-spring-hexagonal` mit Negativbeispielen.

## Wann wir anders entscheiden würden

- **Wenn die Anwendung überwiegend CRUD wäre.** Bei dünner Fachlogik ist die
  Mapping-Schicht reiner Aufwand ohne Gegenwert. Dann Option 1.
- **Wenn das Team Spring sehr gut und Hexagonal gar nicht kennt.** Ein Zuschnitt,
  den niemand einhält, ist schlechter als ein konventioneller, den alle kennen.
- **Wenn die Fachlogik überwiegend als Mengenoperation auf großen Datenmengen
  stattfände.** Slot-Berechnung im Speicher setzt voraus, dass die relevante
  Datenmenge pro Anfrage klein ist — ein Therapeut, ein Zeitraum. Wäre über
  Tausende Therapeuten gleichzeitig zu rechnen, gehörte die Vorauswahl in die
  Datenbank, und der reine Domänenkern verlegte sich auf die Nachprüfung.
