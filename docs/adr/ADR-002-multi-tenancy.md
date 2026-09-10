# ADR-002: Mandantentrennung über eine Spalte, abgesichert durch Row Level Security

- **Status:** angenommen
- **Datum:** 2026-09-10
- **Stufe:** 1

## Kontext

Aptum ist als Software für mehrere Praxen gedacht. Jede Praxis ist ein
Mandant, und die Daten eines Mandanten sind für andere unsichtbar — so steht
es im Glossar, so verlangt es `DATENSCHUTZ.md`, und die Ausschreibung nennt
den Ausbau der Multi-Mandanten-Architektur als Aufgabe (SA3). Bisher gibt es
im Repo dazu ein Wort, keinen Typ und keine Entscheidung.

Die Entscheidung ist jetzt fällig, nicht später: Sie bestimmt, wie die erste
Tabelle geschnitten wird, wie Migrationen laufen und wie eine Abfrage
aussieht. Ist die erste Tabelle da, ist ein Wechsel teuer.

Die Kräfte, die gegeneinander wirken:

- **Isolation muss ausfallsicher sein.** Der gefährliche Fehler ist nicht
  der böswillige Zugriff, sondern die vergessene `WHERE`-Klausel in einer
  Abfrage, die ein Agent um drei Uhr nachts generiert hat. Die Trennung darf
  nicht an der Disziplin jeder einzelnen Abfrage hängen.
- **Der Betrieb muss klein bleiben.** Eine Praxis-Software mit zwanzig
  Mandanten rechtfertigt keine zwanzig Datenbanken. Das ist ein
  Portfolio-Projekt mit einem Menschen dahinter, und Stufe 4 sieht Terraform,
  Helm und ArgoCD vor — je mehr Bewegliches je Mandant, desto mehr davon.
- **Datenschutz zieht in die andere Richtung.** Löschung auf Verlangen
  (Art. 17 DSGVO), Datenexport, Auftragsverarbeitung je Praxis — alles ist
  einfacher, wenn die Daten eines Mandanten physisch beieinanderliegen.

**Annahmen, als solche markiert:** Die Datenbank ist Postgres — das steht
nirgends entschieden, aber das CI-Gerüst nennt seit Stufe 0 „Testcontainers
mit echtem Postgres". Die Mandantenzahl liegt im Bereich von Dutzenden, nicht
Tausenden. Beides ist im Gespräch zu klären; beides würde die Entscheidung
unten kippen, wenn es nicht stimmt.

## Optionen

**1. Eine Spalte `mandant_id` in jeder Tabelle, gemeinsames Schema**

Der verbreitete Weg. Eine Datenbank, ein Schema, eine Migration. Jede Zeile
trägt ihren Mandanten, jede Abfrage filtert danach — Hibernate kann das mit
einem Mandantenfilter automatisch anhängen. Billig im Betrieb, vertraut für
jedes Team, und Testcontainers braucht genau eine Datenbank.

Der Einwand ist der oben genannte: Die Isolation hängt an der Disziplin. Ein
nativer SQL-Aufruf, ein Report, eine Abfrage außerhalb des Hibernate-Filters —
und Mandant A sieht Mandant B. Ohne zweite Verteidigungslinie ist das eine
Frage der Zeit.

**2. Ein Schema je Mandant**

Dieselbe Datenbank, aber jeder Mandant hat sein eigenes Schema mit eigenen
Tabellen. Die Verbindung wird beim Anmelden auf das Schema geroutet, und
danach *kann* eine Abfrage gar nicht in ein fremdes Schema greifen — es gibt
keine `WHERE`-Klausel, die man vergessen könnte.

Das ist ein echter Vorteil, und dazu ein zweiter: Löschung, Export und
Wiederherstellung je Mandant sind ein `DROP SCHEMA`, ein `pg_dump -n`, ein
Restore. Für den Datenschutz ist das die sauberste Antwort, die es ohne
eigene Datenbank gibt. Wer Option 2 wählt, hat gute Gründe.

Der Einwand: Migrationen laufen je Schema, also zwanzigmal. Die Verbindung
muss geroutet werden, und das Routing ist genau die Stelle, an der ein
Fehler denselben Schaden anrichtet wie die vergessene `WHERE`-Klausel — nur
seltener und schwerer zu testen. Und Hibernate mit Schema-Routing ist deutlich
weniger vertraut als Hibernate mit einem Filter.

**3. Eine Datenbank je Mandant**

Die stärkste Isolation. Eigene Zugangsdaten, eigene Sicherung, eigene
Verschlüsselung je Mandant — das ist die Antwort, die eine Klinikkette oder
ein Auditor hören will. Der Einwand ist der Betrieb: zwanzig Datenbanken sind
zwanzig Dinge, die laufen, gesichert, migriert und überwacht werden müssen.
Für ein Projekt dieser Größe ist das nicht verhältnismäßig.

## Entscheidung

**Option 1, mit einer zweiten Verteidigungslinie, die den Einwand aufhebt:
Row Level Security in Postgres.**

- Jede Tabelle mit Mandantendaten trägt `mandant_id NOT NULL`.
- Postgres erzwingt über Row Level Security, dass eine Verbindung nur Zeilen
  ihres Mandanten sieht. Der Mandant wird je Transaktion als
  Sitzungsvariable gesetzt; ohne sie liefert jede Abfrage **null Zeilen**.
  Die vergessene `WHERE`-Klausel fällt damit *geschlossen* aus, nicht offen.
- Hibernate hängt den Mandantenfilter zusätzlich an — nicht als Sicherheit,
  sondern damit die Abfragen den Index nutzen und die Absicht im Code lesbar
  ist. Die Sicherheit liegt in der Datenbank.
- Der Mandant lebt im Sicherheitskontext der Anfrage, nicht in den
  Domänenobjekten. Eine `Verordnung` weiß nicht, welcher Praxis sie gehört;
  die Persistenz weiß es. Das ist Regel 4 aus `DATENSCHUTZ.md`: Die
  Mandanten-ID gehört nie in eine API-Antwort.
- **Der Isolationstest ist das Gate.** Ein Test mit Testcontainers legt Daten
  für zwei Mandanten an und prüft, dass keine Abfrage — auch keine native —
  die des anderen sieht. Er läuft in der CI, bevor die erste Abfrage
  produktiv geht. Ein Mandantenkonzept ohne diesen Test ist eine Behauptung.

Erkennbar im Repo, sobald die Anwendungsschicht steht: ein Typ `MandantId`
im Sicherheitskontext, eine Migration mit `ENABLE ROW LEVEL SECURITY` je
Tabelle, und der Isolationstest im CI-Job `backend-it`.

## Konsequenzen

**Positiv**

- Die Isolation hängt nicht mehr an der Disziplin jeder Abfrage. Ein Agent,
  der eine Abfrage ohne Mandantenbezug schreibt, bekommt null Zeilen, nicht
  fremde — und der Isolationstest wird rot.
- Ein Schema, eine Migration, eine Datenbank in Testcontainers. Der Betrieb
  bleibt so klein wie das Projekt.
- Die Mandanten-ID bleibt aus dem Domänenkern draußen. Die Regeln wissen
  nichts von Praxen, und das ist richtig: Eine Frist ist eine Frist.

**Negativ**

- **Die Sitzungsvariable ist der neue Ort zum Vergessen.** Wird sie nicht
  gesetzt, liefert jede Abfrage nichts — das fällt sofort auf, aber es fällt
  auf. Jede Transaktion braucht die Verdrahtung, und die gehört an eine
  einzige Stelle, nicht in jeden Service.
- **Löschung und Export je Mandant sind Abfragen, keine Operationen.**
  Art. 17 DSGVO wird zu `DELETE ... WHERE mandant_id = ?` über jede Tabelle,
  in der richtigen Reihenfolge. Option 2 hätte das mit einem Befehl erledigt.
- **Kein Mandant kann physisch getrennt gesichert werden.** Wer das verlangt,
  bekommt es mit dieser Entscheidung nicht.
- **Row Level Security ist Postgres-spezifisch.** Ein Datenbankwechsel nähme
  die zweite Verteidigungslinie mit. Das ist verschmerzbar — es gibt keinen
  Grund für einen Wechsel —, aber es ist eine Bindung.
- Nachbarn teilen sich die Ressourcen. Ein Mandant mit einer schweren
  Auswertung bremst die anderen.

## Wann wir anders entscheiden würden

- **Wenn ein Mandant physische Trennung verlangt** — eine Klinikkette, ein
  Auditor, eine Krankenkasse als Kunde. Dann Option 3 für diesen Mandanten,
  und die Frage, ob das Modell zwei Betriebsarten tragen kann.
- **Wenn Löschung und Export je Mandant zum Alltag würden.** Bei
  regelmäßigen Anfragen nach Art. 17 oder Art. 20 DSGVO ist Option 2 die
  ehrlichere Antwort: ein Befehl statt einer Abfragekette über alle Tabellen.
- **Wenn die Mandantenzahl in die Tausende ginge.** Dann ist Option 1 zwar
  weiter richtig, aber die Frage nach Sharding käme dazu, und die stellt sich
  hier nicht.
- **Wenn die Datenbank nicht Postgres wäre.** Ohne Row Level Security bleibt
  von Option 1 nur die Disziplin — und dann wäre Option 2 der sicherere Weg.
- **Wenn der Isolationstest nicht gebaut würde.** Ohne ihn ist diese
  Entscheidung eine Behauptung, und dann wäre Option 2 mit ihrem
  strukturellen Schutz vorzuziehen — Struktur schlägt Disziplin, wenn niemand
  die Disziplin prüft.
