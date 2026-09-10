# ADR-009: Verletzte Regeln blockieren, Übersteuerung nur mit Begründung

- **Status:** angenommen
- **Datum:** 2026-09-10
- **Stufe:** 1

> **Zur Verspätung:** Die Produktsicht hat diese Frage am 09.09. als offen
> markiert, mit dem Zusatz „gehört in eine ADR, sobald die erste Regel gebaut
> wird". Die erste Regel entstand am 10.09., die elfte auch — und der
> Prüfbericht hat die Entscheidung stillschweigend getroffen, bevor sie
> geschrieben war. Das ist der zweite Fall dieser Art nach ADR-008. Er steht
> hier, damit er nicht der dritte wird.

## Kontext

Die Regeln des Domänenkerns liefern drei Ausgänge: erfüllt, Warnung,
verletzt. Was aus „verletzt" folgt, war nicht entschieden. Die Produktsicht
formuliert die Spannung: Das System soll den Fehler zum Zeitpunkt der Buchung
verhindern, weil ein Hinweis in einer Auswertung zu spät kommt — aber es gibt
legitime Fälle, in denen die Praxis bewusst gegen eine Regel bucht. Der
Patient kann nur heute, der zugelassene Raum ist defekt, die verordnende
Person hat am Telefon zugestimmt und das Fax kommt morgen.

Das Wireframe des Buchungsdialogs zeigt die Antwort bereits: eine verletzte
Regel blockiert, daneben ein Textfeld, das ausgefüllt sein muss, bevor die
Schaltfläche „Übersteuern mit Begründung" aktiv wird, mit dem Hinweis, dass
die Begründung protokolliert wird. Was fehlte, war die Entscheidung im Modell
und ihre Begründung.

## Optionen

**1. Nur warnen, nie blockieren**

Die Praxis kennt ihre Fälle besser als das System. Jede Regelverletzung wird
angezeigt, die Buchung geht trotzdem durch, die Warnung bleibt am Termin. Der
Vorteil ist echt: kein Arbeitsfluss wird unterbrochen, keine Übersteuerung
muss begründet werden, und ein System, das nie im Weg steht, wird benutzt.
Der Einwand: Eine Warnung, die man immer wegklicken kann, wird immer
weggeklickt. Nach drei Wochen sind alle Termine gelb, und die Absetzung kommt
trotzdem — genau der Zustand, den das Produkt beseitigen soll.

**2. Blockieren, ohne Ausnahme**

Eine verletzte Regel ist eine verletzte Regel. Wer trotzdem buchen will,
ändert die Daten, bis sie stimmen. Konsequent, einfach, keine
Protokollierung nötig. Der Einwand: Es gibt die legitimen Fälle, und ein
System, das sie nicht zulässt, wird umgangen — der Termin landet im
Papierkalender, und das System weiß nichts davon. Schlimmer als Option 1.

**3. Blockieren, Übersteuerung mit Begründungspflicht**

Die verletzte Regel blockiert. Wer trotzdem bucht, gibt eine Begründung an,
und die wird mit Name und Zeitpunkt am Termin gespeichert. Die Hürde ist
klein genug für den legitimen Fall und groß genug, dass sie nicht zur
Gewohnheit wird.

## Entscheidung

**Option 3.** Im Modell:

- `Pruefbericht.blockiert()` ist wahr, sobald eine Regel verletzt ist.
  Warnungen blockieren nicht.
- `Buchungsentscheidung` hat drei Ausgänge: frei, blockiert, übersteuert.
- `Uebersteuerung` trägt Begründung und Urheber; beides ist Pflicht, leer ist
  keine Übersteuerung.
- Eine Übersteuerung ohne verletzte Regel wird nicht gespeichert. Sie hätte
  nichts übersteuert.
- **Jede Regel ist übersteuerbar.** Es gibt keine Klasse von Regeln, die es
  nicht ist — noch nicht. Siehe unten.

## Konsequenzen

**Positiv**

- Der Fehler wird im Moment der Buchung verhindert, nicht hinterher
  berichtet. Das ist die Produktentscheidung aus `PRODUKT.md`, jetzt im Code.
- Die Begründung ist der Text, den die Praxis später gegenüber der Kasse
  vorlegt. Sie entsteht dort, wo das Wissen ist — bei der Buchung — und nicht
  Monate später aus der Erinnerung.
- Warnung und Verstoß bleiben verschieden. Die Frequenzabweichung, über die
  ein Mensch nach Rücksprache entscheidet, braucht keine Übersteuerung; sie
  war nie ein Verstoß.

**Negativ**

- **Jede Regel ist übersteuerbar, auch die, bei denen das fachlich fragwürdig
  ist.** Eine fehlende Abrechnungserlaubnis lässt sich nicht mit einer
  Begründung heilen — die Behandlung ist nicht abrechenbar, egal was im
  Protokoll steht. Eine Unterscheidung zwischen übersteuerbaren und harten
  Regeln wäre richtiger, ist aber nicht getroffen: Sie braucht die
  Erfahrung, welche Regeln in der Praxis übersteuert werden, und die gibt es
  nicht. Das ist die bekannte Lücke dieser Entscheidung.
- Das Protokoll muss gelesen werden. Eine Übersteuerung, die niemand
  auswertet, ist eine Warnung mit mehr Tipparbeit.
- Die Hürde ist klein. Wer jeden Tag dieselbe Begründung eintippt, hat
  Option 1 mit Umweg.

## Wann wir anders entscheiden würden

- **Wenn sich zeigte, dass bestimmte Regeln nie legitim übersteuert werden.**
  Dann bekämen sie einen eigenen Ausgang, der auch mit Begründung nicht
  buchbar ist. Die Qualifikationsregel ist die erste Kandidatin.
- **Wenn sich zeigte, dass fast alles übersteuert wird.** Dann sind die
  Regeln zu streng oder die Praxis nutzt das System nicht so, wie es gedacht
  ist — und Option 1 wäre ehrlicher als ein Protokoll voller Floskeln.
- **Wenn das Protokoll niemanden erreicht.** Ohne Auswertung ist Option 3
  teurer als Option 1 und nicht sicherer. Dann entweder die Auswertung bauen
  oder die Pflicht streichen.
