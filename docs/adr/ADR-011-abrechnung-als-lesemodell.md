# ADR-011: Die Abrechnung ist ein Lesemodell im Scheduling-Dienst, kein dritter Dienst

- **Status:** angenommen
- **Datum:** 2026-09-12
- **Stufe:** 5

## Kontext

`services/billing/` existiert seit Stufe 0 — als leerer Ordner mit einer
`.gitkeep`. Der Plan sah drei Dienste vor: Scheduling, AI-Assist, Billing.
Die Ausschreibung nennt „Heilmittel, Kalender und Abrechnung" als Bereiche
des Frontends und ag-grid als Werkzeug; `PRODUKT.md` führt die
Abrechnungsübersicht als Stufe 6 und begrenzt sie ausdrücklich: eine
*lesende* Sicht auf bereits korrekte Daten, keine Kassenabrechnung nach
§ 302 SGB V.

Als die Übersicht gebaut werden sollte, stand die Frage, wo sie hingehört.
Ein leerer Ordner ist dabei ein schlechter Ratgeber: Er drängt dazu, ihn zu
füllen, damit der Plan aufgeht — auch wenn die Fachlichkeit, die er ankündigt,
nicht existiert.

Die Frage der Abrechnung — *welche Einheiten sind erbracht und
absetzungssicher?* — ist dieselbe Frage, die jede Buchung schon beantwortet
hat, nur zu einem anderen Zeitpunkt. Das ist die These aus `PRODUKT.md`,
Abschnitt 1, und sie hat eine Konsequenz für den Schnitt: Wer „prüffest"
entscheidet, braucht die Regeln.

## Optionen

**A. Ein dritter Dienst, wie geplant.** Eigener Prozess, eigenes Bild,
eigene Datenbankverbindung, eigener OpenAPI-Vertrag. Architektonisch die
sauberste Trennung, und drei Dienste belegen SA7 sichtbarer als zwei. Der
echte Vorteil: Wenn eines Tages ein § 302-Datenaustausch oder eine
Absetzungslogik dazukommt, ist der Platz schon da.

Der Preis: Der Dienst müsste entweder die Regelklassen kopieren (zwei
Meinungen darüber, wann eine Verordnung verfallen ist — die Abrechnung hätte
die falsche) oder sie über die Schnittstelle des Scheduling-Dienstes
befragen (ein Dienst, der für jede Zeile einen anderen Dienst fragt, ist kein
Dienst, sondern ein Umweg). Beides für eine Sicht, die nichts schreibt.

**B. Ein Lesemodell im Scheduling-Dienst.** Eine reine Funktion in der
Domäne (`domain/abrechnung/`), die je Verordnungsakte eine Zeile bildet und
das bestehende `Regelwerk` über das Erbrachte laufen lässt. Ein Anwendungsfall,
ein Endpunkt, ein Port-Methode `alle()`. Die Regeln werden nicht kopiert,
sondern verwendet. Der Preis: Der Scheduling-Dienst bekommt eine zweite
Aufgabe, und ein Fremder, der `services/` öffnet, sieht zwei Dienste, wo drei
angekündigt waren.

**C. Die Tabelle im Frontend rechnen lassen.** Verordnungen laden, im Browser
zählen und Fristen prüfen. Am schnellsten gebaut. Verstößt gegen Regel 3 in
`CLAUDE.md` in ihrer allgemeinen Form: Die Domäne entscheidet, nicht ein
zweiter Regelsatz in TypeScript.

## Entscheidung

**Option B.** Erkennbar an `services/scheduling/domain/.../abrechnung/`,
`AbrechnungAnzeigen` in der Anwendungsschicht, `GET /abrechnung/uebersicht`
und der Methode `Regelwerk.pruefeErbrachtes` — der einzigen Stelle, die
entscheidet, was prüffest ist.

`services/billing/` bleibt, mit einer `README.md` statt der `.gitkeep`, die
diese Entscheidung nennt und die Bedingung, unter der der Ordner Code
bekommt. Ein leerer Ordner sieht wie eine Lücke aus; ein Ordner mit einer
begründeten Entscheidung ist eine.

Im Frontend liegt die Übersicht als lazy geladene Route: ag-grid wiegt mehr
als der Rest der Anwendung zusammen, und wer Termine sucht, soll es nicht
laden. Registriert sind nur die Module, die die Seite braucht.

## Konsequenzen

- Die Abrechnung nennt Regeln beim selben Namen wie der Buchungsdialog, mit
  derselben Begründung — weil es derselbe Code ist. Eine Regeländerung ändert
  beide Sichten, ohne dass jemand daran denken muss.
- `Regelwerk` hat jetzt drei Einstiege: Verordnung mit Kandidat, Ressourcen
  mit Kandidat, Erbrachtes ohne Kandidat. Der dritte lässt Restkontingent
  und Frequenz aus — mit Begründung im Code — und prüft die Gültigkeit am Tag
  der letzten Behandlung, nicht heute. Das ist eine fachliche Auslegung, die
  hier steht, damit sie diskutierbar ist.
- Der Port `VerordnungRepository` bekommt `alle()`. Für eine Praxis sind das
  Hunderte Akten; die Grenze, ab der Sortieren und Filtern in die Datenbank
  gehörte, nennt ADR-001, und bis dahin tut es die Tabelle.
- Negativ: SA7 („Umsetzung und Optimierung von Microservices") wird mit zwei
  Diensten belegt, nicht drei. Das Mapping sagt das so.
- Negativ: Der Scheduling-Dienst ist damit der Dienst für „alles, was die
  Regeln braucht". Kommt echte Abrechnungslogik dazu, wird er der falsche Ort
  — siehe unten.

## Wann wir anders entscheiden würden

- **Wenn Abrechnung schreibt.** Sobald eine Verordnung als abgerechnet
  markiert wird, ein Datenträgeraustausch nach § 302 erzeugt oder eine
  Absetzung verbucht wird, ist Abrechnung ein eigener Regelkreis mit eigenem
  Zustand. Dann zieht `domain/abrechnung/` in `services/billing/` um; der
  Endpunkt und die Tabelle bleiben, nur die Adresse hinter dem Reverse Proxy
  ändert sich.
- **Wenn die Regeln als Bibliothek geteilt werden könnten.** Ein eigenes
  Modul `regeln`, das beide Dienste einbinden — dann wäre Option A ohne
  Kopie möglich. Heute gibt es dafür keinen zweiten Abnehmer.
- **Wenn die Übersicht langsam wird.** `alle()` lädt jede Akte samt Verlauf.
  Bei zehntausend Verordnungen je Mandant gehörte die Vorauswahl in eine
  Abfrage mit Seitengrenzen, und ag-grid bekäme ein serverseitiges Zeilenmodell.
