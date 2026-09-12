# Von Instanzen zu Mandanten — ein Konzept

Die Ausschreibung nennt als Aufgabe die „Harmonisierung mehrerer
Systeminstanzen“ (SA2). Das ist eine Bestandsaufgabe: Sie setzt Software
voraus, die über Jahre in mehreren Installationen gewachsen ist. Ein
Portfolio-Projekt hat keine Bestandsinstanzen und kann diese Erfahrung nicht
vorweisen — das steht so in `ANFORDERUNGEN.md`, Abschnitt D, und bleibt dort
stehen.

Was ein Portfolio-Projekt leisten kann, ist zu zeigen, *wie* man an eine
solche Aufgabe herangeht, und welche Entscheidungen im eigenen Code sie schon
vorbereiten. Das ist dieses Dokument. Es ist ein Konzept, kein Erfahrungsbericht,
und es nennt an jeder Stelle, was Annahme ist.

## 1. Die Ausgangslage, wie sie typischerweise ist

**Annahme:** Eine Praxissoftware, die pro Kunde installiert wurde — bei
jeder Praxis oder jedem Praxisverbund eine eigene Instanz mit eigener
Datenbank. Über die Jahre sind die Instanzen auseinandergelaufen:

- **Versionsstände.** Nicht jede Praxis hat jedes Update eingespielt. Das
  Schema ist ähnlich, nicht gleich; einzelne Spalten existieren nur in
  manchen Instanzen.
- **Stammdaten.** Heilmittel, Diagnosegruppen, Räume, Qualifikationen sind
  je Instanz erfasst worden — mit eigenen Schreibweisen, eigenen Kürzeln,
  teils frei getippt.
- **Eigenheiten.** Was in einer Praxis „so gemacht wird“, steckt als
  Konfiguration, als Sonderfall im Code oder als Gewohnheit der Benutzer in
  der Instanz. Manches davon ist Fachregel, manches Praxisentscheidung,
  manches Zufall.
- **Kennungen.** Jede Instanz vergibt ihre Kennungen selbst. Patient 4711
  gibt es in jeder Instanz, und es ist jedes Mal jemand anders.
- **Betrieb.** Jede Instanz wird einzeln aktualisiert, gesichert und
  überwacht — oder nicht.

Das Ziel ist eine mandantenfähige Anwendung: eine Installation, ein Schema,
eine Version, und jede Praxis sieht nur ihre Daten.

## 2. Was zuerst kommt: die Trennung von Regel und Einstellung

Bevor eine Zeile migriert wird, muss eine Frage je Eigenheit beantwortet sein:
**Ist das eine Fachregel oder eine Praxiseinstellung?**

- Eine **Fachregel** gilt für alle, weil sie aus der Heilmittel-Richtlinie
  oder den Verträgen nach § 125 SGB V kommt. Die 28-Tage-Frist für den
  Behandlungsbeginn ist keine Praxismeinung. Sie gehört in den Regelkern,
  einmal, mit Fundstelle — in Aptum: eine Regelklasse, eine ID in
  `regeln.md`, geprüft von `regel-check` (ADR-007).
- Eine **Praxiseinstellung** ist eine Entscheidung, die eine Praxis anders
  treffen darf als die nächste: Rüstzeit zwischen Behandlungen, Nachruhe nach
  Wärmeanwendung, Ausfallhonorar bei kurzfristiger Absage. In Aptum ist das
  die `Praxiseinstellung`, ein Wert je Mandant, den die Regeln lesen.

Der schwierige Fall ist die Eigenheit, die als Fachregel im Code steht, aber
eine Praxisentscheidung ist — oder umgekehrt. Das Ausfallhonorar ist das
Beispiel aus diesem Repo: Verbreitet als „Regel mit 24-Stunden-Frist“, aber
ohne sozialrechtliche Grundlage, eine zivilrechtliche Vereinbarung nach
§ 615 BGB (`OFFENE-PUNKTE.md`, Punkt 5). Wer das als Regel migriert, zwingt
allen Praxen die Vereinbarung einer auf. Deshalb ist es in Aptum eine
Einstellung.

Das Ergebnis dieses Schritts ist eine Liste: je Eigenheit eine Zeile, Regel
oder Einstellung, mit Fundstelle oder mit dem Vermerk „Praxisentscheidung“.
Diese Liste ist das wichtigste Dokument der Harmonisierung, und sie entsteht
mit den Praxen, nicht am Schreibtisch.

## 3. Das Zielschema ist entschieden, nicht neu

Aptum trennt Mandanten über eine Spalte `mandant_id` in jeder Tabelle,
erzwungen durch Row Level Security in Postgres (ADR-002). Die Entscheidung
hat drei Alternativen abgewogen — Spalte, Schema je Mandant, Datenbank je
Mandant — und ist mit Bedingungen versehen: Dutzende Mandanten, nicht
Tausende; Postgres.

Für die Harmonisierung heißt das: Jede Instanz wird **ein Mandant**. Ihre
Daten bekommen beim Import eine `mandant_id`, und ab dann sorgt die
Datenbank dafür, dass eine Praxis die andere nicht sieht — nicht die
Disziplin einer Abfrage. Der Isolationstest gegen echtes Postgres
(`MandantIsolationTest`) ist genau der Test, den man nach jedem Import
laufen lässt.

Ob ein Schema je Mandant besser wäre, ist in ADR-002 beantwortet, samt der
Bedingung, unter der man anders entscheiden würde: Wenn Löschung und Export
je Praxis im Vordergrund stehen, ist `DROP SCHEMA` die einfachere Antwort.
Bei einer Harmonisierung, in der Praxen später auch zusammengelegt oder
getrennt werden, wäre das neu abzuwägen.

## 4. Die Migration je Instanz

Je Instanz vier Schritte, in dieser Reihenfolge, jeder mit einer Prüfung,
die rot werden kann.

**4.1 Bestandsaufnahme.** Schema der Instanz gegen das Zielschema
vergleichen: Welche Tabellen und Spalten fehlen, welche sind zusätzlich,
welche haben denselben Namen und eine andere Bedeutung. Datenqualität
messen, bevor man sie bewertet: Wie viele Verordnungen ohne
Ausstellungsdatum, wie viele Termine ohne Verordnung, wie viele Heilmittel,
die in keinem Katalog stehen. Das Ergebnis ist eine Zahl je Befund, nicht ein
Gefühl.

**4.2 Stammdaten auf einen Katalog.** Heilmittel und Diagnosegruppen sind
keine Praxisdaten, sondern Katalogdaten aus der Heilmittel-Richtlinie. In
Aptum sind sie Aufzählungen im Domänenmodell (`Heilmittel`,
`Diagnosegruppe`), mit ihren Fachwerten und Fundstellen. Die frei getippten
Werte der Instanz werden auf diesen Katalog abgebildet — „KG“, „Kranken-
gymnastik“, „KG Einzel“ auf `KG_EINZEL`. Was sich nicht abbilden lässt,
wird nicht geraten, sondern als offen ausgewiesen, Zeile für Zeile. Das ist
dasselbe Prinzip wie im AI-Layer: Was nicht lesbar ist, bleibt leer statt
erfunden.

Räume, Personen und Qualifikationen sind Praxisdaten und bleiben je Mandant;
sie werden übernommen, nicht harmonisiert. Aptum hält sie heute in einer
`Stammdaten`-Schnittstelle mit einer festen Testpraxis dahinter — der
Adapter je Mandant ist die erste Erweiterung, die eine echte Migration
verlangt (siehe Abschnitt 6).

**4.3 Daten übernehmen, Kennungen neu vergeben.** Jede Instanz hat ihre
Kennungen selbst vergeben; im Zielsystem kollidieren sie. Aptum verwendet
UUIDs (`VerordnungId`, `TerminId`), und der Import vergibt neue. Die alte
Kennung wird in einer Zuordnungstabelle je Mandant aufbewahrt — nicht im
Domänenmodell, sondern im Adapter —, damit ein Beleg aus der alten Instanz
noch zur neuen Verordnung führt. Der Import läuft als eine Transaktion je
Instanz, mit gesetzter `mandant_id`; Row Level Security verhindert, dass eine
Zeile in den falschen Mandanten fällt (`WITH CHECK`).

**4.4 Fachliche Prüfung mit den bestehenden Regeln.** Hier wird die
Migration zur Fachfrage, und hier hilft der Regelkern am meisten: Nach dem
Import laufen die Verordnungsregeln über jeden Verlauf — dieselbe Methode,
die die Abrechnungsübersicht heute schon nutzt (`Regelwerk.pruefeErbrachtes`,
ADR-011). Die Abrechnungsübersicht des migrierten Mandanten zeigt dann, wie
viele Verordnungen prüffest sind und wie viele beanstandet, und *warum*. Das
ist keine technische, sondern eine fachliche Abnahme: Stimmt die Zahl mit dem
überein, was die Praxis über ihre Verordnungen weiß? Weicht sie ab, ist
entweder die Migration falsch oder die alte Instanz hat Regeln nicht
geprüft, die gelten — beides will man wissen, bevor man umschaltet.

## 5. Der Umstieg

- **Parallelbetrieb mit Lesezugriff.** Die alte Instanz bleibt lesbar, bis
  die Praxis den ersten Abrechnungslauf aus dem neuen System hinter sich hat.
  Ein Abrechnungslauf ist der natürliche Prüfpunkt: Wenn die Zahlen stimmen,
  stimmt der Rest mit hoher Wahrscheinlichkeit auch.
- **Eine Instanz nach der anderen**, nicht alle auf einmal — und die erste
  ist die mit den wenigsten Eigenheiten, nicht die größte. Was dort gelernt
  wird, macht die zweite billiger.
- **Rückweg definiert.** Solange die alte Instanz steht, ist der Rückweg das
  Stoppen des neuen Mandanten. Danach gibt es keinen; das ist der Moment, den
  man mit der Praxis ausdrücklich vereinbart.

## 6. Was Aptum dafür schon hat — und was fehlt

| Vorhanden | Wo | Fehlt für eine echte Harmonisierung |
|---|---|---|
| Mandantentrennung per RLS, mit Isolationstest | ADR-002, `V1__mandantentrennung.sql`, `MandantIsolationTest` | Rollen und Verschlüsselung je Mandant, wenn ein Auditor sie verlangt |
| Regeln als benannte Klassen mit Fundstelle | `domain/regel/`, `regeln.md`, ADR-007 | Die Liste „Regel oder Einstellung“ je Eigenheit der Bestandsinstanzen |
| Praxiseinstellung als Wert, den Regeln lesen | `Praxiseinstellung`, `Stammdaten` | Ein Stammdaten-Adapter je Mandant statt der festen Testpraxis |
| Katalogdaten als Aufzählung | `Heilmittel`, `Diagnosegruppe` | Die Abbildungstabelle alter Schreibweisen auf den Katalog |
| Kennungen als UUID | `VerordnungId`, `TerminId` | Die Zuordnungstabelle alt → neu je Mandant |
| Fachliche Abnahme über die Regeln | `Regelwerk.pruefeErbrachtes`, Abrechnungsübersicht | Ein Importpfad, der sie nach dem Laden aufruft |
| Mandant im Anfragekontext, nicht im Modell | `MandantFilter`, `MandantKontextHalter` | Ein signiertes Token statt `X-Mandant` (`OFFENE-PUNKTE.md`, Punkt 8) |
| Ein Deployment für alle Mandanten | `deploy/`, ADR-010 | Werte je Mandant außerhalb des Charts, ein Ort für Geheimnisse |

Die rechte Spalte ist Arbeit, die nur mit echten Instanzen entsteht. Sie
steht hier, damit sichtbar ist, dass die linke Spalte nicht zufällig so
geschnitten ist.

## 7. Was dieses Konzept nicht ist

Kein Erfahrungsbericht. Niemand hat mit diesem Repo zwanzig Instanzen
zusammengeführt, und die Stellen, an denen es in Wirklichkeit weh tut — die
eine Praxis, deren Daten seit 2014 niemand bereinigt hat; die Eigenheit, die
sich als Vertragsbestandteil herausstellt; der Abrechnungslauf, der am
Monatsende trotzdem raus muss — stehen hier als Annahmen, nicht als
Erinnerungen. SA2 bleibt deshalb im Mapping „teilweise belegbar“: das
Vorgehen ist da, die Jahre nicht.
