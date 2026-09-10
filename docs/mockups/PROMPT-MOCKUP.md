# Prompt 2: Hochauflösendes Mockup

Dieser Text ist als **eigenständiger Prompt** formuliert und lässt sich
unverändert weiterreichen. Er setzt keinen Zugriff auf dieses Repository
voraus.

**Reihenfolge:** Dieser Prompt kommt *nach* den Wireframes aus
[PROMPT-WIREFRAMES.md](PROMPT-WIREFRAMES.md). Die Wireframes klären Struktur
und Informationshierarchie, dieser Schritt klärt die visuelle Sprache. Wer
beides gleichzeitig entscheidet, entscheidet beides schlechter.

Stand: 2026-09-09 · Ziel: Stufe 2 (Kalender-Grid)

---

Entwirf die visuelle Gestaltung für **Aptum**, eine Terminplanungs-Software für
deutsche Physio- und Ergotherapiepraxen. Der Produktname gehört als Wortmarke in
die Kopfzeile; eine schlichte Wortmarke genügt, kein Bildzeichen nötig. Die Struktur der Screens ist bereits als
Wireframe geklärt — hier geht es um Typografie, Farbe, Zustände und Dichte.
Die Oberfläche ist durchgehend deutsch.

## Was für ein Produkt das ist

Eine Fachanwendung, in der Mitarbeiterinnen einer Praxis **acht Stunden am
Tag** arbeiten. Kein Marketing-Screen, keine Consumer-App. Der Vergleichspunkt
ist eher ein Buchungssystem oder eine Warenwirtschaft als eine
Produktivitäts-App.

Bedienkontext, der die Gestaltung bestimmt: Die Rezeption arbeitet im Stehen,
zwischen zwei Patienten, häufig mit dem Telefon am Ohr und einer freien Hand,
unter Zeitdruck. Sie schaut hundertmal am Tag auf denselben Wochenkalender.
Daraus folgt zweierlei: **hohe Informationsdichte** ist ein Vorteil, kein
Makel — und alles, was bei der ersten Betrachtung charmant wirkt und bei der
tausendsten nervt (Animationen, kräftige Flächen, dekorative Elemente), ist
falsch.

Die Software beantwortet im Kern eine einzige Frage: Ist diese Behandlung zu
diesem Zeitpunkt regelkonform? Sie prüft ärztliche Verordnungen gegen
gesetzliche Fristen. Ein übersehener Verstoß bedeutet, dass eine geleistete
Behandlung Monate später nicht bezahlt wird. Der Ton der Gestaltung ist
entsprechend: **sachlich, verlässlich, medizinisch-nüchtern** — nicht
verspielt, nicht startup-bunt, aber auch nicht behördlich-grau. Vertrauen
durch Klarheit.

## Zu gestaltende Screens

1. **Terminsuche** — zweispaltig, links Suchkriterien nach vier Dimensionen
   gegliedert, rechts Trefferliste
2. **Wochen-Kalender-Grid** — Therapeut:innen als Spalten, Zeit als Zeilen,
   07:00 bis 19:00
3. **Buchungsdialog mit Regelprüfung** — modal, mit einer Liste geprüfter
   Regeln in drei Zuständen
4. **Verordnungsübersicht** — Liste plus Detail mit einer Fristen-Zeitleiste

Dazu, als eigenes Artboard: **ein Token- und Komponentenblatt** (siehe
„Pflicht-Lieferbestandteil" unten).

## Die gestalterische Kernaufgabe

Das Kalender-Grid muss **sechs Zustände** gleichzeitig und auf engem Raum
unterscheidbar machen:

| Zustand | Fachliche Besonderheit |
|---|---|
| gebuchter Termin | Patientenkürzel und Heilmittel lesbar |
| **Rüstzeit** | Vor- und Nachbereitung, gehört zum Termin, ist aber keine Behandlung |
| **Nachruhe** | Blockiert den **Raum**, nicht die Therapeutin. Dieser Unterschied muss im Bild lesbar sein. |
| Abwesenheit | Urlaub, Fortbildung |
| freier Slot | die eigentliche Zielfläche |
| gesperrt | mit erkennbarem Grund |

Das ist die schwierigste Stelle des ganzen Entwurfs. Sechs Zustände in Zellen
von etwa 120 × 30 Pixeln, ohne dass die Fläche zum Flickenteppich wird.

## Harte Vorgaben

**Nie Farbe allein.** Jeder Zustand muss zusätzlich durch Text, Symbol,
Rahmen oder Muster erkennbar sein. Prüfung: Wenn der Entwurf in Graustufen
Information verliert, ist er nicht fertig. Etwa acht Prozent der Männer haben
eine Rot-Grün-Sehschwäche — bei „gebucht rot, frei grün" ist das keine
Randgruppe.

**Kontrast mindestens 4.5:1** für Text, **3:1** für große Schrift und für die
Ränder von Bedienelementen. Das wird in diesem Projekt nicht geschätzt,
sondern in der Continuous Integration aus den Farbtoken **nachgerechnet**.
Farbwerte, die das nicht einhalten, fallen später auf und müssen ersetzt
werden — besser gleich richtig wählen.

**Sichtbarer Fokuszustand** für jedes Bedienelement, und zwar entworfen, nicht
dem Browser überlassen. Das Grid wird mit den Pfeiltasten bedient; der
Fokusring ist dort ein Hauptbedienelement und muss auch auf farbigen Flächen
sichtbar bleiben.

**Mindestens 44 Pixel** für alles, was angetippt oder angeklickt wird.
Ausnahme sind die Grid-Zellen selbst, die dichter sein dürfen — dort ist der
Kompromiss bewusst, weil ein Arbeitstag in eine Bildschirmhöhe passen muss.

**Reduzierte Bewegung** wird respektiert: Übergänge im Grid werden dann
abgeschaltet, nicht nur verkürzt.

**Ausschließlich erkennbare Testdaten.** Keine realistisch wirkenden
Patientennamen. Kürzel wie „Testfall A · P-0042", Therapeut:innen als „T.
Alpha", „T. Beta". Das ist eine Projektregel: Die Domäne verarbeitet
Gesundheitsdaten.

## Was zu entscheiden ist

**Typografie.** Ein bis zwei Schriften, eine davon mit Tabellenziffern für
Uhrzeiten und Mengen — in einem Kalender müssen Ziffern untereinander stehen.
Bitte nicht Inter, Roboto oder Arial. Eine Schrift, die bei 11 bis 12 Pixeln
noch klar bleibt, ist wichtiger als eine, die in der Überschrift beeindruckt.

**Farbe ist bereits festgelegt** — siehe die Palette unten. Sie ist nicht
geschätzt, sondern durchgerechnet: Alle geforderten Kontrastpaare halten
die WCAG-Schwellen ein, geprüft von einem Skript, das in der Continuous
Integration mitläuft. Verwende diese Werte. Wenn du eine Farbe ergänzen musst,
nenne sie und gib den Kontrastwert an, gegen den sie geprüft gehört.

Ich erwarte **weniger** Farbe als üblich, nicht mehr: Farbe ist in dieser
Anwendung ein knappes Signalmittel für Regelverstöße und darf nicht durch
Dekoration entwertet werden.

**Dichte.** Lege eine Zeilenhöhe für das Grid fest und rechne vor, wie viele
Stunden damit ohne Scrollen auf einen üblichen Praxisbildschirm passen.

**Dunkelmodus:** bitte begründet ja oder nein. Für eine Praxis am Empfangstresen
mit Tageslicht spricht wenig dafür — aber wenn du ihn vorschlägst, dann als
vollständige zweite Token-Ebene, nicht als invertierte Fassung.

## Die festgelegte Palette

Verwende diese Werte. Die Benennung folgt einer Regel des Projekts:
technische Struktur englisch, Zustände der Domäne deutsch.

**Grundflächen und Text**

| Token | Hex | Verwendung |
|---|---|---|
| `--surface-page` | `#F6F5F2` | Seitenhintergrund |
| `--surface-raised` | `#FFFFFF` | Karten, Dialoge, Tabellenzeilen |
| `--surface-sunken` | `#EBE9E4` | eingelassene Flächen, Kopfzeilen |
| `--border-subtle` | `#D9D5CD` | rein trennend |
| `--border-strong` | `#8C867B` | Ränder von Bedienelementen |
| `--text-primary` | `#1A1917` | Fließtext |
| `--text-secondary` | `#5A554D` | Beschriftungen |
| `--text-muted` | `#6E6860` | Einheiten, Nebenangaben |

**Akzent und Fokus** — genau eine Akzentfarbe.

| Token | Hex | Verwendung |
|---|---|---|
| `--accent` | `#1D5B87` | primäre Aktion, Verweise, Auswahl |
| `--accent-strong` | `#164869` | Hover, aktiver Zustand |
| `--accent-text` | `#FFFFFF` | Text auf gefüllter Akzentfläche |
| `--focus-ring` | `#0F3E5C` | Fokusring, Kern |
| `--focus-ring-offset` | `#FFFFFF` | Fokusring, heller Absatz |

Der Fokusring ist **zweifarbig**, und das ist kein Stilmittel: Der dunkle Ring
allein erreicht auf der gefüllten Akzentfläche nur 1.52:1 und wäre auf einer
blauen Schaltfläche unsichtbar. Der helle Absatz löst das. Zeichne ihn so.

**Die sechs Zustände im Kalender-Grid**

| Zustand | Fläche | Rand | Text | Muster |
|---|---|---|---|---|
| frei | `#FFFFFF` | `#8C867B` | `#3A362F` | keines |
| belegt | `#E3EBF2` | `#1D5B87` | `#143349` | keines |
| Rüstzeit | `#EFECE4` | `#8C867B` | `#4A453B` | feine Diagonalschraffur 45° |
| Nachruhe | `#F3EDDC` | `#9A7B3A` | `#4B3D1C` | Punktraster 6 px |
| Abwesenheit | `#E8E6E1` | `#6E6860` | `#3D3A34` | breite Diagonalschraffur 135° |
| gesperrt | `#F7E7E6` | `#97302E` | `#6B1F1E` | Kreuzschraffur |

**Ergebnis der Regelprüfung**

| Ausgang | Fläche | Rand | Text |
|---|---|---|---|
| erfüllt | `#E8F0E9` | `#3F6E48` | `#24512F` |
| Warnung | `#FBF0D8` | `#9A7B3A` | `#6B4A05` |
| verletzt | `#F9E5E4` | `#97302E` | `#7A2220` |

**Schrift:** Source Sans 3 für Fließtext, IBM Plex Mono für Uhrzeiten und
Mengen. Der Mono-Satz ist keine Stilfrage — in einem Kalender müssen Ziffern
untereinander stehen, sonst springt die Spalte bei jedem Wechsel.

**Größen:** 11 px Legende · 12 px Grid- und Tabellenzellen · 14 px Fließtext
und Bedienelemente · 16 px Abschnittsüberschrift · 20 px Seitenüberschrift.
Abstände 4 / 8 / 12 / 16 / 24 / 32 px. Radien 3 px und 5 px.

**Grid-Maße, im Entwurf zu prüfen:** 15-Minuten-Raster bei 18 px Zeilenhöhe,
also 72 px je Stunde. Ein Praxistag von 07:00 bis 19:00 ergibt damit 864 px.
Sag mir, wenn das im Entwurf nicht aufgeht.

## Pflicht-Lieferbestandteil: das Tokenblatt

Zusätzlich zu den Screens brauche ich ein Artboard, das die Palette **zeigt**:

- die sechs Grid-Zustände als Musterzellen nebeneinander, einmal farbig und
  **einmal in Graustufen** — das ist die eigentliche Prüfung, ob die
  Unterscheidung ohne Farbe trägt
- die drei Prüfergebnisse als Musterzeilen, jeweils mit Symbol und Wort
- die Typo-Skala mit Größe, Schnitt und Zeilenhöhe
- den Fokusring auf hellem Grund **und** auf gefüllter Akzentfläche
- jede Farbe, die du über die Liste hinaus ergänzt hast, mit Name, Hex-Wert
  und dem Kontrastpaar, gegen das sie zu prüfen ist

## Fachbegriffe (nicht übersetzen)

| Begriff | Bedeutung |
|---|---|
| **Verordnung** | Ärztliches Rezept, Vordruck Muster 13. Legt Heilmittel, Anzahl der Einheiten, Frequenz und Diagnosegruppe fest. |
| **Heilmittel** | Die verordnete Therapieleistung, z. B. Manuelle Lymphdrainage. |
| **Behandlungseinheit** | Eine einzelne Therapiesitzung. |
| **Diagnosegruppe** | Kurzschlüssel des Heilmittelkatalogs, etwa WS, EX, LY. |
| **Frequenz** | Verordnete Behandlungshäufigkeit, z. B. „2× wöchentlich". |
| **Rüstzeit** | Vor- und Nachbereitung um eine Behandlung herum. |
| **Nachruhe** | Ruhephase nach Bädern, blockiert den Raum, nicht die Therapeutin. |
| **Behandlungsunterbrechung** | Pause zwischen zwei Terminen. Über 14 Kalendertage verfällt die Verordnung, sofern nicht begründet. |
| **Restkontingent** | Noch offene Behandlungseinheiten auf der Verordnung. |
| **Mandant** | Eine Praxis. Daten eines Mandanten sind für andere unsichtbar. |

## Stil, den ich nicht möchte

Verlaufshintergründe, Emoji als Symbole, Karten mit rundem Rahmen und farbigem
linkem Balken, große weiche Schatten, Glasmorphismus, sowie überflüssige
Kennzahlen-Kacheln. Symbole als Strich-SVG in einem einheitlichen Stil und
einer einheitlichen Strichstärke.

**Format:** Desktop 1440 × 900 px, plus das Tokenblatt als eigenes Artboard.
