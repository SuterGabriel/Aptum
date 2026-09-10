# ADR-007: Fachliche Zahlen tragen eine Fundstellen-ID

- **Status:** angenommen
- **Datum:** 2026-09-10
- **Stufe:** 0 (vorgezogen, weil die Regel vor der ersten Regelklasse stehen muss)

## Kontext

Der wichtigste Befund aus Stufe 0 steht im
[Entwicklungslog](../ENTWICKLUNGSLOG.md): Ein Agent wollte an mehreren Stellen
konkrete Fristen einsetzen, bevor die Quellenrecherche zurück war. Plausible
Zahlen, keine Quelle. Der Satz dazu lautet: *Falscher Code fällt im Test auf,
falsche Fachlogik nicht.*

Das ist keine Übertreibung. Ein Test, der prüft, ob 28 Tage korrekt gerechnet
werden, ist grün — auch wenn die 28 erfunden ist. Die gesamte Testpyramide
dieses Projekts steht auf Zahlen, die aus einer Richtlinie stammen müssen, und
kein Test kann das nachprüfen.

Der Skill `heilmittel-domain` zieht daraus bereits eine Regel: *Wenn eine Zahl
nicht in `regeln.md` mit Quelle steht, wird sie nicht in den Code geschrieben.*
Bisher ist das eine Absichtserklärung. Sie wird von genau der Instanz befolgt
oder nicht befolgt, deren Fehler sie verhindern soll.

Erschwerend kommt die Struktur der Quellen dazu. Von 65 recherchierten Regeln
sind vier `UNSICHER` — die Qualifikationsstunden für Manuelle Lymphdrainage
stehen in zwei Anlagen mit verschiedenen Zahlen, für die verbreitete
24-Stunden-Ausfallfrist gibt es überhaupt keine sozialrechtliche Grundlage. Für
diese vier gilt eine andere Regel als für die übrigen 61: Sie werden
parametrisiert, nicht als Konstante geschrieben. Auch das steht bisher nur in
Prosa.

Die Entscheidung ist jetzt fällig, weil sie die Form jeder Regelklasse
bestimmt. Nachträglich eingezogen prüft sie nur noch, was ohnehin schon
dasteht.

## Optionen

**1. Freitext-Kommentar mit dem Paragraphen**

```java
// § 15 Abs. 1 S. 1 HeilM-RL: Behandlungsbeginn innerhalb 28 Kalendertagen
private static final int BEGINN_FRIST_TAGE = 28;
```

Der naheliegende Weg, und der Vorteil ist echt: Die Quelle steht vollständig
da, in der Sprache der Richtlinie, ohne Umweg über eine zweite Kennung. Wer den
Code liest, kann den Paragraphen direkt nachschlagen. Es gibt nichts zu
pflegen, nichts zu synchronisieren, und die Regel ist in null Sätzen erklärt.

Der Einwand ist, dass eine Maschine damit nichts anfangen kann. Ob `§ 15
Abs. 1` existiert, ob er das sagt, ob der Kommentar noch zur Zahl darunter
passt — alles unprüfbar. Ein Kommentar, der nach einer Wertänderung stehen
bleibt, ist schlechter als keiner, weil er Sicherheit vortäuscht. Und genau
diese Sorte Fehler ist hier der teuerste.

**2. Fundstellen-ID gegen einen Katalog**

Jede Zeile in `regeln.md` bekommt eine stabile ID, jede fachliche Konstante
nennt sie. Ein Skript prüft, dass die ID existiert, dass ihr Status `BELEGT`
ist, und dass in einer Regelklasse keine nackte Zahl steht.

**3. Die Zahlen ganz aus dem Code nehmen**

`regeln.md` wird zur Datenquelle — YAML oder JSON —, die Werte werden geladen
oder der Code wird daraus erzeugt. Der Vorteil ist der größte von allen: Es
gibt die Zahl nur einmal. Ein neuer Rechtsstand ist eine Datenänderung, keine
Codeänderung, und die Frage nach der Abweichung zwischen Tabelle und Code kann
gar nicht entstehen.

Zwei Einwände. Erstens verschwindet die Regel aus dem Code — was `pruefe()`
tut, steht dann in einer Datei, die beim Lesen der Klasse nicht offen ist.
Zweitens ist es Maschinerie: ein Ladepfad, ein Schema, eine Fehlerbehandlung
für den Fall, dass ein Wert fehlt, und ein Generierungsschritt oder eine
Laufzeitabhängigkeit. Regel 4 in [CLAUDE.md](../../CLAUDE.md) sagt, was dann
passiert.

## Entscheidung

**Option 2.** Die Form ist:

```java
/** @fundstelle HM-FRIST-01 */
private static final int BEGINN_FRIST_TAGE = 28;
```

- IDs haben die Form `HM-<BEREICH>-<NN>` und stehen in der ersten Spalte jeder
  Regeltabelle in
  [`regeln.md`](../../.claude/skills/heilmittel-domain/regeln.md).
- **IDs werden nie neu vergeben.** Ändert sich ein Wert durch einen neuen
  Rechtsstand, bleibt die ID und der Wert wird ersetzt. Entfällt eine Regel,
  bleibt die Zeile mit Status `OFFEN` stehen.
- In einer Regelklasse unter `domain/regel/` steht **keine nackte Zahl**. Jede
  fachliche Zahl ist eine benannte Konstante mit Fundstelle darüber.
- Nur Status `BELEGT` darf als Konstante in den Code. `UNSICHER` und `OFFEN`
  werden zu Parametern je Praxis — die Ausfallregel ist das Vorbild.
- Ausgenommen sind die Zahlen 0, 1 und 2, weil sie in jedem Code vorkommen und
  praktisch nie fachlich sind. Der Kompromiss ist benannt, siehe unten.

Geprüft von `scripts/regel-check.mjs` im Hook vor dem Commit und in der CI.

## Konsequenzen

**Positiv**

- Die Zusage des Skills ist maschinell geprüft statt behauptet. Ein Agent, der
  eine plausible Frist einsetzt, bekommt die CI rot zurück — genau der Fehler,
  der in Stufe 0 aufgetreten ist.
- Die Trennung zwischen `BELEGT` und `UNSICHER` wirkt im Code. Bisher hätte
  niemand bemerkt, wenn die 24-Stunden-Ausfallfrist doch als Konstante
  gelandet wäre.
- Der Katalog wird auch ohne Java geprüft: 65 Zeilen, eindeutige IDs, gültige
  Status. Das Gate ist ab heute wirksam, nicht erst ab Stufe 1.
- Die Grenzfallliste bekommt eine Adresse. Ein Test kann die ID im Namen
  tragen, und damit ist die Kette Quelle → Regel → Test durchgehend benannt.

**Negativ**

- **Die Zahl steht weiterhin zweimal da**, in der Tabelle und im Code, und der
  Check vergleicht sie nur grob. Er prüft, dass die ID existiert und belegt
  ist und dass die Zahl in der Zeile vorkommt — nicht, *welche* der Zahlen
  einer Zeile gemeint ist. Bei `SB1, SB2, EN1, EN2, PS4: 10 · PS2, PS3: 20`
  deckt die Zeile eine 10 und eine 20 gleichermaßen. Der Zahlendreher und der
  Verweis auf die falsche Zeile fallen auf, die Verwechslung innerhalb einer
  Zeile nicht. Näher kommt man nicht heran, solange die Werte nur in Prosa
  stehen.

  *Nachtrag vom 2026-09-10:* Die Fassung bei Annahme kannte den Wertabgleich
  noch nicht; er kam, nachdem eine Sonde gezeigt hatte, dass eine 82 mit der
  Fundstelle „28 Kalendertage" durchging. Die Formulierung oben ist die
  aktuelle.
- **Die ID ist eine zweite Sprache neben dem Paragraphen.** Wer `§ 15` sucht,
  findet `HM-FRIST-01` nicht. Gegenmittel ist allein, dass die Fundstelle in
  derselben Tabellenzeile steht.
- **Die Ausnahme für 0, 1 und 2 ist eine Lücke.** Eine fachliche Zwei — „2×
  wöchentlich" — rutscht durch. Ohne die Ausnahme meldet das Gate jede
  Schleife, und ein Gate mit Fehlmeldungen wird abgeschaltet. Der Tausch ist
  bewusst.
- Regelklassen werden geschwätziger. Drei Zeilen für eine Konstante, wo eine
  gereicht hätte.

## Wann wir anders entscheiden würden

- **Wenn die Werte häufig wechselten.** Bei jährlich angepassten Sätzen wäre
  jede Anpassung eine Codeänderung mit Deployment. Dann Option 3: Werte in
  Konfiguration, Code liest sie.
- **Wenn das Team die Domäne im Kopf hätte.** Der Katalog löst ein
  Wissensproblem. Wer die Richtlinie kennt, sieht einer erfundenen Frist an,
  dass sie erfunden ist, und der Freitext-Kommentar aus Option 1 genügt.
- **Wenn es nur eine Handvoll Zahlen gäbe.** Bei fünf Konstanten ist ein
  Katalog mit Prüfskript mehr Aufwand als Nutzen. Bei 65 kippt es.
- **Wenn die Regeln keine Primärquelle hätten.** Interne Geschäftsregeln ohne
  Gesetzestext haben keine Fundstelle, auf die man verweisen könnte. Dann
  bliebe nur, die Entscheidung selbst zu dokumentieren — also eine ADR statt
  einer Katalogzeile.
- **Wenn der Abgleich zwischen Tabelle und Code nachgerüstet würde** und sich
  dabei zeigt, dass er nur mit maschinenlesbaren Werten geht. Dann wäre Option
  3 nachträglich doch die richtige, weil sie das Problem an der Wurzel löst,
  das hier offen bleibt.
