# billing

Dieser Ordner ist leer, und das ist eine Entscheidung, keine Lücke (ADR-011).

Die Abrechnungsübersicht — welche Einheiten sind erbracht und prüffest — ist
heute ein Lesemodell im Scheduling-Dienst:
`services/scheduling/domain/.../abrechnung/`, Endpunkt
`GET /abrechnung/uebersicht`, Tabelle unter `/abrechnung` im Frontend. Sie
liegt dort, weil „prüffest" dieselben Regeln braucht, die jede Buchung
geprüft haben, und Regeln nicht kopiert werden.

Ein eigener Dienst entsteht hier, sobald Abrechnung *schreibt*: eine
Verordnung als abgerechnet markieren, einen Datenträgeraustausch nach § 302
SGB V erzeugen, eine Absetzung verbuchen. Das ist ein eigener Regelkreis mit
eigenem Zustand — und heute bewusst nicht gebaut (`docs/PRODUKT.md`,
Abschnitt 5).
