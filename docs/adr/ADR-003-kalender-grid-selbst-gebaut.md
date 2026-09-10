# ADR-003: Das Kalender-Grid wird selbst gebaut, nicht zugekauft

- **Status:** angenommen
- **Datum:** 2026-09-10
- **Stufe:** 2

## Kontext

Das Kalender-Grid ist die fachliche Kernkomponente des Frontends: ein
Zeitgitter, Therapeutinnen als Spalten, Viertelstunden als Zeilen, sechs
Zustände auf engem Raum — frei, belegt, Rüstzeit, Nachruhe, Abwesenheit,
gesperrt —, bedient per Tastatur, im Stehen, zwischen zwei Patienten. Die
Mockup-Prompts beschreiben es bis auf die Zeilenhöhe.

Es ist zugleich die Komponente, an der dieses Projekt seinen stärksten
Anspruch festmacht. Der Skill `a11y-grid` sagt: WCAG steht in der
Ausschreibung als Nice-to-have, fast niemand kann es belegen, hier ist es
automatisiert. Roving Tabindex, ein zugänglicher Name je Zelle mit Zeit und
Zustand, eine Live-Region nach der Buchung, ein Fokusring, der auf farbiger
Fläche sichtbar bleibt, Zustände über Muster statt Farbe — das ist die Liste,
die die Kontrastprüfung aus Stufe 0 und der axe-Test aus Stufe 2 belegen
sollen.

ADR-006 hat die Vorentscheidung getroffen: Angular CDK, eigene Komponenten,
kein Komponenten-Set — und die Nummer 003 für genau diese Frage reserviert.
Sie wurde jetzt konkret, weil eine Bibliothek im Raum stand, die das
Zeitgitter mitbringt: Kendo UI for Angular mit seinem Scheduler.

## Optionen

**1. Kendo UI for Angular, Scheduler**

Die Option, die gut klingt, weil sie es ist. Der Scheduler bringt Tages- und
Wochenansicht, Ressourcen als Spalten, Drag-and-Drop, Tastaturnavigation und
ARIA-Auszeichnung fertig mit. Was hier zwei Wochen Handarbeit wäre, ist dort
ein Nachmittag Konfiguration, und das Ergebnis sieht vom ersten Tag an
professionell aus. Für ein Produkt, das schnell einen Kalender braucht, ist
das die richtige Wahl.

Drei Einwände. Erstens läge die Barrierefreiheit der schwierigsten Komponente
dann außerhalb dessen, was dieses Repo prüfen kann — ADR-006 hat Material und
PrimeNG genau deshalb verworfen, und bei Kendo wiegt der Einwand schwerer,
weil es die Kernkomponente selbst betrifft. Zweitens kämpfen die Vorgaben
aus den Mockups — Schraffur für Rüstzeit, Punktraster für Nachruhe, 18 Pixel
je Zeile, der zweifarbige Fokusring aus den Token — gegen ein fremdes DOM
statt mit einem eigenen. Drittens ist Kendo kommerziell: Ohne aktivierten
Schlüssel zeigt die Anwendung ein Banner, der Schlüssel müsste als Secret in
die CI, und ein Trial läuft ab. Für ein öffentliches Repo, dessen Argument
„alles hier ist nachprüfbar" lautet, ist eine Komponente, die ohne Schlüssel
nicht sauber läuft, ein Fremdkörper.

**2. Eine freie Kalenderbibliothek**

FullCalendar oder ähnliches über einen Angular-Wrapper. Kein Lizenzproblem,
breit eingesetzt. Aber dieselben zwei ersten Einwände wie bei Kendo, und
dazu ein dritter: Diese Bibliotheken sind für Ereignisse gebaut, nicht für
Ressourcenbelegung mit sechs Zuständen. Rüstzeit und Nachruhe als eigene,
unterscheidbare Zellzustände darzustellen hieße, gegen das Modell der
Bibliothek zu arbeiten.

**3. Selbst gebaut auf dem Angular CDK**

`FocusKeyManager` für den Roving Tabindex, `LiveAnnouncer` für die Meldung
nach der Buchung, `Overlay` und `FocusTrap` für den Buchungsdialog. Kein
vorgegebenes Aussehen, keine Lizenz, und jede Zeile Barrierefreiheit ist
eine, die wir geschrieben, getestet und mit axe geprüft haben. Der Preis ist
Handarbeit, und das Risiko, eine Zugänglichkeitsfrage schlechter zu lösen als
eine eingespielte Bibliothek.

## Entscheidung

**Option 3.** Das Grid ist ein eigenes Grid-Widget auf dem CDK, mit den Rollen
`grid`, `row`, `gridcell` und `columnheader`, genau einer Zelle mit
`tabindex="0"`, Pfeiltasten, Home, End, Eingabe und Escape. Jede Zelle hat
einen zugänglichen Namen, der Zeit und Zustand nennt. Die sechs Zustände
kommen aus den Token in `tokens.css`, einschließlich der Muster.

Eine Bibliothek gehört dorthin, wo ADR-006 sie schon hingestellt hat: ag-grid
für die Abrechnungsübersicht, weil die Ausschreibung ag-grid namentlich
nennt (S6). Kendo kommt nirgends vor.

Erkennbar im Repo: die Grid-Komponente unter `frontend/src/app/`, ein
Playwright-Test mit axe-core über das Grid als Merge-Bedingung, und der
Tastaturpfad — Tab hinein, Pfeile darin, Escape heraus — als eigener Test.

## Konsequenzen

**Positiv**

- Die WCAG-Zusage bleibt prüfbar. Was das Grid an Barrierefreiheit hat, steht
  im Repo, nicht in einem `node_modules`-Ordner.
- Die Mockup-Vorgaben lassen sich umsetzen, wie sie stehen. Kein Kampf gegen
  ein fremdes DOM.
- Kein Lizenzschlüssel, kein Banner, kein Secret in der CI.
- Das Portfolio zeigt die Fähigkeit, nach der die Ausschreibung fragt — ein
  barrierefreies Widget zu bauen —, statt der Fähigkeit, eines zu
  konfigurieren.

**Negativ**

- **Zwei Wochen Handarbeit statt eines Nachmittags.** Das ist die ehrliche
  Größenordnung, und sie ist der Preis dieser Entscheidung.
- **Das Risiko liegt bei uns.** Ein Fehler in der Tastaturnavigation ist
  unser Fehler, nicht Kendos. Gegenmittel: der axe-Test als Merge-Bedingung
  und der Subagent `a11y-auditor` für das, was axe nicht sieht.
- Drag-and-Drop, Mehrfachauswahl, Zoom — alles, was ein Scheduler mitbringt
  und hier fehlt, muss bei Bedarf einzeln gebaut werden. Für ein Show-Projekt
  ist das verschmerzbar; für ein Produkt wäre es eine Liste.

## Wann wir anders entscheiden würden

- **Wenn Kendo-Erfahrung selbst das Ziel wäre.** Eine Stelle, die Kendo
  namentlich fordert, kippt die Abwägung — dann ist der Scheduler der
  Beleg, nicht das Hindernis. Die Ausschreibungen, auf die dieses Projekt
  antwortet, nennen ag-grid und WCAG, nicht Kendo.
- **Wenn das Grid nicht die Kernkomponente wäre.** Ein Produkt, in dem der
  Kalender eine von zwanzig Ansichten ist, sollte ihn zukaufen. Hier ist er
  die eine Ansicht, an der alles hängt.
- **Wenn Drag-and-Drop und Serienbearbeitung zur Pflicht würden.** Ab einem
  gewissen Funktionsumfang ist das eigene Widget teurer als die
  Nachprüfung eines gekauften — und dann wäre Option 1 mit einem
  dokumentierten a11y-Audit je Version die ehrlichere Antwort.
- **Wenn Progress eine freie Lizenz ohne Aktivierung anböte.** Der dritte
  Einwand entfiele; die ersten beiden blieben.
