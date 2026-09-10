# Prompt 1: Wireframes

Dieser Text ist als **eigenständiger Prompt** formuliert und lässt sich
unverändert weiterreichen. Er setzt keinen Zugriff auf dieses Repository
voraus — alle nötigen Fachbegriffe und Regeln stehen darin.

**Reihenfolge:** Dieser Prompt kommt zuerst. Die visuelle Gestaltung folgt
danach mit [PROMPT-MOCKUP.md](PROMPT-MOCKUP.md).

Stand: 2026-09-09 · Ziel: Stufe 2 (Kalender-Grid) vorbereiten

---

Erstelle vier Wireframe-Artboards für **Aptum**, eine Terminplanungs-Software
für deutsche Physio- und Ergotherapiepraxen. Der Produktname gehört als
Wortmarke in die Kopfzeile jedes Screens. Die Oberfläche ist durchgehend auf
Deutsch, die Fachbegriffe sind unten definiert und werden **nicht übersetzt
oder vereinfacht**.

## Worum es geht

Das Produkt plant keine Termine im Sinne eines Kalenders. Es beantwortet die
Frage, ob eine bestimmte Behandlung zu einem bestimmten Zeitpunkt
**regelkonform** ist. Der Hintergrund: Eine Behandlung kann nachträglich
wertlos werden. Sie wird durchgeführt, dokumentiert, abgerechnet — und Monate
später von der Krankenkasse abgesetzt, weil zwischen zwei Terminen 15 statt
14 Tage Pause lagen und der Grund nicht vermerkt war. Die Arbeit ist
geleistet, das Geld kommt nicht.

Ein freier Termin ist deshalb keine Lücke im Kalender, sondern die
Schnittmenge aus vier Dimensionen:

1. **Therapeut** — Arbeitszeit minus Abwesenheiten minus gebuchte Termine,
   gefiltert nach Qualifikation. Nicht jede Person darf jede Leistung
   abrechnen.
2. **Raum** — Ausstattung und Belegung. Krankengymnastik am Gerät braucht
   mindestens 30 m² mit vier Pflichtgeräten.
3. **Patient** — Wunschfenster.
4. **Verordnung** — Fristen, Frequenz, Restkontingent.

**Die zentrale Gestaltungsaufgabe:** Diese Regelprüfung sichtbar machen, ohne
die Oberfläche in eine Formularhölle zu verwandeln. Das System soll den Fehler
im Moment der Buchung verhindern, nicht hinterher in einer Auswertung
berichten.

## Wer damit arbeitet

| Rolle | Stellt die Frage |
|---|---|
| Rezeption | „Wann kann diese Person das nächste Mal kommen?" |
| Therapeut:in | „Was steht heute an, und ist die Verordnung noch gültig?" |
| Praxisleitung | „Welche Verordnungen laufen in den nächsten zwei Wochen ab?" |
| Abrechnung | „Welche Einheiten sind erbracht und absetzungssicher?" |

Die Rezeption ist die Hauptnutzerin der ersten drei Screens. Sie arbeitet im
Stehen, zwischen zwei Patienten, oft mit einer Hand und unter Zeitdruck, mit
dem Telefon am Ohr.

## Die vier Artboards

### 1. Terminsuche

Der Einstieg. Zweispaltig: links die Suchkriterien, rechts die Vorschläge.

Die Suchkriterien sind sichtbar nach den **vier Dimensionen** gegliedert und
auch so nummeriert — das ist die zentrale Metapher des Produkts und darf in
der Gestaltung nicht verschwinden.

Muss sichtbar sein:
- Patient und gewählte Verordnung, dazu kompakt: Restkontingent (z. B. „4 von
  10"), Frequenz („2× wöchentlich"), Verfallsdatum
- Therapeutenfilter, der zeigt, **warum** jemand ausgeschlossen ist — nicht
  nur wer übrig bleibt. Beispiel: gefiltert auf Abrechnungserlaubnis für
  Manuelle Lymphdrainage, zwei von vier Personen erfüllen das
- Raumanforderungen, aus dem Heilmittel abgeleitet, nicht manuell gewählt
- Wunschfenster: Zeitraum, Wochentage, Tageszeit
- Ergebnisliste: je Vorschlag Datum, Uhrzeit, Dauer inklusive Rüstzeit,
  Therapeut, Raum — und kleine Marker, welche Regeln geprüft wurden
- **Ein Hinweis auf ausgeblendete Vorschläge mit Begründung.** Etwa: zwei
  Termine wurden weggelassen, weil die Verordnung dann bereits verfallen wäre.
  Das ist der Moment, in dem das Produkt seine Fachlogik zeigt.

### 2. Wochen-Kalender-Grid

Zeitgitter, Therapeut:innen als Spalten, Zeit als Zeilen, etwa 07:00 bis
19:00.

Muss sichtbar sein — und zwar als **visuell klar unterscheidbare Zustände**:
- gebuchter Termin (Patientenkürzel, Heilmittel)
- **Rüstzeit** vor und nach der Behandlung, dem Termin zugehörig, aber
  erkennbar abgesetzt
- **Nachruhe** — blockiert den Raum, aber nicht die Therapeutin. Dieser
  Unterschied muss im Bild lesbar sein, er ist fachlich wesentlich
- Abwesenheit (Urlaub, Fortbildung)
- freier Slot
- gesperrter Slot mit Grund

Dazu eine **Legende**, die die Muster erklärt, sowie eine Statuszeile am
unteren Rand, in der Buchungsergebnisse gemeldet werden.

Zeige außerdem eine **fokussierte Zelle mit sichtbarem Fokusring** — die
Tastaturbedienung ist hier kein Nachgedanke, siehe Barrierefreiheit unten.

### 3. Buchungsdialog mit Regelprüfung

Der wichtigste Screen. Modaler Dialog über abgedunkeltem Hintergrund.

Oben eine kompakte Zusammenfassung: Patient, Heilmittel, Therapeut, Raum,
Datum, Uhrzeit, Dauer.

Darunter die **Regelprüfung als benannte Liste** — jede Regel mit Name,
Ergebnis und einer Zeile Begründung in Klartext. Zeige gemischte Ergebnisse,
damit alle Zustände im Bild sind:

| Regel | Ergebnis | Begründung im Klartext |
|---|---|---|
| Verordnungsfrist | erfüllt | Behandlungsbeginn 12 Tage nach Ausstellung, zulässig sind 28 |
| Unterbrechung | erfüllt | Letzte Behandlung vor 6 Tagen, Grenze 14 Tage |
| Frequenz | Warnung | Verordnet 2× wöchentlich, dies wäre der 3. Termin in dieser Woche |
| Qualifikation | erfüllt | Abrechnungserlaubnis für Manuelle Lymphdrainage liegt vor |
| Raumausstattung | **verletzt** | Krankengymnastik am Gerät verlangt mindestens 30 m²; Raum 2 hat 24 m² |

Die verletzte Regel **blockiert die Buchung**. Der Dialog bietet stattdessen
eine **Übersteuerung mit Begründungspflicht** an: ein Textfeld, das ausgefüllt
sein muss, bevor die Schaltfläche aktiv wird, mit dem sichtbaren Hinweis, dass
die Begründung protokolliert wird.

Drei Schaltflächen: Abbrechen · Übersteuern mit Begründung (zunächst
deaktiviert) · Buchen (deaktiviert, solange eine Regel verletzt ist).

### 4. Verordnungsübersicht mit Fristenstatus

Links eine Liste der Verordnungen mit Ampelstatus, rechts die Detailansicht
einer ausgewählten Verordnung.

In der Detailansicht:
- die Kopfdaten der Verordnung (Muster 13): Heilmittel, Diagnosegruppe,
  Leitsymptomatik, verordnete Einheiten, Frequenz, ausstellende Praxis
- eine **waagerechte Zeitleiste** von der Ausstellung bis zum Verfall, mit
  eingezeichneten Behandlungsterminen, den Abständen dazwischen und den
  beiden kritischen Grenzen: spätester Behandlungsbeginn und Verfall
- Restkontingent als Zahl und als Balken
- eine Warnliste: was in den nächsten Tagen kippt, mit Datum

Die Zeitleiste ist das Herz dieses Screens. Sie macht in einem Blick sichtbar,
was sonst in vier Zahlenfeldern versteckt ist.

## Fachbegriffe (nicht übersetzen, nicht vereinfachen)

| Begriff | Bedeutung |
|---|---|
| **Verordnung** | Das ärztliche Rezept, Vordruck Muster 13. Legt Heilmittel, Anzahl der Behandlungseinheiten, Frequenz, Diagnosegruppe und Leitsymptomatik fest. |
| **Heilmittel** | Die verordnete Therapieleistung, z. B. Manuelle Lymphdrainage, Krankengymnastik am Gerät. |
| **Behandlungseinheit** | Eine einzelne Therapiesitzung. Zähleinheit für Kontingent und Abrechnung. |
| **Diagnosegruppe** | Kurzschlüssel des Heilmittelkatalogs, etwa WS, EX, ZN, LY (Physio) oder SB1, EN1, PS2 (Ergo). |
| **Leitsymptomatik** | Die Schädigung, die die Behandlung begründet. |
| **Frequenz** | Verordnete Behandlungshäufigkeit, z. B. „2× wöchentlich". Für die Praxis bindend. |
| **Rüstzeit** | Vor- und Nachbereitung um eine Behandlung herum. Praxisabhängiger Parameter, keine feste Zahl. |
| **Nachruhe** | Ruhephase nach Bädern und Bewegungsbad, Richtwert 20–25 Minuten. **Blockiert den Raum, nicht die Therapeutin.** |
| **Behandlungsunterbrechung** | Pause zwischen zwei Terminen. Ab mehr als 14 Kalendertagen verfällt die Verordnung, sofern kein dokumentierter Grund vorliegt. |
| **Dringlicher Behandlungsbedarf** | Ärztliches Kennzeichen, verkürzt den spätesten Behandlungsbeginn von 28 auf 14 Kalendertage. |
| **Zertifikatsleistung** | Leistung, die nur mit nachgewiesener Zusatzqualifikation erbracht werden darf. |
| **Abrechnungserlaubnis** | Die personenbezogene Freigabe, eine Zertifikatsleistung abzurechnen. |
| **Restkontingent** | Noch offene Behandlungseinheiten auf der Verordnung. |
| **Mandant** | Eine Praxis. Daten eines Mandanten sind für andere unsichtbar. |

Zwei Regeln, die man beim Gestalten kennen sollte, weil sie die Zahlen auf den
Screens erklären:

- Eine Verordnung verfällt, wenn die Behandlung nicht innerhalb von **28
  Kalendertagen** nach Ausstellung beginnt — bei gekennzeichnetem dringlichem
  Bedarf innerhalb von **14**.
- Eine Unterbrechung von mehr als **14 Kalendertagen** lässt die Verordnung
  verfallen, außer sie ist begründet.

## Harte Gestaltungsvorgaben

**Nie Farbe allein.** Jeder Zustand — frei, belegt, gesperrt, Warnung,
Regelverstoß — muss zusätzlich durch Text, Symbol oder Muster erkennbar sein.
Das gilt besonders im Kalender-Grid, wo die Versuchung am größten ist, alles
über Farbflächen zu lösen. Prüfe die Entwürfe in Graustufen: Wenn dann
Information verloren geht, ist der Entwurf nicht fertig.

**Kontrast mindestens 4.5:1** für Text, 3:1 für große Schrift und für die
Ränder von Bedienelementen.

**Tastaturbedienung ist sichtbar mitgedacht.** Das Grid ist ein
Grid-Widget, keine Tabelle mit Klick-Handlern: Ein sichtbarer Fokusring gehört
in den Entwurf, im Dialog ebenso. In einer Praxis wird der Kalender im Stehen
zwischen zwei Patienten bedient — Tastaturbedienbarkeit ist dort
Arbeitsgeschwindigkeit, bevor sie Barrierefreiheit ist.

**Ausschließlich erkennbare Testdaten.** Keine realistisch wirkenden
Patientennamen. Verwende Kürzel wie „Testfall A · P-0042", Therapeut:innen
heißen „T. Alpha", „T. Beta". Das ist eine Projektregel, keine Vorliebe: Die
Domäne verarbeitet Gesundheitsdaten.

**Dichte über Großzügigkeit.** Das ist ein Arbeitswerkzeug für den ganzen
Arbeitstag, kein Marketing-Screen. Viel Information pro Fläche, aber klar
gegliedert.

## Fidelity und Stil

**Wireframes, nicht Hochglanz.** Struktur und Informationshierarchie stehen im
Vordergrund. Zurückhaltende Farbigkeit: neutrale Grundfläche, eine Akzentfarbe,
Zustände primär über Muster, Rahmen und Text. Statische Entwürfe genügen, keine
funktionierenden Bedienelemente.

Bitte vermeiden: Verlaufshintergründe, Emoji als Symbole, Karten mit rundem
Rahmen und farbigem linkem Balken, sowie die Schriftarten Inter, Roboto und
Arial. Symbole als Strich-SVG in einem einheitlichen Stil.

**Format:** vier Artboards, Desktop 1440 × 900 px; der Buchungsdialog darf ein
kleineres Artboard sein. Deutsche Beschriftungen durchgehend.
