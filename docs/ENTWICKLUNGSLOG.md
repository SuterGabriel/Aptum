# Entwicklungslog

Beobachtungen zum Einsatz von KI-Agenten in diesem Projekt. Ehrlich, auch wo es
nicht gut aussieht.

Der Grund für dieses Dokument: consultingheads führt "nachweisbarer, effektiver
Einsatz von KI in der Softwareentwicklung" als Must-have. Ein Vermittler bekommt
zwanzig Profile, die das behaupten. Eine konkrete Beobachtung, wo Agenten
getragen haben und wo nicht, ist glaubwürdiger als jede Produktivitätszahl.

Regel für dieses Log: Einträge entstehen am selben Tag. Rückwirkend geschriebene
Beobachtungen sind Erinnerungen, und Erinnerungen bevorzugen die Fälle, die gut
ausgingen.

---

## 2026-09-09 — Stufe 0, Setup

**Was delegiert wurde:** Das gesamte Gerüst. Ordnerstruktur,
Anforderungs-Mapping aus den beiden Ausschreibungstexten, ADR-001, sechs Skills,
fünf Commands, zwei Subagents, der Beleg-Check und die CI. Zusätzlich als
eigener Auftrag an einen Subagenten die Quellenrecherche zur
Heilmittel-Richtlinie.

**Was gut lief.** Das Anforderungs-Mapping. Zwei Ausschreibungstexte in eine
Tabelle mit Status und Belegspalte zu überführen ist genau die Sorte Arbeit, die
von Hand eine Stunde dauert und langweilig genug ist, dass man sie verschiebt.
Ebenso die Skills: Sie bestehen aus Regeln, die ich formulieren kann, in einer
Struktur, die ich nicht jedes Mal neu erfinden will.

**Die Quellenrecherche war der klare Gewinn.** Ein Subagent hat die
Primärdokumente geladen und im Volltext ausgewertet: Heilmittel-Richtlinie,
Heilmittelkatalog, die Verträge nach § 125 SGB V für Physio- und Ergotherapie
samt Anlagen. Ergebnis sind rund siebzig belegte Regelzeilen mit Fundstelle und
Rechtsstand.

Wertvoller als die belegten Zeilen sind aber die sechs Widersprüche, die dabei
auffielen. Zwei Beispiele:

- Die Qualifikation für Manuelle Lymphdrainage steht in Anlage 1 mit 170
  Stunden, in der neuen Anlage 7 mit 140 Unterrichtseinheiten. Eine Zahl hätte
  man aus jeder einzelnen Quelle mit voller Überzeugung übernommen.
- Für das verbreitete Ausfallhonorar mit 24-Stunden-Frist gibt es überhaupt
  keine sozialrechtliche Grundlage. Es ist eine zivilrechtliche Vereinbarung.
  Ein Modell, das die 24 Stunden fest verdrahtet, wäre fachlich falsch.

Beide Erkenntnisse haben das Domänenmodell verändert, bevor die erste Zeile Code
existiert: Ausfallfrist und Rüstzeit werden Parameter je Praxis, keine
Konstanten.

**Was auffiel.** Der Agent wollte an mehreren Stellen konkrete Fristen
einsetzen, bevor die Recherche zurück war. Plausible Zahlen, keine Quelle. Genau
der Fehler, vor dem der Skill `heilmittel-domain` warnt, und er trat schon beim
Anlegen dieses Skills auf.

Gegenmaßnahme: `regeln.md` startete leer mit Status OFFEN, und die Regel "keine
Zahl ohne Quelle" steht ganz oben im Skill. Erst nach der Recherche wurden
Zahlen eingetragen, jede mit Fundstelle und Status.

Das ist der wichtigste Befund aus Stufe 0. Falscher Code fällt im Test auf,
falsche Fachlogik nicht.

**Was nicht funktionierte.** Zwei handwerkliche Fehler beim Schreiben der
Dateien:

- Mehrere Dateien entstanden mit Umschreibungen wie `fuer` statt "für". Die
  Korrektur per Wörterbuch ersetzte anschließend einen Java-Methodennamen mit,
  aus `pruefe` wurde `prüfe`. Gefunden beim Nachlesen, nicht durch eine Prüfung.
  Wäre es Produktivcode gewesen, hätte der Compiler es gefangen. In einer
  Markdown-Datei fängt es niemand.
- Eine Datei fiel bei der ersten Korrektur durchs Raster, weil die Dateiliste
  von Hand zusammengestellt war statt aus dem Verzeichnisbaum.

Beides sind Fehler derselben Art: Eine Massenoperation ohne Prüfung des
Ergebnisses. Genau der Fall, für den der Beleg-Check gedacht ist.

**Offen:** Java ist auf dieser Maschine noch nicht installiert. Vor Stufe 1
nachzuholen: ein JDK 21 und die Entscheidung zwischen Gradle und Maven.

---

## 2026-09-09 — Stufe 0, Nachtrag: eine Lücke im Beleg-Check

**Was delegiert wurde:** Eine Durchsicht des eigenen Stands vor dem Start von
Stufe 1. Nicht „schreib etwas", sondern „prüf, ob das, was dasteht, stimmt".

**Was die Prüfung abgefangen hat.** `docs/PRODUKT.md` war an drei Stellen in
[ANFORDERUNGEN.md](ANFORDERUNGEN.md) als Beleg eingetragen (SA8, C11, CA5) und
existierte nicht. Zwei dieser drei Stellen stehen in den Aufgaben-Tabellen, die
keine Statusspalte haben — was dort steht, ist also eine unbedingte Behauptung
über den Ist-Stand, nicht ein Ziel.

Der Beleg-Check hat das nicht gefangen, obwohl er genau dafür gebaut wurde. Er
prüfte die Existenz der Mapping-Datei, aber nicht, ob die Pfade *in* der
Belegspalte auf etwas zeigen. Der Guardrail hatte also eine Lücke an der Stelle,
an der er am meisten behauptet.

**Was daran lehrreich ist.** Der erste Reflex war, jeden Pfad in der Belegspalte
zu prüfen. Das wäre falsch gewesen: Bei Zeilen mit Status `offen` ist die
Belegspalte laut eigener Legende ein Ziel, kein Beleg — `docs/AI-PIPELINE.md`
(C9) darf dort stehen, ohne zu existieren. Eine Prüfung, die solche Zeilen rot
macht, erzieht dazu, Ziele aus dem Dokument zu entfernen. Sie hätte das Repo
also ärmer gemacht, nicht ehrlicher.

Die Prüfung unterscheidet jetzt: unbedingte Behauptungen müssen existieren,
Zeilen mit `offen`, `zu klären`, `teilweise belegbar` oder `nicht belegbar`
dürfen nach vorn zeigen.

**Was nicht funktionierte.** Der erste Versuch, `PRODUKT.md` über ein
Shell-Heredoc zu schreiben, brach an einem Zitatzeichen ab. Kein Schaden, aber
ein Muster: Fließtext mit Sonderzeichen gehört nicht durch eine Shell.

**Gegenprobe.** Die neue Prüfung wurde einmal absichtlich rot gemacht — Datei
weg, Exit-Code 1, zwei Zeilen `FEHLT`, Datei zurück, wieder grün. Ein Guardrail,
von dem niemand gesehen hat, wie er ausschlägt, ist eine Behauptung wie jede
andere.

---

## 2026-09-10 — Stufe 0, Nachtrag: die erste Prüfstufe auf der Maschine

**Was delegiert wurde:** Ein Vergleich der eigenen Pipeline mit der eines
anderen Projekts, das drei Stufen hat — Git-Hooks, CI, Deploy. Danach der Bau
dessen, was hier fehlte: die erste Stufe. Vier Gates laufen jetzt vor dem
Commit, zwei davon sind neu.

**Was der Vergleich ergeben hat.** Aptum hatte nur die mittlere Stufe. Jede
Prüfung lief erst auf GitHub, also nach dem Commit. Das ist die unbequemere
Reihenfolge: Was rot wird, ist bereits Geschichte.

Der zweite Befund war unangenehmer. `.claude/hooks/` stand in
[ANFORDERUNGEN.md](ANFORDERUNGEN.md) unter C8 in der Belegspalte und enthielt
eine `.gitkeep`. Der Beleg-Check ließ das durch, weil er Existenz prüft und ein
Verzeichnis existiert. Dasselbe Muster wie bei `PRODUKT.md` im Eintrag darüber,
nur eine Ebene tiefer: nicht ein fehlender Pfad, sondern ein leerer.

**Was die neuen Gates auf dem ersten Lauf abgefangen haben.** Der wertvollste
Abschnitt, und diesmal ist er nicht leer:

- `scripts/beleg-check.sh:9`, in einem Kommentar: „eine Behauptung, die niemand
  `prueft`". Ausgerechnet in der Datei, die Behauptungen prüft.
- `.gitignore:37`: „hier landet nie ein `Schluessel`".
- `docs/ENTWICKLUNGSLOG.md:66` — diese Datei. Die Zeile, die den Vorfall vom
  Vortag beschreibt, zitierte `fuer` in geraden Anführungszeichen statt in
  Backticks. Der Text, der den Fehler dokumentiert, enthielt ihn dadurch
  selbst. Die Zeile darunter macht es bei `pruefe` richtig.

Drei Stellen, alle aus derselben Woche, alle beim Nachlesen übersehen. Genau
die Fehlerklasse, die der Eintrag vom Vortag beschreibt.

**Was am Zuschnitt gelernt wurde.** Der erste Entwurf des Prosa-Checks arbeitete
mit einer Wortliste. Das war der falsche Schnitt: Die Liste veraltet, und sie
sagt nichts darüber, was in *diesem* Repo richtig ist. Die Fassung, die blieb,
leitet die verbotenen Formen aus dem Repo selbst ab — jedes Wort, das irgendwo
mit Umlaut steht, darf nirgends in Umschrift auftauchen.

Wichtiger war die zweite Frage: Wie unterscheidet die Prüfung `fuer` von
`pruefe()`? Beides ist ASCII, beides steht in einer Datei. Ein Wörterbuch kann
das nicht, und genau daran ist die Korrektur am Vortag gescheitert. Die Antwort
ist der Ort statt des Wortes: In Markdown wird alles außer Code geprüft, in
allen anderen Dateien nur reine Kommentarzeilen. Auf einer Kommentarzeile kann
kein Bezeichner stehen. Damit gehen `pruefe()`, `--hitflaeche-min` und
`Pruefergebnis` aus dem Glossar durch, ohne dass eine Ausnahmeliste sie einzeln
freistellen muss.

**Was nicht funktionierte.** Der Prosa-Check meldete beim ersten Lauf zwei
Treffer in seinem eigenen Kopfkommentar, weil der `fuer` als Beispiel zitiert.
Kein Fehler des Skripts — dieselbe Korrektur wie im Fließtext: Zitiertes gehört
in Backticks. Amüsant, aber auch der Beleg, dass die Regel keine Ausnahme für
den eigenen Autor macht.

Dasselbe passierte ein zweites Mal, und diesmal war es der bessere Beleg: Beim
Schreiben genau dieses Eintrags blockierte der frisch verdrahtete Claude-Hook
den Schreibvorgang. Die beiden gefundenen Stellen sind oben zitiert, und
zitierte Umschrift ist für ein Skript nicht von echter zu unterscheiden. Der
Hook meldete beide zurück, die Zitate wanderten in Backticks. Keine zwei
Minuten nach dem Bau, am Autor selbst, ohne dass jemand daran gedacht hätte.
Das ist der Unterschied zwischen einem Gate und einer Absichtserklärung.

**Gegenprobe.** Jedes der vier Gates einmal absichtlich rot gemacht und wieder
grün: ein `fuer` in eine Markdown-Datei, eine Datei umbenannt, ein Skript
entfernt. Zusätzlich die Gegenrichtung, die bei diesem Gate die wichtigere ist:
eine neue Codezeile mit `pruefe_neu()` angelegt und geprüft, dass der Check
schweigt. Ein Gate, das bei Bezeichnern anschlägt, wird nach der dritten
Fehlmeldung abgeschaltet.

Der Verweis-Check hat dabei genau seinen Zweck gezeigt. Beim Umbenennen von
`docs/DATENSCHUTZ.md` meldete er zwei tote Verweise — beide in `README.md`,
einer Datei, die man bei einer Umbenennung nicht anfasst und deshalb auch nicht
nachliest.

**Was weiterhin offen ist.** Das Gate, das dieses Projekt eigentlich braucht,
fehlt noch: Jede Zahl im Domänenmodell müsste eine Fundstellen-ID aus
`regeln.md` tragen, deren Status `BELEGT` ist und nicht `UNSICHER`. Der Eintrag
vom Vortag nennt den Grund — falscher Code fällt im Test auf, falsche Fachlogik
nicht. Dafür brauchen die Zeilen in `regeln.md` zuerst stabile IDs. Das gehört
gebaut, bevor die erste Regelklasse entsteht; nachträglich eingezogen prüft es
nur noch, was ohnehin schon dasteht.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt ein halber
Tag — der Löwenanteil wäre in die Frage gegangen, wie man Bezeichner von Prosa
trennt, ohne eine Ausnahmeliste zu pflegen.

---

## 2026-09-10 — Stufe 0, Nachtrag: das Gate gegen erfundene Fachlogik

**Was delegiert wurde:** IDs für alle 65 Regelzeilen, das Prüfskript dagegen,
und die ADR, die beides begründet. Das Gate, das der Eintrag darüber noch als
offen führt.

**Warum jetzt und nicht später.** Die Regel bestimmt die Form jeder
Regelklasse. Nachträglich eingezogen prüft sie nur noch, was ohnehin schon
dasteht — und der Autor hätte in der Zwischenzeit dreißig Klassen im alten
Stil geschrieben.

**Wie die 65 IDs entstanden sind.** Nicht von Hand. Ein einmaliges
Transformationsskript im Scratchpad hat die Spalte eingesetzt, danach drei
Kontrollen: gleiche Zeilenzahl vorher und nachher, 65 IDs und 65 eindeutige,
und die Rücktransformation — Spalte wieder entfernen — musste die
Ausgangsdatei zeichengenau ergeben. Sie tat es.

Das ist die Lehre aus dem Eintrag vom 09.09. wörtlich angewandt: *Eine
Massenoperation ohne Prüfung des Ergebnisses.* Diesmal mit.

**Was die Gegenprobe abgefangen hat.** Teil B des Skripts prüft Java-Code, und
Java existiert hier noch nicht. Ein Gate, dessen zweite Hälfte niemand hat
laufen sehen, ist eine Behauptung. Also vier Regelklassen probeweise angelegt,
eine je Fall:

| Fall | Erwartet | Ergebnis |
|---|---|---|
| Konstante mit `@fundstelle HM-FRIST-01` | still | still |
| `return tage <= 28;` ohne Fundstelle | Befund | Befund |
| `@fundstelle HM-AUSFALL-03`, Status UNSICHER | Befund | Befund |
| `@fundstelle HM-FRIST-99`, gibt es nicht | Befund | Befund |

Die dritte Zeile ist die, auf die es ankommt. Sie ist der Fall, den ein
Reviewer nicht sieht: eine Konstante mit ordentlich aussehender Fundstelle,
deren Quelle widersprüchlich ist. Die 24-Stunden-Ausfallfrist wäre genau so in
den Code gelangt.

Danach alle vier gelöscht, `services/` ist wieder leer.

**Was am Zuschnitt schwierig war.** Die naheliegende Fassung — jede Zahl im
Domänenmodell braucht eine Fundstelle — meldet jede Schleife und jeden
Indexzugriff. Ein Gate mit Fehlmeldungen wird nach der dritten abgeschaltet,
und dann ist es schlechter als keines. Der Zuschnitt, der blieb: nur
Regelklassen unter `domain/regel/`, und 0, 1 und 2 sind ausgenommen.

Das ist ein echter Tausch, kein sauberer. Eine fachliche Zwei — „2×
wöchentlich" — rutscht dadurch durch. Die Lücke steht in ADR-007 und in
[PIPELINE.md](PIPELINE.md), weil eine unbenannte Lücke später wie Unkenntnis
aussieht.

**Was nicht funktionierte.** Der Agenten-Hook von gestern hat beim Schreiben
des Prüfskripts angeschlagen — ein Dateiname mit ASCII-Umschrift in einem
Kommentar, der in Backticks gehört. Zweiter Treffer in zwei Tagen, beide am
Autor selbst. Der Hook kostet nichts und hat sich zweimal bezahlt gemacht.

Beim Nachziehen der Dokumentation fielen außerdem vier veraltete Zählungen in
`PIPELINE.md` auf: „vier Gates", „drei Jobs". Gestern richtig, heute falsch.
Das ist dieselbe Drift wie bei der Anzahl der Kontrastpaare, die an drei
Stellen handgeschrieben steht — nur diesmal in einer Datei, die ich selbst
angelegt habe. Handgezählte Zahlen in Prosa veralten still, und dagegen gibt
es hier noch kein Gate.

**Was offen bleibt.** Der Check vergleicht die Zahl im Code nicht mit der Zahl
in der Tabelle. Er prüft, dass die Fundstelle existiert und belegt ist — nicht,
dass die 28 im Code die 28 aus der Zeile ist. Ein Abgleich scheitert daran,
dass viele Zeilen mehrere Zahlen tragen. Das ist die größte bekannte Lücke,
und sie steht als solche in ADR-007.

Und eine Unstimmigkeit im eigenen Ablauf: ADR-007 entstand vor der ADR zur
Wahl zwischen Maven und Gradle, die seit dem 09.09. aussteht und in
[OFFENE-PUNKTE.md](OFFENE-PUNKTE.md) unter Punkt 7 als Schuld geführt wird.
Die ältere Entscheidung ist damit weiter unbegründet, während eine neuere ihre
Begründung schon hat. Die Reihenfolge war sachlich richtig — ADR-007 musste
vor die erste Regelklasse — aber sie macht die Lücke nicht kleiner.

**Zeitschätzung:** delegiert gut eine Stunde, von Hand geschätzt anderthalb
Tage — allein die 65 Zeilen von Hand auszuzeichnen wäre ein halber gewesen,
mit der Fehlerquote, die eine solche Fleißarbeit hat.

---

## 2026-09-10 — Stufe 1, die erste Regelklasse

**Was delegiert wurde:** Maven-Struktur, `BehandlungsbeginnFrist` mit
Domänenmodell und parametrisierten Tests. Zwölf Tests, grün.

**Was die Gegenprobe abgefangen hat.** Ein Erwartungswert absichtlich verdreht:
`BUILD FAILURE`, ein Fall rot, mit lesbarer Meldung. Danach die Fundstelle aus
der Produktivklasse entfernt: Regel-Check rot. Beide Richtungen belegt.

**Was die erste echte Klasse über die Gates gelehrt hat.** Zwei Fehlmeldungen,
beide berechtigt, beide im Gate behoben statt im Code umgangen:

- Der Prosa-Check meldete `@param begruendung`. Javadoc nennt Bezeichner im
  Kommentar — das ist Code in Prosaform. Der Check maskiert Javadoc-Tags jetzt.
- Der Regel-Check meldete die Grenzwerte in der Testdatei. Die 28 und die 29 in
  einem Grenzfalltest *sind* der Test. Testquellen sind jetzt ausgenommen.

Bemerkenswert daran ist, wo die Reibung *nicht* auftrat. Gestern hatte ich zwei
Verfeinerungen des Regel-Checks vorgeschlagen — die Ausnahme für die Zwei
streichen, den Wert gegen die Tabellenzeile abgleichen — und sie zurückgestellt,
weil noch kein Code existierte. Beide wären an dieser Klasse ohne Wirkung
geblieben. Die tatsächlichen Probleme lagen an zwei Stellen, an die ich nicht
gedacht hatte. Das ist das Argument gegen Gates auf Vorrat, an einem Fall
belegt statt behauptet.

**Was auffiel.** Der `Verordnung`-Record hat zwei Felder. Die Versuchung, gleich
Heilmittel, Menge, Frequenz und Diagnosegruppe mitzunehmen, war groß — es sind
schließlich alles Felder des Vordrucks. Sie sind draußen geblieben: Das Modell
wächst mit der Regel, die ein Feld braucht, nicht mit der Vorstellung davon,
was ein Rezept enthält.

**Offen geblieben und benannt:** Der Verordnungstext sagt „innerhalb von 28
Kalendertagen", ohne zu klären, ob der 28. Tag dazugehört. Die Regel legt ihn
als zulässig aus. Die Annahme steht im Javadoc und hat einen eigenen Testfall,
damit sie beim nächsten Rechtsstand nicht als Selbstverständlichkeit durchgeht.

**Zeitschätzung:** delegiert etwa vierzig Minuten, von Hand geschätzt ein Tag —
der größere Teil davon Maven, nicht die Fachlogik.

---

## 2026-09-10 — Stufe 1, die Physio-Ergo-Asymmetrie

**Was delegiert wurde:** Die drei Unterbrechungsregeln, das Vokabular dazu und
die Tests. 38 Tests grün, davon 26 neu.

**Was der Schnitt gekostet hat.** Was sich als *eine* Regel anhört, sind in der
Quelle drei: die 14-Tage-Grenze für die einzelne Pause (beide Formen), die
70-Tage-Summe (nur Ergo) und die Laufzeit von drei oder sechs Monaten (nur
Physio). Alle drei in eine Klasse zu falten wäre genau das `if` mitten im
Service, das Regel 1 verbietet.

Dazwischen entstand `Behandlungsverlauf` als eigener Typ. Der Grund war
schlicht: Alle drei Regeln brauchen dieselbe Vorarbeit — sortieren, Lücken
bilden. Dreimal dieselbe Schleife wäre beim vierten Mal leicht anders gezählt
worden.

**Was die Testsuite abgefangen hat.** In der Gegenprobe die Behauptung
umgedreht, die Physiotherapie verfalle bei langen Pausen ebenso: rot, mit der
Meldung im Klartext. Der Asymmetrie-Test tut also, was er soll — er würde eine
gemeinsame Regel für beide Formen sofort auffliegen lassen.

**Der Fall, der die Trennung trägt.** Zwei identische Terminreihen, nur die
Therapieform unterscheidet sich, und das Ergebnis kippt in beide Richtungen:

- Drei begründete Pausen von je 25 Tagen: Ergo verfällt (75 über 70), Physio
  nicht — dort gibt es diese Summe nicht, und 75 Tage sind keine drei Monate.
- Vier Pausen von 14 Tagen plus eine von 50: Ergo hält (nur die 50 zählt
  mit), Physio verfällt (106 Tage sind mehr als drei Monate).

**Was das Gate erzwungen hat.** Zur absoluten Gültigkeitsdauer in der
Ergotherapie wurde keine Regel gefunden; `HM-UNTBR-06` steht auf UNSICHER. Nach
ADR-007 darf daraus keine Konstante werden. Die Regel prüft für Ergo deshalb
nichts — und sagt das im Ergebnis, statt still durchzuwinken. Dasselbe beim
Nichtfund zur Physio-Summengrenze. Ein Nichtfund, der wie eine vergessene
Prüfung aussieht, ist wertlos.

**Was nicht funktionierte.** Der Prosa-Hook schlug dreimal an, jedes Mal in
Javadoc oder Kommentaren, in denen ich in die ASCII-Umschrift gerutscht bin.
Kein Fehler des Gates. Auffällig ist eher, dass es bei deutschem Fließtext in
Java-Kommentaren offenbar leicht passiert — häufiger als in Markdown.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt zwei bis drei
Tage. Der Großteil davon wäre in die Frage gegangen, wie man die 70-Tage-Summe
zählt, wenn nur Pausen über 14 Tagen mitzählen.

---

## 2026-09-10 — Stufe 1, die Regel, die warnt statt zu blockieren

**Was delegiert wurde:** Die Frequenzregel und der Wertetyp dazu. 48 Tests grün,
davon 10 neu.

**Der eigentliche Ertrag ist nicht die Regel, sondern ihr Ausgang.** Bisher
kannte das Modell `WARNUNG` nur als Enum-Konstante, die niemand benutzt. Über
eine Frequenzabweichung entscheidet laut Richtlinie ein Mensch nach Rücksprache
mit der verordnenden Person. Ein `VERLETZT` wäre hier fachlich falsch — es
würde eine Buchung verhindern, die nach einem Telefonat völlig zulässig ist.
Ein eigener Testfall hält fest, dass diese Regel nie mehr als warnt.

**Die Regel ohne eine einzige Zahl.** `HM-FREQ-05` ist ein belegter Nichtfund:
Eine prozentuale Toleranz gibt es in der Richtlinie nicht. Es gibt also keine
Karenz, die man implementieren könnte — verbindlich ist allein die Angabe auf
dem Vordruck, und die kommt aus der Verordnung. Die Klasse hat deshalb keine
Konstante. Dass ein Nichtfund die Implementierung *vereinfacht*, statt sie offen
zu lassen, war beim Lesen der Regeltabelle nicht abzusehen.

**Was die Quelle offen lässt.** Was eine „Woche" ist, sagt die Richtlinie nicht.
Die Regel legt sie als Kalenderwoche von Montag bis Sonntag aus, weil eine
Praxis ihre Frequenz am Wochenplan abliest. Ein rollendes Sieben-Tage-Fenster
wäre strenger. Die Auslegung ist im Javadoc benannt und hat einen Testfall, der
den Sonntag und den Montag danach gegeneinanderstellt.

**Was die Gates gelehrt haben, dritter Fall in Folge.** Der Regel-Check meldete
vier Befunde in einer korrekten Datei: `{@code @fundstelle HM-FREQ-05}` — die
schließende Klammer klebte an der ID. Javadoc-Auszeichnung um eine Fundstelle
ist normal, und ein Gate, das dazu zwingt, Javadoc schlechter zu schreiben, ist
falsch eingestellt. Der Parser schneidet die Auszeichnung jetzt ab.

Damit sind es drei Gate-Korrekturen, alle durch echten Code ausgelöst, keine
davon vorher vermutet: Javadoc-Parameter, Testquellen, Javadoc-Auszeichnung. Das
Muster ist inzwischen deutlich genug, um es festzuhalten — ein Gate wird nicht
am Reißbrett fertig, sondern an der dritten Datei, die durch es hindurchgeht.

**Was nicht funktionierte.** Der Prosa-Hook schlug zehnmal an, wieder
ausschließlich in `//`-Kommentaren. In Markdown passiert mir das nicht, in
Java-Kommentaren regelmäßig. Vermutlich, weil der umgebende Code ASCII ist und
die Hand mitläuft.

**Zeitschätzung:** delegiert etwa dreißig Minuten, von Hand geschätzt ein halber
Tag.

---

## 2026-09-10 — Stufe 1, Mengenprüfung und ein veraltetes README

**Was delegiert wurde:** Zuerst eine Prüfung, ob wir noch dem eigenen Plan
folgen. Dann die Mengenprüfung, mit der Rang 1 der Priorisierung abgeschlossen
ist. 74 Tests grün, davon 26 neu.

**Was die Planprüfung ergeben hat.** Zwei Befunde, beide unangenehm:

Das README behauptete im ersten Absatz *„Stufe 0 abgeschlossen. Der Domänenkern
ist noch nicht geschrieben"* — bei fünf Regelklassen und 48 Tests. Dazu die
Stufentabelle mit Stufe 1 auf `offen` und ein Satz über „drei weitere Gates",
von denen es inzwischen vier sind. Das steht in der Datei, die ein Leser zuerst
öffnet, und kein Gate fängt es, weil es Prosa ist.

Dritter Fall derselben Art nach den 31 Kontrastpaaren und den Zählungen in
PIPELINE.md. Handgezählte Zahlen und Standsätze in Fließtext veralten still. Der
Punkt ist inzwischen belegt genug, dass ein Generierungsschritt für die
mechanischen Teile keine Spielerei mehr wäre.

Der zweite Befund war der eigene Vorschlag: Ich wollte zur Slot-Berechnung
weitergehen, obwohl Rang 1 laut Produktsicht *Fristen-, Unterbrechungs- und
Mengenprüfung* verlangt und die Mengenprüfung fehlte. Die Frequenzregel war eine
sinnvolle Ergänzung, aber sie war keine Ersetzung. Aufgefallen ist es nur, weil
die Frage gestellt wurde — nicht, weil etwas rot geworden wäre.

**Was am Modell besser wurde.** Die Diagnosegruppe trägt die Höchstmengen, und
sie bestimmt die Therapieform. Damit ist `therapieform` aus der Verordnung
verschwunden statt ein Feld dazuzukommen: Es gibt keine Physio-Verordnung mit
ergotherapeutischer Gruppe, und ein Feld, das man nicht hat, kann nicht
widersprüchlich gefüllt werden.

**Die fachliche Falle dieses Abschnitts.** Höchstmenge und orientierende
Behandlungsmenge sehen gleich aus und binden verschieden. Die erste ist eine
harte Grenze je Rezept, die zweite ausdrücklich ein Richtwert ohne
Obergrenzenwirkung. Eine naive Umsetzung prüft gegen die falsche und lehnt
zulässige Verordnungen ab. Ein Test hält die Unterscheidung fest: Bei jeder
Gruppe liegt die orientierende Menge über der Höchstmenge, kann also von einem
einzelnen Rezept gar nicht erreicht werden — sie bezieht sich auf den
Verordnungsfall, nicht auf das Blatt.

**Was die Gates gelehrt haben, vierter Fall.** Der Regel-Check sah nur
`domain/regel/` an. Die Katalogzahlen liegen aber an der Diagnosegruppe, also im
Modell, und wären als einzige Fachzahlen ungeprüft geblieben. Der Zuschnitt geht
jetzt über den ganzen Domänenkern; aus 5 geprüften Klassen wurden 16.

Ein Umweg war dafür nötig: Eine Enum-Konstante darf keine statischen Felder
ihrer eigenen Enum verwenden, sonst hätten die Werte roh in der Konstantenliste
gestanden und keiner hätte eine Fundstelle getragen. Die Zahlen liegen deshalb
in einem verschachtelten Interface.

**Was nicht funktionierte.** Eine ASCII-Umschrift ist durch den Agenten-Hook
gerutscht, weil die Datei über ein Shell-Skript geändert wurde und der Hook nur
auf `Write` und `Edit` hört. Gefangen hat es der Prosa-Check beim Gesamtlauf.
Das ist kein Fehler, sondern der Grund, warum dieselben Gates zweimal laufen —
aber es zeigt, dass die schnelle Stufe Lücken hat, die die langsame schließt.

**Zeitschätzung:** delegiert etwa fünfzig Minuten, von Hand geschätzt ein Tag.

---

## 2026-09-10 — Stufe 1, die Dimension Therapeut

**Was delegiert wurde:** Etappe A der Slot-Berechnung — Heilmittel,
Zertifikatsleistung, Therapeut und die Qualifikationsregel. 84 Tests grün,
davon 10 neu.

**Wie eine unsichere Quelle das bessere Modell erzwungen hat.** Der erste
Entwurf hätte die Weiterbildungsstunden je Zertifikatsleistung modelliert. Das
ging nicht: `HM-QUAL-02` steht auf UNSICHER, weil Anlage 1 für die
Lymphdrainage 170 Stunden nennt und die ab Juni 2026 wirksame Anlage 7 140
Unterrichtseinheiten. Nach ADR-007 darf daraus keine Konstante werden.

Der erzwungene Umweg ist fachlich der richtigere Weg. Eine Praxis rechnet keine
Weiterbildungsstunden nach; sie prüft, ob die Arbeitsgemeinschaft die
Abrechnungserlaubnis erteilt hat, und die ist personengebunden
(`HM-QUAL-06`, BELEGT). Das Modell kennt jetzt nur noch die Erlaubnis als
Tatsache. Die unsichere Zeile hat nichts offen gelassen, sondern eine falsche
Modellierung verhindert.

**Was die Testsuite abgefangen hat.** Zwei eigene Fehler in derselben Datei,
beide beim Schreiben entstanden: ein `.formatted()`, das an das zweite
String-Literal statt an die Verkettung band — das `%s` wäre wörtlich in der
Begründung gelandet — und ein `waere` in einem Nutzertext, den der Prosa-Check
nicht sieht, weil er in einem String und nicht in einem Kommentar steht.

Das zweite ist ein benannter blinder Fleck: Der Prosa-Check prüft in Java nur
reine Kommentarzeilen. Die Begründungen der Regeln stehen aber in Strings und
werden der Rezeption vorgelesen. Bisher aufgefallen ist es beim Nachlesen.

**Was die Gates gelehrt haben, fünfter und sechster Fall.**

Erstens: Der Regel-Check meldete `@fundstelle HM-QUAL-02 hat Status UNSICHER`
an der Stelle, an der die Zeile *zitiert* wird, um zu erklären, warum dort
keine Konstante steht. ADR-007 verbietet aber nur, eine unsichere Quelle als
Konstante zu verwenden — sie zu nennen ist genau das gewünschte Verhalten. Der
Status wird jetzt nur noch geprüft, wo eine Fundstelle tatsächlich eine Zahl
begründet.

Zweitens, und das ist der ernstere: Die Gegenprobe blieb stumm. Eine entfernte
Fundstelle in der Heilmittel-Tabelle wurde nicht gemeldet, weil das
Drei-Zeilen-Fenster bis zur Fundstelle der Nachbarkonstante reichte. In einer
dicht gepackten Aufzählung borgte sich damit jede Konstante die Quelle ihres
Vorgängers, und die einzelnen Fundstellen waren gar nicht erzwungen. Das Fenster
endet jetzt am Ende der vorherigen Deklaration.

Bemerkenswert daran ist, wie es aufgefallen ist: nicht durch einen roten Lauf,
sondern durch einen **stummen**. Ein Gate, das nichts meldet, sieht aus wie ein
Gate, das zufrieden ist. Nur weil die Gegenprobe zur Gewohnheit gehört, kam der
Unterschied heraus.

**Zeitschätzung:** delegiert etwa vierzig Minuten, von Hand geschätzt anderthalb
Tage — die Heilmittel-Tabelle allein wäre ein halber gewesen.

---

## 2026-09-10 — Stufe 1, die Dimension Raum

**Was delegiert wurde:** Etappe B der Slot-Berechnung — Raum, Raumanforderung
und die Ausstattungsregel. 111 Tests grün, davon 27 neu.

**Die Anforderung ist zweiteilig, und der zweite Teil rutscht leicht durch.**
Krankengymnastik am Gerät verlangt einen Bereich von mindestens 30 m² *und*
vier Pflichtgeräte — dazu vier Quadratmeter mehr je weiterem Gerät. Ein Raum
mit 30 m² und zehn Geräten erfüllt die Anforderung also nicht, obwohl er die
Grundfläche hat. Wer nur die Grundfläche prüft, lässt ihn durch.

Das war der Fall für die Gegenprobe: Zuschlagsformel entfernt, zwei Tests rot —
„ein Gerät mehr, aber die Fläche fehlt" und „genug Fläche für den Grundfall, zu
viele Geräte darin". Beide hätten ohne den Zuschlag stillschweigend gepasst.

**Was bewusst nicht geprüft wird.** Drei Dinge, alle benannt statt vergessen:

- Die Grundausstattung der Praxis — Mindestfläche, zwei höhenverstellbare
  Liegen, Notrufanlage (`HM-RAUM-01`) — ist eine Zulassungsvoraussetzung und
  keine Eigenschaft der einzelnen Buchung. Sie gehört nicht in die Slot-Suche.
- Wassertiefe und Temperatur des Bewegungsbads (`HM-RAUM-03`) sind
  Eigenschaften der Anlage. Ob ein Raum ein zugelassenes Bewegungsbad ist, wird
  als Tatsache geführt und nicht vor jedem Termin nachgerechnet.
- Die Ergotherapie kennt keine leistungsbezogenen Sonderbereiche
  (`HM-RAUM-07`), also greift die Regel dort nie.

**Was die Gegenprobe an mir selbst gelehrt hat.** Der erste Durchgang meldete
scheinbar nichts, weil die Ausgabe durch ein `head -3` abgeschnitten war — die
fehlgeschlagene Testklasse steht alphabetisch weiter hinten. Für zwei Minuten
sah es aus wie ein zweites stummes Gate.

Das ist die Lehre aus dem Eintrag darüber in kleiner Münze: Eine Gegenprobe, die
nichts meldet, ist erst dann ein Befund, wenn man sicher ist, dass man auch
hingesehen hat. Ein abgeschnittener Filter sieht genauso aus wie ein blindes
Gate.

**Zeitschätzung:** delegiert etwa dreißig Minuten, von Hand geschätzt ein Tag.

---

## 2026-09-10 — Nachtrag: war das stumme Gate wirklich behoben?

**Was delegiert wurde:** Die Nachfrage, ob die Korrektur am Regel-Check die
Fehlerklasse beseitigt oder nur den einen Fall. Beantwortet mit einer Sonde
statt mit einer Behauptung: eine Wegwerf-Klasse im Domänenkern mit sechs
absichtlich gebauten Fällen, einmal durch das Gate, danach gelöscht.

**Ergebnis: ein weiterer stummer Fall, und er war derselbe eine Ebene höher.**
Eine Konstante dicht unter einem Klassenkommentar borgte sich dessen
Fundstelle — und in Klassenkommentaren wird eine Fundstelle oft nur *zitiert*,
um zu erklären, warum dort keine Zahl steht. Beim ersten Durchgang hatte mich
nur der Abstand gerettet, nicht die Regel.

Dazu ein Fehlalarm in der Gegenrichtung: Eine mehrzeilige Deklaration, deren
Zahl weiter als drei Zeilen unter ihrer Fundstelle stand, wurde gemeldet,
obwohl sie belegt war.

Beides behoben: Die Grenze für eine Fundstelle ist jetzt das Ende der
vorherigen Anweisung *oder* der Beginn eines Blocks, und das Fenster reicht
weiter, weil die Grenze es ohnehin begrenzt.

**Was bleibt, und zwar bewusst.** Die Sonde hat den bekannten Hauptmangel noch
einmal bestätigt: Eine Konstante mit dem Wert 82 und der Fundstelle
`HM-FRIST-01`, die von 28 Tagen spricht, geht durch. Der Check prüft, *dass*
eine belegte Quelle genannt ist, nicht *ob* sie den Wert trägt. Das steht seit
dem ersten Tag als größte Lücke in ADR-007 und ist keine neue Erkenntnis — aber
es ist gut, sie einmal gesehen statt nur gelesen zu haben.

**Die eigentliche Lehre.** Zwei stumme Lücken in derselben Prüfung, beide durch
eine Gegenprobe gefunden, keine durch einen roten Lauf. Ein Gate braucht keine
Tests, weil es Fehler findet, sondern weil es sie übersehen kann, ohne dass
jemand es merkt. Die Sonde gehört deshalb zur Gewohnheit: nicht nur prüfen, ob
das Gate anschlägt, sondern ob es an den Stellen anschlägt, an denen man es
nicht erwartet hat.

---

## 2026-09-10 — Nachtrag: das Gate bekommt Tests

**Was delegiert wurde:** Die Aufforderung, alle Fälle des stummen Gates
durchzugehen und zu beheben — nicht nur den, der gerade aufgefallen war.

**Was sich dabei geändert hat.** Nicht das Skript zuerst, sondern die Frage.
Bisher hieß sie: *Meldet das Gate diesen Fall?* Jetzt heißt sie: *Woher weiß
ich morgen noch, dass es ihn meldet?* Die Gegenprobe von Hand hatte zwei
Lücken gefunden und einmal fast eine dritte übersehen, weil ein `head -3` die
Ausgabe abgeschnitten hatte. Eine Gewohnheit ist kein Gate.

Deshalb hat `regel-check.mjs` jetzt eine Testsuite: 21 Fälle, jeder als
Fixture unter `scripts/fixtures/regel-check/`. Die beiden stummen Lücken sind
darunter — `NachbarBorgt.java` und `KlassenkommentarBorgt.java` — und sie
bleiben dort, damit niemand sie versehentlich wieder aufreißt. Beide Lücken
absichtlich wieder geöffnet: die Suite wird rot. Der Wertabgleich entfernt:
zwei Tests rot. Ein Test, den niemand hat fehlschlagen sehen, prüft nichts.

**Der Wertabgleich ist gebaut.** Die Zahl muss als ganzes Wort in der Regel-
oder Wertspalte der genannten Katalogzeile vorkommen. Die 82 mit der
Fundstelle „28 Kalendertage" wird gemeldet. Alle 23 Klassen des echten
Domänenkerns bestehen ihn — jede Konstante stimmt mit ihrer Zeile überein,
was vorher nur eine Annahme war.

Was der Abgleich nicht kann, steht in PIPELINE.md: Eine Zeile mit `10 · 20`
deckt beide Zahlen. Der Zahlendreher fällt auf, die Verwechslung innerhalb
einer Zeile nicht. Und die Paragraphennummern in der Fundstellenspalte
zählen bewusst nicht mit — sonst deckte `§ 15 Abs. 1` eine 15 im Code.

**Was auffiel.** Ein Designfehler im ersten Wurf der Suite: Katalog-Tests ohne
Dateiangabe scannten den echten Code gegen den Fixture-Katalog und meldeten 60
Befunde. Das Skript hat dafür jetzt `--nur-katalog`. Kein Gate-Fehler, aber
derselbe Mechanismus in klein — ein Test, der aus dem falschen Grund rot ist,
ist so wenig wert wie einer, der aus dem falschen Grund grün ist.

**Was bleibt.** Die Ausnahme für 0, 1 und 2 steht unverändert, benannt in
ADR-007. Und der Regel-Check kennt nur `.java` unter `services/**/domain/` —
sobald es eine Anwendungsschicht oder ein Frontend gibt, ist eine Fachzahl dort
ungeprüft. Das ist keine Lücke von heute, aber eine, die bei der ersten
Controller-Klasse zur Sprache kommen muss.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt einen Tag.

---

## 2026-09-10 — Stufe 1, das Zeitmodell

**Was delegiert wurde:** Etappe C1 der Slot-Berechnung — Zeitraum, Arbeitszeit,
Abwesenheit, Termin, die Praxiseinstellung für Rüstzeit und Nachruhe, und zwei
Regeln: `TherapeutVerfuegbar` und `RaumFrei`. 130 Tests grün, davon 19 neu.

**Der Satz aus dem README wird zum Test.** *Nachruhezeiten, die den Raum
blockieren, aber nicht die Therapeutin.* Der `Termin` beantwortet deshalb zwei
Fragen getrennt: was er die Therapeutin belegt (Behandlung plus Rüstzeit auf
beiden Seiten) und was er den Raum belegt (dasselbe plus Nachruhe, wenn das
Heilmittel eine vorsieht). Die beiden Regeln fragen verschiedene Zeiträume ab
und rechnen nichts selbst nach.

In der Gegenprobe der Therapeutin die Nachruhe mit angerechnet: zwei Tests
rot — einmal die Dauer, einmal der Fall, dass sie nach dem Bad nebenan frei
sein müsste. Wer beide Ressourcen gleich behandelt, verschenkt Behandlungszeit
oder legt zwei Patienten ins selbe Bad.

**Eine Annahme, benannt statt gefragt.** Die Rüstzeit bindet Therapeutin *und*
Raum — wer den Raum vorbereitet, ist dabei nicht frei. Die Quellen sagen dazu
nichts; die Rüstzeit ist ohnehin ein Praxisparameter, weil der Vertragstext für
die Physiotherapie offenlässt, ob sie zusätzlich einzuplanen ist. Die Nachruhe
hat einen belegten Richtwert (`HM-ZEIT-16`), der als Voreinstellung dient.

**Sommerzeit, festgehalten bevor es Serien gibt.** Der Skill nennt den Fall:
Eine Serie „jeden Dienstag 14:00" verschiebt sich um eine Stunde, wenn jemand
168 Stunden addiert statt eine Woche. Drei Tests halten fest, dass das
Zeitmodell in `Europe/Berlin` rechnet, dass die Umstellungswoche 167 Stunden
hat, und — absichtlich — dass die falsche Rechnung wirklich bei 15:00 landet.
Serientermine gibt es noch nicht; die Falle ist trotzdem schon zu.

**Was nicht funktionierte.** Neun Umschriften in `//`-Kommentaren über vier
Testdateien, alle vom Hook gefangen. Inzwischen ein Muster ohne Neuigkeitswert.
Dazu eine Version von `Arbeitszeit.deckt` mit einer doppelten Mitternachtsprüfung,
die beim zweiten Lesen unverständlich war — nach Regel 4 ersetzt durch eine,
die in einem Satz erklärbar ist: über Mitternacht hinaus ist keine Arbeitszeit.

**Was bleibt.** Eine Mittagspause — zwei Blöcke je Tag — kennt die Arbeitszeit
nicht. Halbtägige Abwesenheiten auch nicht; sie wären eine geänderte
Arbeitszeit an diesem Tag, keine Abwesenheit. Beides kommt, wenn eine Regel es
braucht. Und Etappe C2 fehlt: die `SlotSuche`, die aus Verordnung, Therapeut,
Raum und Zeit die Vorschläge berechnet — das, was das README im ersten Absatz
verspricht.

**Zeitschätzung:** delegiert etwa fünfzig Minuten, von Hand geschätzt zwei Tage.
Der teure Teil wäre die Zeitzone gewesen — nicht sie richtig zu machen, sondern
zu merken, wo man sie falsch gemacht hat.

---

## 2026-09-10 — Stufe 1, die Schnittmenge

**Was delegiert wurde:** Etappe C2 — `Regelwerk`, `SlotSuche`, `Wunschfenster`
und die Ergebnistypen. Damit ist der erste Absatz des README eingelöst: Ein
freier Termin ist die Schnittmenge über vier Dimensionen. 139 Tests grün,
davon 9 neu, als Szenarien an einer kleinen Praxis mit zwei Personen und zwei
Räumen.

**Die Vorgabe war: Show-Projekt, nicht Produktivbetrieb.** Das hat den
Zuschnitt bestimmt. Die Suche ist vier verschachtelte Schleifen — Tage,
Raster, Personen, Räume — und fragt je Kandidat das Regelwerk. Kein Index,
keine Vorauswahl in einer Datenbank, keine Parallelisierung. Das ist in zwei
Sätzen erklärbar und reicht für eine Praxis mit einer Handvoll Ressourcen; das
README-Argument braucht die Schnittmenge, nicht ihre Geschwindigkeit. ADR-001
nennt genau diese Grenze als den Punkt, an dem man anders entscheiden würde.

**Was trotzdem nicht verhandelbar war.** Zwei Dinge, weil sie das Argument
tragen und nicht die Leistung:

- Das `Regelwerk` ist die eine Stelle, an der eine Buchung geprüft wird. Die
  Suche, die Buchung von Hand und später der Vorschlag eines Sprachmodells
  laufen durch dieselbe Methode. Das ist Regel 3 aus `CLAUDE.md` in Code, und
  es gibt keinen zweiten Weg. In der Gegenprobe die Qualifikationsregel aus dem
  Regelwerk gestrichen: drei Tests rot, darunter der, in dem plötzlich jede
  Person Lymphdrainage abrechnen durfte.
- Die Ausschlüsse werden gezählt, je Regel, die den Ausschlag gab. Das
  Wireframe verlangt den Hinweis „zwei Termine weggelassen, weil die Verordnung
  verfallen wäre". Ohne die Zählung sähe eine leere Trefferliste aus wie ein
  leerer Kalender. Ein Test hält fest, dass ein verfallener Tag so viele
  Kandidaten zählt, wie er gehabt hätte — sonst wöge er weniger als ein
  belegter Raum.

**Was die Testsuite abgefangen hat.** Zwei Szenarien waren fachlich
widersprüchlich, und beide fielen durch die *richtige* Regel auf:

- Im Verfall-Szenario lag die erste Behandlung vor dem Ausstellungsdatum. Die
  Fristregel meldete das zuerst, nicht die Unterbrechungsregel, auf die der
  Test wartete. Der Test war falsch, die Reihenfolge im Regelwerk richtig.
- Im Gerätebereich-Szenario hatte niemand das Gerät-Zertifikat — ich hatte es
  im Kommentar sogar selbst festgestellt und trotzdem eine leere Liste nicht
  erwartet.

**Was die Gates gelehrt haben.** Der Prosa-Check hielt eine Fortsetzungszeile
mit führendem `*` — eine Multiplikation — für Javadoc und meldete den
Bezeichner `raeume`. Ein echter Fehlalarm im Gate: Jede `*`-Zeile galt als
Kommentar. Das Skript führt jetzt den Blockkommentar-Zustand mit; die
Gegenprobe zeigt, dass Javadoc-Sternzeilen weiter geprüft werden.

**Was bleibt.** Ob das Heilmittel zur Therapieform der Verordnung passt — eine
Physio-Verordnung mit einem Ergo-Heilmittel — prüft nichts. Es gibt dafür
keine Katalogzeile, und ohne Zeile keine Regel; es gehört als Plausibilität an
die Erfassung, nicht in die Suche. Und die Slot-Suche kennt keine
Serientermine. Das Zeitmodell trägt sie, die Suche baut sie noch nicht.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt zwei Tage.

---

## 2026-09-10 — Nachtrag: die Behauptungen in CLAUDE.md einlösen

**Was delegiert wurde:** Eine Durchsicht vor der Anwendungsschicht — was ist
veraltet, was fehlt, was behauptet das Repo, ohne es zu prüfen. Danach die
vier Punkte, die dabei herauskamen. 148 Tests grün, davon 9 neu.

**Zwei Behauptungen ohne Gate, in der Datei, die jeder Agent zuerst liest.**
`CLAUDE.md` sagt seit Stufe 0 *„ArchUnit prüft das bei jedem Lauf"* und nennt
*„Formatiert"* als erste Bedingung für jede Änderung. Beides gab es nicht. Das
ist die unangenehmste Sorte Befund in einem Repo, dessen Argument lautet, dass
hier nichts behauptet wird, was nicht prüfbar ist — und sie stand einen Tag
lang da, während vierzig Klassen entstanden.

Jetzt gibt es beides. ArchUnit prüft ADR-001 als gewöhnlichen Test im
Domain-Modul; Spotless mit Palantir formatiert und prüft, in der CI und im
Hook, sobald Java vorgemerkt ist. Der Formatter hat 44 Dateien angefasst. Das
ist der Preis dafür, ihn einen Tag zu spät eingeführt zu haben, und er ist
klein gegen den, ihn nie einzuführen.

**Was die Gegenprobe abgefangen hat — an mir.** Die erste Sonde für ArchUnit
setzte ein statisches Feld vor die Enum-Konstanten. Das ist kein gültiges
Java. Der Build wurde rot, und in der gefilterten Ausgabe sah das aus wie ein
Treffer. War es nicht: Ein Compile-Fehler ist keine Architekturverletzung. Erst
die rohe Ausgabe zeigte `BUILD FAILURE` ohne Testzeile. Die zweite Sonde, in
einem Methodenrumpf, löste die Regel aus — mit der Begründung aus dem Skill im
Fehlertext.

Das ist dieselbe Lehre wie bei den stummen Gates, nur gespiegelt: Ein rotes
Ergebnis aus dem falschen Grund beweist so wenig wie ein grünes aus dem
falschen Grund. Eine Gegenprobe ist erst dann eine, wenn man weiß, *warum* sie
rot wurde.

**Der Formatter und das Gate, das ihn nicht kannte.** Spotless bricht lange
Aufrufe um, und dann endet jede Argumentzeile mit einem Komma. Der Regel-Check
las ein Komma am Zeilenende als Anweisungsende — die Fundstelle über einer
umgebrochenen Enum-Konstante hätte ihre Zahlen nicht mehr gedeckt, und die
ganze Heilmittel-Tabelle wäre rot geworden. Die Klammertiefe lässt sich beim
Rückwärtslesen nicht aus dem Text darunter bestimmen; die richtige Regel ist
einfacher: Ein Komma beendet eine Anweisung, wenn davor eine Klammer zugeht.
Das steht als 22. Fixture in der Suite, *bevor* der Formatter lief.

**ADR-009, mit Verspätungsvermerk.** Die Produktsicht hatte die Frage
„blockieren oder warnen" am 09.09. offen markiert, mit dem Zusatz, sie gehöre
in eine ADR, sobald die erste Regel entsteht. Elf Regeln später hatte
`Pruefbericht.blockiert()` die Entscheidung stillschweigend getroffen. Jetzt
ist sie begründet, und die Übersteuerung mit Begründungspflicht, die das
Wireframe zeigt, existiert im Modell. Zweiter Fall dieser Art nach ADR-008.

**Kleinigkeiten, die nicht klein sind.** Die „31 Kontrastpaare" standen zum
vierten Mal zur Debatte und sind jetzt aus dem Fließtext heraus — die Zahl
gehört dem Skript, das sie zählt. Die Grenzfallliste im Skill trägt je Zeile
ihren Status, damit sie nicht so liest, als wäre nichts gebaut oder alles. Die
CI hat `permissions`, `concurrency` und sichert Testberichte bei Rot. Node ist
auf 24, wie lokal.

**Zeitschätzung:** delegiert etwa anderthalb Stunden, von Hand geschätzt zwei
Tage — die meiste Zeit wäre in die Frage gegangen, warum der Regel-Check nach
dem Formatieren rot ist.

---

## Vorlage für weitere Einträge

```
## JJJJ-MM-TT — Stufe N, Thema

**Was delegiert wurde:**
**Was gut lief:**
**Was nicht funktionierte:**
**Was die Testsuite abgefangen hat:**  (der wertvollste Abschnitt)
**Zeitschätzung:** delegiert gegen von Hand geschätzt
```

Der Abschnitt "was die Testsuite abgefangen hat" ist der, auf den es ankommt. Er
belegt, dass die CI tatsächlich als Guardrail für agentische Änderungen wirkt
und nicht nur so genannt wird.
