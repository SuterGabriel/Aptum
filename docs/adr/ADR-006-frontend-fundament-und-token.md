# ADR-006: Angular CDK mit eigenen Komponenten, Gestaltungstoken als CSS Custom Properties

- **Status:** angenommen
- **Datum:** 2026-09-09
- **Stufe:** 2 (vorgezogen, weil das Mockup eine Palette braucht)

## Kontext

Das Frontend existiert noch nicht. `frontend/` war leer, es gab keine
UI-Bibliothek, keine Komponenten und keine Farbtoken.

Die Entscheidung ist trotzdem fällig, und zwar aus zwei Richtungen. Erstens
braucht der Auftrag an die Gestaltung eine Grundlage: Wer ein Mockup bestellt,
ohne das Fundament zu kennen, bekommt Entwürfe, die sich nicht bauen lassen.
Zweitens hat sich dieses Repository bereits drei Zusagen gegeben, die den
Spielraum stark einschränken:

1. Der Skill `a11y-grid` verspricht, dass Kontrastwerte **aus den Farbtoken
   nachgerechnet** werden, nicht geschätzt. Das setzt eine maschinenlesbare
   Token-Datei voraus. Das ist keine Stilfrage mehr, sondern eine technische
   Anforderung.
2. ADR-003 ist für die Entscheidung reserviert, das Kalender-Grid selbst zu
   bauen statt eine Bibliothek zu nehmen. Die schwierigste Komponente ist
   damit ohnehin Handarbeit.
3. `docs/ANFORDERUNGEN.md` führt unter S6 ag-grid für die Abrechnungsübersicht.

Der fachliche Kern des Frontends ist ein Zeitgitter, das sechs Zustände auf
engem Raum unterscheidbar machen muss — frei, belegt, Rüstzeit, Nachruhe,
Abwesenheit, gesperrt — und das per Tastatur bedienbar sein muss. Das ist
keine Komponente, die ein Komponenten-Set mitbringt.

## Optionen

**1. Angular Material**
Googles Komponenten-Set auf Basis des Angular CDK. Barrierefreiheit ist
ordentlich, die Abdeckung breit, der Einstieg schnell. Zwei Einwände: Das
Material-Aussehen ist erkennbar und lässt sich nur mit erheblichem Aufwand
übertheimen — für ein Portfolio-Stück, das Gestaltungsarbeit belegen soll,
ist „sieht aus wie jede andere Material-App" ein Verlust. Und die eigentliche
Kernkomponente, das Zeitgitter, bringt Material nicht mit.

**2. PrimeNG**
Sehr großer Komponentenumfang, neutraleres Aussehen, in deutschen
Enterprise-Projekten verbreitet. Der Einwand ist gewichtiger als er klingt:
Die Barrierefreiheit schwankt je Komponente. Bei einem Projekt, das WCAG als
Alleinstellungsmerkmal führt und einen axe-Test zur Merge-Bedingung macht,
wäre jede zugekaufte Komponente ein Risiko, das man einzeln nachprüfen muss.

**3. Angular CDK plus eigene Komponenten**
Das CDK ohne Material: `FocusTrap` für den Dialog, `LiveAnnouncer` für die
Meldung nach der Buchung, `ListKeyManager` für den Roving Tabindex, `Overlay`
für Dialoge. Genau die Bausteine, die der Skill `a11y-grid` beschreibt — ohne
vorgegebenes Aussehen. Dafür gibt es keine geschenkte Datumsauswahl, keine
fertige Tabelle, keine Auswahlliste.

## Entscheidung

**Option 3.** Angular CDK als Fundament, Komponenten selbst gebaut, ag-grid
ausschließlich für die Abrechnungsübersicht aus S6.

Dazu die Token-Entscheidung:

- Gestaltungstoken sind **CSS Custom Properties** in genau einer Datei,
  `frontend/src/styles/tokens.css`. Kein Umweg über JSON mit
  Generierungsschritt: Ein kleines Skript kann CSS ohne Build-Werkzeug lesen,
  und mehr Maschinerie, als sich in zwei Sätzen erklären lässt, verstößt gegen
  Regel 4 in `CLAUDE.md`.
- Die **Kontrastanforderungen stehen als Kommentar in derselben Datei**, in
  der Form `/* @kontrast --text-secondary auf --surface-page >= 4.5 */`.
  `scripts/kontrast-check.mjs` liest sie und rechnet nach; der CI-Job
  `kontrast` führt das bei jedem Lauf aus.
- **Muster sind ebenfalls Token.** Rüstzeit, Nachruhe, Abwesenheit und
  gesperrte Slots unterscheiden sich zusätzlich durch benannte Schraffuren.
  Eine Schraffur, die nicht benannt ist, wird bei der dritten Komponente
  anders gezeichnet, und die Regel „nie Farbe allein" zerfällt still.
- **Benennung:** technische Struktur englisch, Zustände der Domäne deutsch —
  `--slot-frei-bg`, nicht `--slot-free-bg`. Dieselbe Trennung wie im
  Java-Code.

Die Palette wurde nicht geschätzt. Alle geforderten Paare sind nachgerechnet;
wie viele es sind und welche Werte sie erreichen, steht im Ausgabeprotokoll
des Prüfskripts — absichtlich nicht hier, damit die Zahl nicht veraltet.

## Konsequenzen

**Positiv**

- Die WCAG-Zusage ist maschinell geprüft statt behauptet. Wer eine Farbe
  ändert, die den Kontrast reißt, bekommt die CI rot — auch ein Agent.
- Das Aussehen ist frei gestaltbar. Das Mockup kann eine eigene visuelle
  Sprache entwickeln, statt Material-Vorgaben zu umgehen.
- Der zweifarbige Fokusring ist eine direkte Folge der Nachrechnung: Der
  dunkle Ring allein erreicht auf der gefüllten Akzentfläche nur 1.52:1 und
  wäre auf einer blauen Schaltfläche unsichtbar. Das fällt beim Rechnen auf,
  beim Hinschauen nicht.
- Keine zugekaufte Komponente, deren Barrierefreiheit einzeln nachzuweisen
  wäre.

**Negativ**

- Deutlich mehr Handarbeit bei Standardkomponenten. Datumsauswahl,
  Auswahllisten, Tabellen entstehen selbst.
- Das Risiko, eine Zugänglichkeitsfrage schlechter zu lösen als eine
  eingespielte Bibliothek, liegt jetzt bei uns. Gegenmittel: der axe-Test als
  Merge-Bedingung und der Subagent `a11y-auditor`.
- Die Token-Datei liegt unter `frontend/src/styles/`, bevor das
  Angular-Projekt existiert. Beim Anlegen des Projekts muss das Gerüst in ein
  leeres Verzeichnis erzeugt und die Datei danach zusammengeführt werden.
  Kleine, einmalige Reibung.
- Zwei Schriften kommen von Google Fonts. Ohne Netz oder in einer
  abgeschotteten Umgebung fällt die Ersatzschrift ein; die Fallback-Kette ist
  deshalb bewusst gesetzt.

## Wann wir anders entscheiden würden

- **Wenn das Frontend überwiegend aus Formularen und Tabellen bestünde.** Dann
  wäre der Eigenbau reiner Aufwand, und Material oder PrimeNG wären richtig.
  Hier trägt eine einzige, nicht zukaufbare Komponente — das Zeitgitter — den
  fachlichen Kern.
- **Wenn ein Team mit gemischtem Kenntnisstand daran arbeitete.** Ein
  Komponenten-Set setzt einen Qualitätsboden, den selbst gebaute Komponenten
  erst durch Disziplin erreichen.
- **Wenn ein Unternehmens-Designsystem vorgegeben wäre.** Dann käme die
  Palette von dort, und diese ADR beschränkte sich auf die Frage, wie deren
  Token in die Kontrastprüfung gelangen.
- **Wenn die Token über mehrere Ziele hinweg gebraucht würden** — Web, native
  App, Druck. Dann lohnte der Umweg über eine neutrale Quelle mit
  Generierungsschritt, den wir hier bewusst vermeiden.
