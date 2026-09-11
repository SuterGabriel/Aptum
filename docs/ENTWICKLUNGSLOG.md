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

## 2026-09-10 — Stufe 1, das erste, was man starten kann

**Was delegiert wurde:** ADR-002 zur Mandantentrennung und Schritt A der
Anwendungsschicht: drei Module nach ADR-008, Spring Boot in `infrastructure`,
`MandantId` und `MandantKontext` in `application`, ein Health-Endpunkt und
ArchUnit über die Modulgrenzen. 152 Tests grün, davon 4 neu.

**ADR-002 im Moment der Entscheidung.** Zum ersten Mal ohne
Verspätungsvermerk. Die Frage war seit Stufe 0 reserviert und wurde fällig,
weil die erste Tabelle ihren Schnitt braucht. Entschieden: eine Spalte je
Tabelle, Row Level Security in Postgres als zweite Verteidigungslinie, der
Mandant im Sicherheitskontext statt im Domänenobjekt. Der Isolationstest ist
das Gate, und die ADR sagt selbst, was ohne ihn gälte — dann wäre Schema je
Mandant vorzuziehen, weil Struktur Disziplin schlägt, wenn niemand die
Disziplin prüft.

**Was gut lief.** Spring Boot als BOM statt als Parent-Pom. Der eigene Parent
bleibt, und `domain` wie `application` erben nichts von Spring — die Module
ohne Framework bleiben es auch im Build, nicht nur im Quelltext. ArchUnit
prüft die Modulgrenzen jetzt dort, wo alle drei auf dem Klassenpfad liegen,
als zweiter Riegel neben den Maven-Abhängigkeiten.

**Was die Gegenprobe abgefangen hat — zweimal an mir, wieder.** Die Sonde für
die Modulgrenzen kompilierte nicht, weil `application` kein Spring auf dem
Klassenpfad hat; ein Compile-Fehler ist keine Architekturverletzung. Also die
Sonde ins Infrastruktur-Modul, unter das `application`-Paket — ArchUnit prüft
Pakete, nicht Module, und das ist an dieser Stelle die richtige Sonde.

Der zweite Fehler war stiller: `mvn -pl infrastructure` fand
`scheduling-application` nicht, weil es nur im Reactor existiert und nicht im
lokalen Repository. Die Ausgabe zeigte einen Auflösungsfehler, mein Filter
zeigte nichts. Erst der Rücklauf ohne Sonde machte den Fehler sichtbar, weil
er *auch* rot war. Mit `-am` baut Maven die Abhängigkeiten mit, und dann
schlug die Regel an.

Das ist heute das dritte Mal, dass eine Gegenprobe aus dem falschen Grund
rot oder aus dem falschen Grund stumm war. Die Lehre steht inzwischen fest:
Ein Filter über eine Build-Ausgabe muss die Zeile enthalten, die den Erfolg
beweist — nicht nur die, die den Fehler zeigen würde.

**Was bleibt.** Der `MandantKontext` hat noch keine Implementierung; sie
kommt mit dem Header-Platzhalter in Schritt D. Und das Gate der ADR — der
Isolationstest — kommt mit der ersten Tabelle in Schritt B. Bis dahin ist
ADR-002 eine Entscheidung mit angekündigtem Beweis.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt ein Tag —
davon die Hälfte für die Frage, warum `-pl` ohne `-am` nicht auflöst.

---

## 2026-09-10 — Stufe 1, das Gate der ADR-002

**Was delegiert wurde:** Schritt B der Anwendungsschicht: Postgres über
Testcontainers, Flyway-Migration mit Row Level Security, ein
Transaktionsmanager, der den Mandanten je Transaktion setzt, und der
Isolationstest. 158 Tests grün, davon 6 neu — und die sechs haben die meiste
Arbeit gemacht.

**Was die Testsuite abgefangen hat.** Vier Fehler, jeder eine eigene Lehre,
in der Reihenfolge, in der sie auftraten:

1. *„Could not find a valid Docker environment"* — aber Docker lief. Docker
   29 verlangt mindestens API 1.40; docker-java handelt ohne Vorgabe eine
   ältere aus, der Daemon antwortet mit 400, und Testcontainers liest den
   Fehlerkörper als leere Info-Struktur. Die Meldung sagt „kein Docker", die
   Ursache ist „falsche Version". Die Eigenschaft muss in die geforkte
   Test-JVM; eine Umgebungsvariable liest docker-java dafür nicht. Steht jetzt
   im Pom, mit Begründung.
2. *Connection refused* auf einem Port, den es nicht mehr gab. `@Container`
   startet und stoppt je Testklasse, Spring cacht den Kontext über Klassen
   hinweg. Die zweite Klasse fand einen Kontext mit der URL eines toten
   Containers. Ein Container je JVM, einmal gestartet, nie von Hand gestoppt.
3. *„new row violates row-level security policy"* beim Schreiben als eigener
   Mandant — die Policy griff also, aber der Mandant fehlte. Eine Sonde zeigte:
   dieselbe Verbindung, richtige Rolle, Variable gesetzt. Erst die Ergebnisse
   je Methode verrieten die Kette: Der Test „ohne Mandant beginnt keine
   Transaktion" lief zuerst, und mein `doBegin` warf *nach* `super.doBegin()`.
   Der EntityManager blieb am Thread gebunden; der nächste Test hielt ihn für
   seine Transaktion, `doBegin` lief nie, kein Mandant, Policy verletzt, alles
   danach „transaction aborted". Der Test, der Lautstärke prüfen sollte,
   brachte die fünf anderen zum Schweigen. Jetzt wird der Mandant *vor* dem
   Beginn geholt, und ein Fehler danach räumt auf wie Spring selbst.
4. Zählungen 3 und 4 statt 1 — keine Lücke, sondern Zeilen, die sich über
   die Tests ansammelten. Aufräumen als jeder Mandant selbst; die Policy
   begrenzt auch das Löschen.

**Was die Gegenproben belegen.** Policy abgeschaltet: A sieht die Zeile von
B, und `WITH CHECK` schweigt — fünf Tests rot. `set_config` auf einen anderen
Namen umgebogen: alle sechs rot, weil die Datenbank dann auch die eigenen
Zeilen sperrt. Das ist der Satz aus der ADR, gemessen: Die vergessene
WHERE-Klausel fällt geschlossen aus, nicht offen.

**Was an diesem Schritt hängen blieb.** Zwei Rollen, wie im Betrieb: Flyway
als Superuser, die Anwendung als `aptum_app`, angelegt vom Init-Skript des
Containers. Wer hier den Container-Benutzer nähme, bekäme grüne Tests, die
nichts prüfen — Superuser sehen jede Zeile. Das steht im Test, damit es
niemand vereinfacht.

**Was die Filter gelehrt haben, zum vierten Mal heute.** Der Rücklauf nach den
Gegenproben gab nichts aus, weil mein Filter nur Fehlerzeilen zeigte. Ein
stummes Grün ist kein Grün. Der letzte Lauf greift jetzt die Erfolgszeile ab,
nicht die Abwesenheit einer Fehlerzeile.

**Zeitschätzung:** delegiert gut zwei Stunden, von Hand geschätzt drei Tage —
davon zwei für Fehler drei, weil die Meldung „Policy verletzt" auf die
Datenbank zeigt und die Ursache im Transaktionsmanager lag.

---

## 2026-09-10 — Stufe 1, die Adapter

**Was delegiert wurde:** Schritt C — Ports im Domänenkern, JPA-Entities und
Adapter im Infrastruktur-Modul, eine zweite Migration, die Stammdaten der
Testpraxis, und vier Tests, die hin und zurück durch die Tabellen gehen. 162
Tests grün, davon 4 neu.

**Zwei Entscheidungen, benannt statt versteckt.** Die Mandanten-ID füllt die
Datenbank selbst: `default current_setting('app.mandant_id', true)` an jeder
Spalte. Kein Adapter, keine Entity kennt sie — die Persistenz weiß es, die
Domäne nicht, so wörtlich wie ADR-002 es formuliert. Und Personen, Räume und
Praxiseinstellung bleiben Stammdaten im Code, als Testpraxis „T. Alpha" und
„T. Beta". Eine Stammdatenpflege wäre ein Formular über einer Tabelle und
belegte nichts, was die Terminsuche nicht schon belegt.

**Was die Testsuite abgefangen hat.** `speichere` rief `persist` auf, bevor
die Felder gefüllt waren, und der Flush im Verlauf schrieb die leere Zeile.
Hibernate meldete das Pflichtfeld. Erst füllen, dann persistieren — der
Kommentar steht jetzt an der Stelle, damit der nächste, der die Reihenfolge
„aufräumt", weiß, warum sie so ist.

**ADR-001 hat eine Behauptung weniger.** *JPA-Entities werden nie als
API-DTO durchgereicht* stand seit Stufe 0 in der ADR und im Skill. Jetzt
prüft es ArchUnit: Nichts außerhalb des Persistenz-Pakets darf eine Klasse
mit `@Entity` kennen. Die Entities sind zudem paketprivat — der Compiler
verbietet den Zugriff ohnehin. Die Regel greift erst, wenn jemand eine Entity
öffentlich macht, und genau das ist der Moment, in dem sie gebraucht wird.

**Was die Gegenprobe an mir gelehrt hat — fünfter Fall heute.** Die erste
Sonde hing an einer Hilfsklasse im Persistenz-Paket, nicht an der Entity
selbst; die Regel blieb zu Recht still, und ich hätte das beinahe als
Beweis gelesen. Die zweite Sonde machte die Entity öffentlich und gab sie aus
einer Klasse außerhalb zurück — zwei Verletzungen, mit dem ADR-Text im
Fehler. Eine Sonde prüft nur das, was sie tatsächlich tut, nicht das, was
sie prüfen soll.

**Zeitschätzung:** delegiert gut eine Stunde, von Hand geschätzt anderthalb
Tage — die Übersetzung von Hand kostet Zeilen, die ein Werkzeug spart, und
bringt, dass eine Spaltenumbenennung hier auffällt und nicht in einem
Controller.

---

## 2026-09-10 — Stufe 1, die Schnittstelle

**Was delegiert wurde:** Schritt D — drei Anwendungsfälle ohne Framework,
ein Filter für den Mandanten, zwei Controller mit eigenen DTOs, und ein Test,
der die ganze Strecke über HTTP fährt: anlegen, suchen, buchen, noch einmal
buchen. 169 Tests grün, davon 7 neu. Zum ersten Mal lässt sich das Projekt
starten und mit `curl` befragen.

**Der Satz aus CLAUDE.md, an der Stelle, an der er zählt.** Regel 3 — *der
AI-Layer schlägt vor, die Domäne entscheidet* — hat jetzt ihren Ort:
`TerminBuchen`. Ein Vorschlag aus der Suche, eine Buchung von Hand und später
ein Vorschlag eines Sprachmodells landen alle dort, und dort fragt niemand,
woher der Termin kommt. In der Gegenprobe das Regelwerk übersprungen: Die
Doppelbuchung wurde zu 201, die Übersteuerung zu einem stillen FREI. Zwei
Tests rot, genau die beiden.

**Was bewusst ein Platzhalter ist, und so heißt.** Der Mandant kommt aus der
Kopfzeile `X-Mandant`, die jeder setzen kann. Das ist kein
Sicherheitsmechanismus, sondern der Stub, der die Kette Anfrage → Kontext →
Transaktion → Policy einmal vollständig laufen lässt. Er steht als Punkt 8 in
den offenen Punkten, und der Filter sagt es in seinem Kopfkommentar selbst.
Ohne Kopfzeile gibt es 400 — keinen Standardmandanten.

**Was am Zuschnitt auffiel.** Die Höchstmenge greift beim Erfassen, nicht
beim Buchen; `VerordnungAnlegen` ist deshalb der erste Anwendungsfall, der
nur eine Regel kennt. Und die Buchung schreibt zweimal: den Termin in den
Kalender und die Behandlung auf die Verordnung. Ohne das zweite wüsste die
nächste Suche nichts vom Restkontingent — der Fehler wäre erst beim sechsten
Termin aufgefallen, als gebuchte siebte Einheit.

**Was nicht funktionierte.** Nichts am Code — der Build war auf Anhieb grün,
was nach den vier Fehlern in Schritt B fast verdächtig war. Zwei Kleinigkeiten
daneben: Ein neuer Abschnitt in den offenen Punkten stand vor dem vorherigen,
und die Skript-Gates liefen einmal im falschen Verzeichnis, weil ein
paralleler Maven-Aufruf es gewechselt hatte. Beides beim Hinsehen gefunden,
beides ohne Folgen.

**Zeitschätzung:** delegiert etwa eine Stunde, von Hand geschätzt zwei Tage.

---

## 2026-09-10 — Nachtrag: der erste echte Lauf

**Was delegiert wurde:** Die Behauptung „nachspielbar mit `curl`" aus dem
README einlösen — Postgres im Docker, das Jar, die Anwendung außerhalb jeder
Testumgebung, die Strecke von Hand.

**Was beim ersten Versuch still scheiterte.** Alles: kein Health, kein
Ergebnis, `000` auf jeder Anfrage. Mein Skript hatte die Fehler unterdrückt.
Die Ursache lag ganz vorn: Der Volume-Mount für das Rollen-Skript nimmt unter
Git Bash einen umgeschriebenen Pfad und schweigt dazu. Ohne Rolle scheiterte
Flyway V1 beim `GRANT`, und die Anwendung kam nie hoch. Die Kette war
vollständig lesbar — im Anwendungslog, das ich beim ersten Mal nicht gelesen
hatte.

Zweiter Versuch mit sichtbaren Fehlern und der Rolle per `docker exec`:
Health in zwei Sekunden, 56 Vorschläge, 28 ausgeschlossen mit Grund, Buchung
201, Doppelbuchung 409 mit beiden verletzten Regeln, fremder Mandant 404, ohne
Kopfzeile 400. Der Ablauf steht jetzt im README unter „Starten", mit dem
Hinweis auf den Mount.

**Die Lehre, zum wiederholten Mal in anderer Form.** Ein Skript, das
`>/dev/null` an jeden Schritt hängt und am Ende nur Ergebnisse druckt,
verwandelt einen Fehler in der ersten Zeile in eine Reihe leerer Ausgaben.
Der Unterschied zwischen „läuft nicht" und „läuft, aber falsch" ist beim
Lesen des ersten Fehlers zu sehen, nicht beim Lesen des letzten.

---

## 2026-09-10 — Nachtrag: OpenAPI, und eine Durchsicht der Doku

**Was delegiert wurde:** Die Frage, ob alles aktuell ist — und das
OpenAPI-Dokument, damit SA6 belegt ist. 171 Tests grün, davon 2 neu.

**Was die Durchsicht ergeben hat.** `PIPELINE.md` war an fünf Stellen
veraltet: „solange `services/` leer ist", „unter zwei Sekunden, weil dieses
Repo aus Text besteht", „vier Jobs" bei fünf, „Ab Stufe 1 kommen Kompilieren,
Domain-Tests und ArchUnit dazu" — alles Sätze, die am Morgen stimmten und am
Abend nicht mehr. Dazu vier Zeilen im Anforderungs-Mapping, die auf „in
Arbeit" standen, obwohl der Beleg da war: Java, KI-Einsatz, Agenten und
Skills, REST. Hochgestuft mit dem konkreten Pfad, nicht mit dem Ordner.

Das Muster ist dasselbe wie bei den 31 Kontrastpaaren, nur in Sätzen statt
Zahlen: Prosa über den Stand des Repos veraltet in dem Moment, in dem sich
der Stand ändert, und kein Gate liest Prosa. Die Durchsicht am Ende eines
Tages ist deshalb kein Aufräumen, sondern Teil der Arbeit.

**Das OpenAPI-Dokument ist erzeugt, nicht geschrieben.** springdoc liest die
Controller, ein Customizer hängt den `X-Mandant`-Header an jede Operation —
einmal, an einer Stelle, wie im Code. Das Dokument liegt unter
`docs/api/openapi.json`, und ein Test vergleicht es mit dem, was die
laufende Anwendung liefert. Sonst wäre es die nächste Datei, die still
veraltet. Gegenprobe: einen Pfad im Controller umbenannt, Test rot, mit dem
Befehl zum Aktualisieren in der Meldung.

**Was die Testsuite abgefangen hat.** Der erste Vergleich schlug fehl,
obwohl nichts geändert war: springdoc trägt unter `servers` die URL mit dem
zufälligen Port des Testlaufs ein. Der Eintrag ist keine Eigenschaft der
Schnittstelle und wird vor dem Vergleich entfernt. Ein Drift-Gate, das bei
jedem Lauf Drift meldet, wäre nach einem Tag abgeschaltet.

**Zeitschätzung:** delegiert etwa vierzig Minuten, von Hand geschätzt ein
Nachmittag — wie angekündigt.

---

## 2026-09-10 — Stufe 2, das Gerüst

**Was delegiert wurde:** ADR-003 und das Angular-Gerüst gegen
`docs/api/openapi.json`. Dazu die Frage, ob das Kalender-Grid mit Kendo
gebaut werden kann.

**Die Kendo-Frage.** Die ehrliche Antwort ist: für die Terminsuche und die
Abrechnungstabelle ja, für das Grid nein. Der Kendo Scheduler ist gut, aber
er bringt sein eigenes DOM mit, und die Zusagen aus dem Skill `a11y-grid` —
Roving Tabindex, eine Live-Region pro Bewegung, sechs Slot-Zustände aus den
Token — lassen sich in fremdem DOM weder prüfen noch belegen. Ein Grid, das
„barrierefrei laut Hersteller" ist, wäre im Anforderungs-Mapping eine
Behauptung. Selbst gebaut auf dem CDK ist es ein Beleg. Die Lizenzfrage kam
erst als drittes Argument. Steht so in ADR-003, inklusive der Alternativen,
die es verloren haben.

**Was gut lief.** Der Weg vom OpenAPI-Dokument zu TypeScript-Typen ist ein
Befehl, `openapi-typescript`, und die Typen sind eingecheckt. Die CI erzeugt
sie neu und vergleicht per `git diff` — dasselbe Drift-Gate wie im Backend,
eine Ebene weiter. Ändert jemand einen Controller, wird der Backend-Test rot;
aktualisiert er das Dokument, ohne die Typen zu erzeugen, wird das Frontend
rot. Kein Schritt dazwischen, an dem etwas still veraltet.

**Was nicht funktionierte.** Zwei Werkzeuge haben Dateien angefasst, die sie
nichts angehen. Prettier hat `tokens.css` umformatiert: die Spalten waren von
Hand ausgerichtet, damit man Farbwerte nebeneinander lesen kann, und die
`@kontrast`-Zeilen sind Prosa für ein Gate. ESLint wollte die erzeugte
`schema.d.ts` nach Stilregeln umschreiben. Beides ist derselbe Fehler:
Werkzeuge, die auf alles losgehen, was sie finden. Beide Dateien sind jetzt
ausgenommen, mit dem Grund als Kommentar — die erzeugte Datei wird geprüft,
nicht gelintet. Und `ng add angular-eslint` ohne Versionsangabe holte eine
Fassung, die nicht zu Angular 20 passte; erst mit `@20` lief es.

**Was die Testsuite abgefangen hat.** Nichts Fachliches — das Gerüst hat
einen Test, der prüft, dass die Wortmarke da ist und die Navigation einen
Namen hat. Das ist bewusst dünn. Die Terminsuche ist die erste Seite, die
etwas tut, und dort beginnen die Tests, die etwas abfangen können.

**Zeitschätzung:** delegiert etwa eine Stunde, davon die Hälfte für das
Kendo-Argument und die zwei Ausnahmen. Von Hand ein halber Tag.

---

## 2026-09-10 — Stufe 1 abgeschlossen: die Terminsuche

**Was delegiert wurde:** Die erste Seite. Formular nach den vier Dimensionen,
der Suchflow aus dem Skill `angular-rxjs`, Vorschläge mit den ausgeschlossenen
Slots und ihrem Grund. 11 Frontend-Tests grün, davon einer mit axe-core.

**Der Skill hatte eine Referenz, die es nicht gab.** `referenzen/slot-suche.ts`
stand seit Stufe 0 im Skill, die Datei nie im Repo. Jetzt zeigt der Skill auf
den echten Service. Das ist besser als eine Beispieldatei neben dem Code: Die
würde beim nächsten Umbau veralten, und kein Gate liest Beispiele.

**Was gut lief.** Der Suchflow ist eine Kette von acht Zeilen, und jede Zusage
hat ihren eigenen Test: drei Eingaben in 300 ms sind eine Anfrage; die zweite
Anfrage bricht die erste ab (`cancelled` ist wahr); nach einem Fehler
funktioniert die nächste Suche; eine 503 wird mit Abstand wiederholt, eine
400 nicht — dieselbe Anfrage noch einmal ändert an einer falschen Kennung
nichts. Die Komponente hält keinen Zustand und kennt kein `subscribe`. Das
Formular ist ein Strom, der Service macht daraus Zustände, `toSignal` hält
den letzten.

Die zweite und dritte Dimension sind auf der Seite bewusst *kein* Filter.
Wer das Heilmittel abrechnen darf, entscheidet die Regel Qualifikation, den
Raum die Raumanforderung des Heilmittels. Die Seite sagt das so und zeigt
rechts, was dadurch weggefallen ist. Das ist der Moment aus dem
Wireframe-Auftrag, in dem das Produkt seine Fachlogik zeigt.

**Was nicht funktionierte.** Der axe-Test hing fünf Sekunden und starb am
Timeout: `axe.run` arbeitet mit echten Promises und Timern, `fakeAsync`
friert genau die ein. Der Test läuft jetzt mit echten Timern und wartet die
Entprellung real ab. Dann die Gegenprobe, bevor „keine Verstöße" als Beleg
zählt: ein Eingabefeld ohne Beschriftung in einem Wegwerftest — axe meldet
`label`. Erst danach ist grün eine Aussage.

Zweitens hat der Produktions-Build etwas gefunden, das der Testlauf
durchließ: `track v.beginn + v.therapeut` mit zwei optionalen Feldern aus dem
OpenAPI-Dokument. Die erzeugten Typen sagen ehrlich, dass springdoc nichts
als Pflichtfeld kennzeichnet — und der Build nimmt sie beim Wort.

**Was die CI abgefangen hat, was lokal grün war.** Die eingecheckten
API-Typen waren vor dem Prettier-Ausschluss noch einmal formatiert worden —
einfache Anführungszeichen, zwei Leerzeichen. Lokal fiel das nicht auf, weil
das Drift-Gate nur in der CI läuft. Dort erzeugt `openapi-typescript` die
rohe Fassung, und `git diff` meldet 508 geänderte Zeilen, von denen keine
eine Typänderung ist. Nachgewiesen, bevor ich das behaupte: beide Fassungen
ohne Whitespace und mit vereinheitlichten Anführungszeichen haben dieselbe
Prüfsumme. Erzeugte Dateien werden eingecheckt, wie sie erzeugt werden —
das Gate fragt nicht, ob eine Änderung nur hübsch ist.

**Und ein zweites Mal rot, diesmal die Zeitzone.** Lokal 09:00, in der CI
08:00: `DatePipe` formatiert in der Zone des Browsers, und der Runner steht
auf UTC. Der Fehler wäre auch bei einer Praxis aufgefallen, deren Rechner
falsch eingestellt ist — die Uhrzeit eines Termins ist die der Praxis, nicht
die des Geräts. Das Backend schreibt den Offset ohnehin in jeden Wert; die
Anzeige nimmt ihn jetzt von dort. Der Test liefert bewusst `+05:00` und
erwartet 09:00 — so besteht er nur, wenn die Zone aus dem Wert kommt, egal
wo er läuft. Gegenprobe ohne den Fix: 05:00, rot. Ein `TZ=UTC` vor dem
Testbefehl hätte unter Windows nichts bewiesen; Chrome liest die Zone vom
System, nicht aus der Umgebung.

**Was die Testsuite abgefangen hat.** Nichts Fachliches; das liegt im
Backend. Was der Suchflow abfängt, ist Zeit: Race Conditions und tote
Streams sind Fehler, die im Browser nie reproduzierbar auftreten und im
Test mit `tick(300)` jedes Mal.

**Zeitschätzung:** delegiert etwa eineinhalb Stunden. Von Hand ein Tag,
wovon der halbe für die Tests des Suchflows draufginge — die man dann
weglässt.

---

## 2026-09-10 — Stufe 2, Schritt 1: die Woche aus dem Backend

**Was delegiert wurde:** `GET /kalender/woche` — die Daten fürs Gitter, bevor
es das Gitter gibt. 178 Tests grün, davon 7 neu.

**Was gut lief.** Die Wochenansicht rechnet nichts Neues. Was ein Termin für
die Therapeutin belegt und was er für den Raum belegt, sagt `Termin` seit
der Slot-Suche; die Ansicht macht daraus Blöcke — Rüstzeit, Behandlung,
Rüstzeit, und die Nachruhe, wenn das Heilmittel eine vorsieht. Dadurch kann
das Gitter Rüstzeit und Nachruhe gar nicht anders zeichnen, als die Suche
sie rechnet. Der Test dafür vergleicht die Blockgrenzen mit
`belegtTherapeutin` und `belegtRaum`, nicht mit eigenen Zahlen. Frei wird
nicht geliefert: Frei ist, was übrig bleibt.

**Was nicht funktionierte.** Der neue Endpunkt antwortete 400 auf
`?tag=2026-03-04`, und das OpenAPI-Dokument nannte den Parameter `arg0`.
Beides derselbe Grund: Der Compiler behält Parameternamen nur mit
`-parameters`, der Spring-Boot-Parent setzt das still, das BOM aus ADR-008
nicht. Ein Flag im Compiler-Plugin, mit Begründung als Kommentar — nicht
`@RequestParam("tag")` an jeder Stelle, an der es sonst wieder passiert.
Dann die zweite Runde: Nach der Pom-Änderung hatte Maven nicht neu
übersetzt, das Flag war da und die Klasse unverändert. `javap` zeigte
keine `MethodParameters`; erst `clean` half.

Drittens ein falscher Test: „genau ein belegter Block" — aber die anderen
Tests der Klasse buchen in derselben Woche für denselben Mandanten, und die
Klasse räumt nicht auf. Drei Blöcke, rot. Der Test sucht jetzt den Block,
der zu seiner Buchung gehört, und prüft die Rüstzeiten direkt daneben. Das
ist die präzisere Behauptung; die Zählung war nur die bequemere.

**Was die Testsuite abgefangen hat.** Das Drift-Gate hat seine Arbeit
getan: `OpenApiTest` rot, weil ein Pfad dazukam; nach dem Aktualisieren
`api:types` im Frontend, drei neue Typen, elf Tests unverändert grün.

**Zeitschätzung:** delegiert etwa eine Stunde, die Hälfte davon für
`arg0`. Von Hand ein halber Tag.

---

## 2026-09-10 — Stufe 2, Schritt 2: das Kalender-Grid

**Was delegiert wurde:** Das Grid selbst, die Seite drum herum, 16 neue
Frontend-Tests, davon zwei mit axe. 27 grün.

**Was gut lief.** Die Trennung in drei Teile, die je für sich testbar sind.
`raster.ts` macht aus der Woche des Backends reine Daten — je Viertelstunde
und Therapeutin eine Zelle mit Zustand, Text und zugänglichem Namen. Ohne
eine einzige Datumsrechnung: Das Backend liefert Wandzeit der Praxis, und
ISO-Zeitstempel sind als Text vergleichbar. Nach dem Zeitzonen-Fund vom
Vormittag war das kein Stil, sondern Vorsicht. `kalender-grid` weiß nichts
von Belegungen, nur von Zellen, Fokus und Tasten. Die Seite lädt, blättert
und zeigt die Legende. Der Test für das Grid drückt Tasten und liest, welche
Zelle danach `tabindex="0"` trägt — das ist der Roving Tabindex als
Behauptung, die kaputtgehen kann.

Frei liefert das Backend nicht. Das ist der Satz, den ich mir beim Schreiben
am öftesten gesagt habe: Frei ist, was übrig bleibt, und das weiß jedes
Gitter selbst.

**Was nicht funktionierte.** Der Lint mit den Template-Regeln zur
Barrierefreiheit hat den ersten Entwurf abgelehnt: `(click)` auf der Zelle
ohne Tastatur-Handler. Der lag auf dem Host-Element — für den Lint
unsichtbar, für den Browser gleichwertig. Ich hätte die Regel für diese
Zeile abschalten können; stattdessen liegt `keydown` jetzt auf der Zelle,
und der Test schickt die Taste dorthin, wo sie im Browser ankommt: an die
Zelle mit dem Fokus. Das ist der bessere Test.

Der Beleg-Check hat danach zwei Pfade im Anforderungs-Mapping abgelehnt:
`domain/kalender/` und `GET /kalender/woche` — beides keine Dateien. Nur
Dateien sind Belege. Ich hatte es bequem geschrieben; das Gate hat es nicht
durchgelassen.

**Was der Audit-Agent gefunden hat, was axe nicht sieht.** Vor dem Commit
lief der `a11y-auditor` aus `.claude/agents`, wie er es für jede Änderung
am Grid verlangt. Drei Befunde, alle drei an axe vorbei: Die Tagesreiter
hatten Roving Tabindex ohne Pfeilsteuerung — inaktive Reiter mit
`tabindex=-1` und kein `keydown`, per Tastatur unerreichbar, WCAG 2.1.1.
Die beiden axe-Tests der Seite waren grün. `aria-selected` auf den
Zellen war an den Fokus gekoppelt, nicht an eine Auswahl; ein Screenreader
hätte bei jedem Pfeildruck „ausgewählt" gesagt. Und die Ansage „nicht
buchbar" war höflich statt sofort — Antwort auf eine Aktion, nicht
Hintergrund. Alle drei behoben, für die Reiter mit Test. Die Lehre ist die
aus dem Skill: axe prüft Struktur, nicht Verhalten. Ein grüner axe-Lauf
belegt, dass nichts Falsches im DOM steht — nicht, dass man hinkommt.

**Was die Testsuite abgefangen hat.** Nichts Fachliches — und das ist
diesmal die Pointe. Die Zustände im Gitter sind die Blöcke aus
`Wochenansicht`, und die nimmt ihre Grenzen aus `Termin`. Ob Rüstzeit und
Nachruhe richtig liegen, ist in der Domäne getestet, nicht im Frontend.

**Zeitschätzung:** delegiert etwa zwei Stunden. Von Hand zwei Tage, wovon
ein halber für die Tastaturnavigation und ihre Tests draufginge.

---

## 2026-09-10 — Stufe 2, Schritt 3: Ende-zu-Ende mit axe

**Was delegiert wurde:** Playwright gegen Backend und Frontend zusammen, im
echten Chromium, als CI-Job `e2e`. Sechs Tests: über die API buchen, den
Termin im Grid finden, mit der Tastatur hindurchfahren, die Reiter, die
Suche, und axe über beide Seiten.

**Was der echte Browser gefunden hat, was 29 Unit-Tests nicht fanden.** Zwei
Dinge, beide in der ersten Viertelstunde.

Erstens die Wegkreuzung: Die Seite heißt `/kalender`, der API-Pfad auch.
Kaum stand `/kalender` im Dev-Proxy, ging der Seitenaufruf ans Backend statt
an Angular. Der Proxy kennt jetzt `/kalender/woche`, nicht das Präfix. Das
ist kein Testproblem, das wäre beim ersten `npm start` mit Backend jedem
aufgefallen — nur hatte das bis dahin niemand getan. Genau der Moment, den
ich am Nachmittag als fehlend benannt habe: das Ding einmal selbst in der
Hand halten. Der e2e-Job tut das jetzt bei jedem Lauf.

Zweitens ein fachlicher Fehler im Raster. Die Rüstzeit beträgt fünf
Minuten, das Raster fünfzehn. Eine Zelle nahm die Belegung, die ihren
Anfang deckt — 08:55 bis 09:00 deckt 08:45 nicht, also war die Zelle frei,
und die Rüstzeit war im Gitter unsichtbar. Der Unit-Test hatte das sogar
als „fachlich richtig" festgeschrieben; ich hatte den Fall gesehen und
falsch beurteilt. Der e2e-Test suchte den Block „Rüstzeit, Vorbereitung"
und fand ihn nicht. Jetzt zeigt eine Zelle, was sie berührt, mit Vorrang
Behandlung vor Nachruhe vor Rüstzeit — sonst sähe das Gitter den Termin
kürzer, als die Suche ihn rechnet, und das ist genau der Widerspruch, den
die Wochenansicht vermeiden sollte.

**Was gut lief.** Der Job startet Postgres als Service, baut das Jar, legt
die Anwendungsrolle an wie im Isolationstest, wartet auf `/actuator/health`
und lässt Playwright den Dev-Server mit Proxy hochfahren. Die Tests bauen
ihre Daten über die API auf, keine Fixtures. Ein Query-Parameter `?tag=`
an der Kalenderseite, damit ein Test in eine bekannte Woche springt — und
damit gibt es nebenbei Deep-Links.

**Zeitschätzung:** delegiert etwa eine Stunde. Von Hand ein Tag, weil der
Start des Backends im CI-Job das ist, was man drei Mal falsch macht.

---

## 2026-09-10 — Stufe 2, Schritt 4: der Buchungsdialog

**Was delegiert wurde:** Der wichtigste Screen laut Wireframe — die
Regelprüfung als benannte Liste, bevor jemand entscheidet. 35 Frontend-Tests,
davon sechs für den Dialog; 179 im Backend.

**Der Schnitt im Backend zuerst.** Der Wireframe zeigt die Regeln, bevor
jemand auf „Buchen" drückt; das Backend lieferte sie erst mit der Buchung.
`TerminBuchen` prüfte und buchte in einer Methode. Die Prüfung ist jetzt
herausgezogen, `pruefen` liefert dieselbe Entscheidung ohne zu speichern,
`POST /termine/pruefung` gibt denselben Körper wie die Buchung — kein neues
Schema, kein zweiter Weg. Der REST-Test prüft zweimal denselben Slot und
erwartet zweimal „frei": Geprüft ist nicht gebucht. Nebenbei ist das genau
der Aufruf, den der KI-Layer in Stufe 3 braucht: jeden Vorschlag eines
Sprachmodells prüfen, bevor er überhaupt angezeigt wird.

**Was gut lief.** Der CDK-Dialog bringt mit, was der Skill verlangt und was
fast immer vergessen wird: Focus Trap, Escape, Fokus zurück auf den Auslöser.
Der e2e-Test drückt Escape und prüft, dass der Fokus wieder auf „Buchen"
liegt. `aria-modal` steht ausdrücklich in der Konfiguration, nachdem der
erste Unit-Test es als `false` fand — im CDK ist es nicht der Standard.
Die drei Ausgänge tragen je Farbe und Wort, aus den Regel-Token. Blockiert
sperrt den Knopf, bis eine Begründung von zehn Zeichen steht; dann heißt
er „Trotzdem buchen" und die Übersteuerung geht mit — ADR-009 als
Bedienelement. Eine 409 beim Buchen ist im Dialog kein Fehler, sondern die
Liste mit der verletzten Regel und dem Begründungsfeld.

Nach der Buchung sucht die Seite neu, damit der verbrauchte Vorschlag
verschwindet — ohne die Entprellung zu umgehen: Der Service nimmt einen
zweiten Strom `erneut` und wiederholt damit die letzte Suche.

**Was nicht funktionierte.** Der erste Entwurf schloss den Dialog aus einem
`computed` heraus — eine Nebenwirkung in einer Berechnung. Ist jetzt ein
`effect`. Der Build meldete das Budget für Komponenten-CSS (4 kB) als
überschritten: drei Komponenten trugen dieselben Blöcke für `.zahl`,
`.still` und `.fehler`. Einmal global, und alle drei sind kleiner. Und der
e2e-Test behauptete zuletzt, nach der Buchung sei „der erste Vorschlag ein
anderer" — verglich aber nur Datum und Uhrzeit. Nach der Buchung bei
T. Alpha um 08:15 war der erste Vorschlag T. Beta um 08:15. Der Code war
richtig, die Behauptung zu grob; sie vergleicht jetzt den ganzen Vorschlag
und wartet auf die neue Suche.

**Zwei Lehren über e2e-Tests gegen ein echtes Backend.** Playwright lässt
Dateien in mehreren Workern laufen; zwei Tests buchten sich in derselben
Woche die Slots weg, und einer sah eine 409, die keiner geschrieben hatte.
Ein Backend, ein Worker — steht als Kommentar in der Konfiguration. Und
nach dem zehnten Lauf gegen dieselbe lokale Datenbank war die Zelle, die
der Tastaturtest „als frei kannte", nicht mehr frei: Die Rüstzeit einer
früheren Buchung berührte sie. In der CI mit frischer Datenbank wäre das
nie aufgefallen — und irgendwann doch. Der Test sucht jetzt die erste freie
Zelle, statt sie zu kennen. Tests gegen echte Daten dürfen keine Daten
kennen, nur Eigenschaften.

**Zeitschätzung:** delegiert etwa anderthalb Stunden. Von Hand ein Tag,
und der Fokus käme nicht zurück auf den Auslöser.

---

## 2026-09-10 — Stufe 3, Schritt 1: der AI-Layer als Dienst

**Was delegiert wurde:** `services/ai-assist/` in Python — die
Verordnungserfassung aus Freitext, wie der Skill `llm-evals` sie als
Feature 1 festlegt. 15 Tests, ohne Schlüssel, ohne Netz.

**Die Entscheidung davor.** Python statt Java für den zweiten Dienst, mit
drei Gründen, die alle in der Ausschreibung stehen: „Java oder Python“
belegt beides, Anthropic- und MCP-SDK sind dort die Referenz, und zwei
Dienste in zwei Sprachen, die nur über den OpenAPI-Vertrag reden, sind der
ehrlichere Beleg für Microservices als zwei Maven-Module. Der Preis: ein
dritter Werkzeugkasten in der CI — uv, Ruff, mypy, pytest. Ein Job, zwanzig
Zeilen.

**Was gut lief.** Die Pipeline ist vier benannte Schritte, und der erste ist
die Pseudonymisierung — Regel 2 aus `DATENSCHUTZ.md` als Funktion mit
Test, nicht als Absatz. Der Test dafür ist der, den ich am liebsten mag:
Der aufgezeichnete Provider kennt *nur* die pseudonymisierte Fassung des
Textes. Bekäme er den Originaltext, gäbe es keine Aufzeichnung, und der
Test wäre rot. So ist „das Modell sieht keinen Namen“ eine Zusage, die
kaputtgehen kann.

Structured Output über Tool-Use, bei beiden Providern mit demselben
Schema aus demselben Pydantic-Modell — ein Schema, nicht drei. Und die
Regel, die den Unterschied zu einer Demo macht: Ein fehlendes oder
widersprüchliches Feld heißt „nicht extrahierbar“, nie ein geratener
Wert. Die Pipeline setzt das durch, auch wenn das Modell es vergisst.

**Was noch nicht stimmt, und ehrlich gesagt wird.** Kein Schlüssel, also
kein echter Aufruf. Die 15 Tests prüfen die Pipeline, nicht das Modell.
Ob der Prompt gut ist, weiß erst der Eval — Schritt 2. Die
Pseudonymisierung erkennt vier Arten von Angaben mit regulären
Ausdrücken; das steht so in ihrem Kopfkommentar und im README des
Dienstes.

**Was nicht funktionierte.** Nichts am Code — Ruff, mypy strict und pytest
waren nach einer Runde grün. Aber zweimal ist mir ein Python-Skript zum
Bearbeiten der Doku an typografischen Anführungszeichen in der Shell
gestorben. Skripte in eine Datei, dann ausführen. Kleine Lehre, zwei Mal
gelernt.

**Zeitschätzung:** delegiert etwa eine Stunde. Von Hand ein Tag, und die
Pseudonymisierung wäre „später“.

---

## 2026-09-11 — Stufe 3, Schritt 2: die Evals

**Was delegiert wurde:** Die Eval-Suite nach dem Skill `llm-evals`: 55 Fälle
mit `warum`, ein Runner, der feldweise misst und Regressionen einzeln nennt,
ein Test für den Runner, und der CI-Job, der ohne Schlüssel sichtbar
überspringt. 20 Python-Tests.

**Die Fälle.** Absichtlich unausgewogen: vier Normalfälle, der Rest
Grenzfälle — KG ohne Zusatz, KGG gegen KG-Gruppe, MLD ohne Umfang, ZNS
ohne Altersangabe, WS2, ein ICD-Code statt einer Diagnosegruppe, Menge und
Frequenz vertauscht, „mindestens 2x“ ohne Obergrenze, alle 14 Tage, der
31. Februar, ein leeres Ankreuzfeld. Bei vielen davon ist die richtige
Antwort „nicht extrahierbar“, und genau das ist der Punkt: Ein Modell,
das bei fehlender Diagnosegruppe WS rät, weil WS die häufigste ist, hat
eine fachliche Entscheidung getroffen, die ihm nicht zusteht. Das `warum`
je Fall ist Pflicht; der Runner lehnt einen Fall ohne ab.

**Der Runner ohne Modell getestet.** Der Test baut aus den Erwartungen eine
Aufzeichnung, lässt die Suite dagegen laufen und prüft, dass ein
einzelnes falsches Feld genau dieses Feld rot macht — und dass ein Fall,
der vorher grün war und jetzt rot ist, als Regression gemeldet wird, in
der Gegenrichtung aber nicht. Trockenlauf gegen die perfekte Aufzeichnung:
55 von 55 grün. Das beweist nichts über das Modell und alles über den
Runner.

**Was ehrlich gesagt wird.** Kein Schlüssel, also kein echter Lauf, also
keine Zahl. Unter `evals/ergebnisse/` liegt nichts. Der CI-Job schreibt in
diesem Zustand eine Warnung in den Lauf: „Evals übersprungen, kein
Schlüssel“ — der Skill verbietet stummes Grün, und ein grüner
Eval-Job, der nie ein Modell gesehen hat, wäre genau das. Sobald der
Schlüssel als Secret liegt, läuft die Suite bei jeder Änderung an
Prompt, Schema, Provider oder Fällen, und der erste Lauf wird eingecheckt.

**Was nicht funktionierte.** Der Hook hat die Umschriften in den
`warum`-Texten gefunden — nicht in den Fall-Dateien, die kein Prosa-Check
liest, sondern im Generator dafür. Ich hatte sie absichtlich ohne Umlaute
geschrieben, aus Angst vor der Shell. Falscher Reflex: Skript als Datei,
Umlaute wie überall.

**Zeitschätzung:** delegiert etwa eine Stunde, davon die Hälfte für die
Fälle. Von Hand ein Tag — und es wären zwanzig Fälle, alle normal.

---

## 2026-09-11 — Der erste Eval-Lauf, und was er gezeigt hat

**Was passiert ist:** Der Schlüssel lag als Secret, der Lauf wurde von Hand
angestoßen, 2 Minuten 31 Sekunden gegen `claude-sonnet-5`. 43 von 55
Fällen grün.

**Die Zahl, die zählt, ist nicht 78 Prozent.** Feldweise: Datum 100,
Diagnosegruppe 98, Kennzeichen 98, Frequenz 96, Menge 95, Heilmittel 89.
Und im Heilmittel liegen fünf der zwölf roten Fälle an einer Stelle:
Lymphdrainage nach Dauer (30, 45, 60 Minuten), KGG als Gerätetraining,
ZNS ohne Altersangabe. Das sind keine Modellschwächen — das sind
Zuordnungen, die der Prompt nie genannt hat. Der Skill sagt: Wo Provider
sich unterscheiden, ist der Prompt zu schwach. Hier reicht ein Provider,
um es zu sehen.

**Was der Bericht nicht sagen konnte.** `normal-kg-01` rot bei der Menge —
der Normalfall, „wenn der scheitert, ist alles andere egal“. Nur: Der
Bericht nannte das Feld, nicht den Wert. Hat das Modell 2 gesagt, weil
„2x wöchentlich“ dasteht, oder gar nichts? Das ist eine Lücke im Runner,
und sie ist jetzt zu: Jedes rote Feld zeigt ist und soll. Der nächste
Lauf wird es zeigen.

**Was ich nicht ändere.** `unleserlich-01` erwartet bei „K# 6x, W$“
Heilmittel und Diagnosegruppe leer; das Modell hat vermutlich KG und WS
gelesen. Man könnte die Erwartung lockern — ein Mensch läse es auch so.
Ich lasse sie stehen: Der Fall prüft genau, ob das Modell bei Unsicherheit
schweigt, und bei einer Verordnung ist „vermutlich KG“ kein Wert, den ein
System eintragen sollte. Das Gleiche gilt für das leere Ankreuzfeld.

**Die Iteration.** Vier Regeln im Prompt ergänzt: die Heilmittel-Zuordnungen,
„nimm nicht die größere Zahl“ beim Widerspruch, „mindestens 2x“ ohne
Obergrenze ist nicht extrahierbar, und was ein leeres Ankreuzfeld ist. Nicht
mehr, weil eine Prompt-Änderung, die einen Fall repariert, typischerweise
einen anderen bricht — und genau das misst der nächste Lauf gegen die
Basislinie, die aus dem Bericht von Lauf #30 rekonstruiert ist und so
gekennzeichnet.

**Zeitschätzung:** Auswertung und Iteration eine halbe Stunde. Von Hand:
ein Beispiel im Playground, das zufällig klappt.

---

## 2026-09-11 — Zweiter Eval-Lauf: 53 von 55, und ein stummes Gate

**Was der Lauf gezeigt hat.** Nach vier ergänzten Regeln: Heilmittel von 89
auf 100 Prozent, kein Feld unter 98. Die Lymphdrainage-Zuordnung, KGG, ZNS,
das Ankreuzfeld, der Widerspruch bei der Menge, „mindestens 2x“ — alle
grün. Und `normal-kg-01`, der Normalfall, auch; was das Modell beim ersten
Mal gesagt hatte, weiß ich nicht mehr, aber jetzt stünde es im Bericht.

**Die vorhergesagte Regression.** `heilmittel-zwei-genannt` war in Lauf #30
grün und ist jetzt rot: Menge 6 statt leer. Der Auslöser ist meine neue
Regel 3, „die Zahl mit x ist die Menge“. Nur: Der Text sagt „je 6x“, und
das ist eindeutig 6. Die Regression lag in der Erwartung, nicht im Modell
— die erste Fassung des Falls war zu streng. Geändert, mit dem Grund im
`warum`. Das Heilmittel bleibt bei zwei Nennungen nicht extrahierbar.

**Das stumme Gate.** Der Bericht hat diese Regression nicht gemeldet. Kein
Abschnitt „neu gescheitert“, keine „vorher“-Werte. Grund: `.gitignore`
aus Stufe 0 ignorierte `evals/ergebnisse/*.json`. Die Basislinie lag auf
meinem Rechner, nie im Repo, und die CI verglich gegen nichts — ohne ein
Wort. Ich hatte im Commit geschrieben „die Basislinie liegt unter
evals/ergebnisse“ und nicht mit `git ls-files` nachgesehen. Das ist der
Fehler aus dem Regel-Check vom ersten Tag in neuem Gewand: ein Gate, das
bei fehlender Grundlage grün ist. Zwei Korrekturen: der Ignore-Eintrag ist
weg, und der Runner schreibt „KEINE BASISLINIE“ in die erste Zeile, wenn
ihm der Vergleich fehlt — mit Test.

**Was ich stehen lasse.** `unleserlich-01`: Das Modell liest „W$“ als WS.
Ein Mensch auch. Der Fall bleibt rot, weil er genau das prüft, und weil
eine Verordnung mit geratener Diagnosegruppe im Kalender eine falsche
Frist ist. Zwei von 55 rot, beide begründet — das ist der Stand.

**Zeitschätzung:** eine halbe Stunde, davon zwei Drittel für das Gate.

---

## 2026-09-11 — Dritter Eval-Lauf: das Gate greift, und das Modell erfindet ein Feld

**Was der Lauf gezeigt hat.** Zum ersten Mal mit Basislinie im Repo: Der
Bericht zeigt „(vorher 100,0 %)“ hinter den Feldern, und am Ende steht
„NEU GESCHEITERT: heilmittel-kg-ohne-zusatz“. Der Job ist rot. Genau das
war die Zusage — nach dem stummen Lauf davor ist das die Gegenprobe, die
ich brauchte.

**Der Fall selbst.** Das Modell hat ein Feld erfunden:
`verordnungsdatum_hinweis: "n/a"`. Das Schema verbietet Unbekanntes
(`extra="forbid"`), Pydantic wirft, und der ganze Fall stirbt — alle sieben
Felder rot, obwohl die Extraktion nach dem Bericht vermutlich gestimmt
hätte. Nicht der Prompt war das Problem, sondern die Pipeline: Ein
erfundenes Feld ist ein Hinweis, kein Totalausfall. Jetzt baut eine
Funktion den Vorschlag aus der rohen Werkzeug-Eingabe, wirft Unbekanntes
heraus und nennt es in `hinweise`. Das Schema bleibt streng — der
Unterschied ist, wer die Strenge abfängt.

Nebenbei: Das ist der Grund, warum die Suite absichtlich denselben Text
zweimal sieht. Beim zweiten Lauf war dieser Fall grün; beim dritten hat
das Modell anders geantwortet. Ohne die Suite hätte das erst eine Praxis
gemerkt.

**Zeitschätzung:** zwanzig Minuten.

---

## 2026-09-11 — SonarCloud: was ein fremdes Werkzeug findet

**Was delegiert wurde:** SonarCloud als CI-Job mit Abdeckung aus allen drei
Stacks, dann die Durchsicht der ersten 45 Funde.

**Der Befund, der zählt.** Null Funde im Java-Code, null in der Angular-App,
null im Python-Dienst. Alle 45 in `.github/workflows/ci.yml` und in
`scripts/` — also in den Werkzeugen, die die eigenen Gates *bauen*. Die
Gates prüften den Code; niemand prüfte die Gates. Ein fremdes Werkzeug
sieht, was das eigene nicht sehen kann, weil es sich selbst nicht liest.

**Was behoben wurde, und warum echt statt abgelehnt.** Actions auf
Commit-SHAs gepinnt: Ein Tag wie `v4` kann verschoben werden, ein Commit
nicht — das ist ein Lieferkettenargument, kein Stil. `npm ci
--ignore-scripts` und `uv sync --no-build`: beides vorher lokal probiert,
Build und 35 plus 22 Tests grün, also keine Ausrede. `[[` in den
Bash-Skripten: Sie laufen unter Bash, der Shebang sagt es. Fünf reguläre
Ausdrücke mit überlappenden Zeichenklassen — der Prosa-Check hatte ein
Muster, das bei langen Wörtern quadratisch wird; jetzt zwei einfache
Schritte. Gegenprobe nach jeder Änderung: alle Gates und die 22 Fälle der
Regel-Check-Suite liefern dasselbe wie vorher. Dependabot dazu, damit die
Pins nicht in einem Jahr Museumsstücke sind.

**Was abgelehnt wird, mit Grund.** `evals/run.py` nimmt einen Pfad aus einem
CLI-Argument — „Path Traversal“. Es ist ein Skript im Repo, das ein Mensch
aufruft; es gibt keinen Angreifer, der Argumente liefert. Steht so in
Sonar als akzeptiert.

**Was Sonar nicht sieht.** Prosa, Fundstellen, Belege, Kontraste, die
Zusage „der AI-Layer schlägt vor“. Deshalb ersetzt es nichts. Es ist die
zweite Meinung, und die erste hat es sofort gebraucht.

**Nebenbei zwei Dinge, die nicht Sonar fand.** Die Infrastruktur verdrängte
mit ihrem Docker-`argLine` den JaCoCo-Agenten — ohne `@{argLine}` hatte
das Modul keine Abdeckung. Und ein e2e-Test las die fokussierte Zelle vor
dem Render-Zyklus; lokal schnell genug, auf dem Runner eine Zelle Versatz.
Der Test wartet jetzt, bis sich der Fokus bewegt hat.

**Zweite Runde, und warum „akzeptieren“ doch die falsche Antwort war.** Nach
dem ersten Durchgang blieben neun Funde, davon sieben sicherheitsrelevant —
genug, um das Quality Gate rot zu halten (`new_security_rating` 3). Sechsmal
`uv run` ohne `--no-build`: Das Flag gibt es, das Projekt liegt als Lock vor,
gebaut werden muss zur Laufzeit nichts — also behoben, nicht abgelehnt. Und
der Pfad aus dem CLI-Argument in `evals/run.py`, den ich eine Stunde vorher
noch als „Skript für Menschen, kein Dienst“ akzeptieren wollte: Beim
Hinschauen ist die Einschränkung richtiges Verhalten, nicht Sicherheitstheater.
Der Runner liest Fälle aus dem Repo und schreibt Ergebnisse dorthin; ein Pfad
nach `C:\Windows` ist in jedem Fall ein Fehler. Acht Zeilen, ein Test, und die
Ausnahme ist nicht nötig. Die Lehre: „Das gilt hier nicht“ ist bequem, und
beim zweiten Lesen stimmt es oft nicht.

**Was noch offen ist.** Zwei reguläre Ausdrücke mit mehrdeutiger Rückkehr,
beide entschärft, aber erst der nächste Lauf zeigt, ob Sonar zufrieden ist.
Und die Abdeckung fehlte in allen bisherigen Analysen — nicht wegen falscher
Pfade, sondern weil die automatische Analyse noch lief und der CI-Job gar
nicht durchkam. Ein Werkzeug, das zwei Wege anbietet und einen davon still
gewinnen lässt.

**Der erste Lauf aus der CI — und drei neue Erkenntnisse.** Mit abgeschalteter
automatischer Analyse lief der Job zum ersten Mal richtig durch: Abdeckung
83,5 Prozent über alle drei Stacks, Sicherheitsbewertung auf 1, noch vier
Funde. Zwei davon in Java — die automatische Analyse hatte den Java-Code nie
gesehen, weil ihr die kompilierten Klassen fehlten. Ein Werkzeug, das ohne
Konfiguration läuft, prüft eben nicht alles, und es sagt nicht, was es
auslässt.

**Der Fund, der keiner ist.** `java:S8696` verlangt `.equals()` statt `==` bei
`montag.getDayOfWeek() != DayOfWeek.MONDAY`. `DayOfWeek` ist ein Enum, und bei
Enums ist `==` die richtige Vergleichsart — Sonar hat dafür sogar eine eigene
Regel, die genau das fordert. Zwei Regeln desselben Werkzeugs widersprechen
sich, und hätte ich den Fund brav „behoben“, wäre der Code schlechter
geworden. Das ist die Grenze eines fremden Werkzeugs: Es kennt den Kontext
nicht, und wer jeden Fund abarbeitet, ohne ihn zu lesen, verschlimmbessert.

**Die Abdeckung als echter Befund.** Das Gate blieb rot: 73,3 Prozent auf
neuem Code, Schwelle 80. Die Lücke lagen bei den beiden echten Providern, 0
Prozent. Die naheliegende Antwort wäre ein Ausschluss gewesen — „braucht
einen Schlüssel, nicht testbar“. Stimmt aber nicht: Beide nehmen ihren Client
im Konstruktor, genau dafür. Sechs Tests mit einem Stub prüfen jetzt, was
dieser Code selbst tut — dass `tool_choice` auf das Werkzeug zeigt, dass eine
Antwort am Werkzeug vorbei einen Fehler gibt, dass ein erfundenes Feld
herausfliegt. Beide Provider von 0 auf 100 Prozent, gesamt 93. Das Gate hat
eine Lücke gezeigt, die ich sonst nicht gesehen hätte.

**Und eine Selbstverletzung.** Beim Umbau des Kontrast-Checks auf
zeilenweises Lesen hat die Shell meine Backslashes gefressen: Aus `\r?\n`
wurde ein echter Zeilenumbruch mitten im Muster, und das Gate war syntaktisch
kaputt — nicht rot, sondern tot. Aufgefallen, weil ich nach jeder Änderung
die Gegenprobe mache: Token verfälschen, Gate muss rot werden. „2 von 31
Paaren halten die Anforderung nicht ein“ ist der Beweis, dass es lebt. Ohne
diese Gewohnheit wäre ein totes Gate durchgerutscht, und tote Gates sind
schlimmer als fehlende.

**Am Ende des Tages.** Quality Gate grün, Abdeckung 85,5 Prozent über drei
Sprachen, 100 Prozent auf dem neuen Code, drei offene Funde — alle drei
derselbe Enum-Fehlalarm. Von 45 Funden sind 42 behoben und null mit
„gilt hier nicht“ weggeklickt. Zweimal wollte ich genau das tun, und
zweimal war die Ausrede beim zweiten Hinsehen falsch: der Pfad im
Eval-Runner und die angeblich untestbaren Provider.

**Zeitschätzung:** anderthalb Stunden. Von Hand ein Tag, und die Hälfte der
Funde würde mit „won't fix“ weggeklickt.

---

## 2026-09-11 — Stufe 3, Schritt 3: der MCP-Server

**Was delegiert wurde:** Aptum als Werkzeug für ein Sprachmodell. Vier
Werkzeuge auf der REST-Schnittstelle, neun Tests, dazu eine Rauchprobe gegen
das laufende Backend. 39 Python-Tests.

**Die Entwurfsfrage war nicht, was der Server kann, sondern was er nicht
kann.** Regel 3 braucht hier keine zweite Durchsetzung: Wer über
`termin_buchen` bucht, ruft `POST /termine` auf, und dort prüft dieselbe
Domäne wie bei einer Buchung von Hand. Das ist der ganze Punkt — ein
Regelwerk, kein zweiter Weg hinein. Was der Server hinzufügt, sind drei
Einschränkungen an der Grenze:

Der **Mandant ist kein Parameter**. Er kommt aus der Umgebung. Ein Modell
kann nicht sagen „buche in praxis-b“, weil es die Praxis nicht benennen
kann. Ein Test prüft, dass das Wort in keinem der vier Werkzeugschemata
vorkommt — nicht im Code, im Schema, denn das ist, was das Modell sieht.

Die **Übersteuerung verlangt zehn Zeichen Begründung**, und zwar als
`minLength` im Schema statt als Prüfung im Rumpf. Der Unterschied ist
praktisch: Das Modell liest die Bedingung, bevor es aufruft. Die Rauchprobe
mit `begruendung: "ok"` kam gar nicht bis zum Dienst — die Schema-Prüfung
lehnte ab, mit lesbarer Meldung.

**Fehler müssen `ToolError` sein.** Das habe ich erst beim Testen gemerkt:
Eine beliebige Ausnahme aus einem Werkzeug erreicht das Modell als „Error
executing tool woche_anzeigen“ — der Grund bleibt im Serverlog. Wer will,
dass ein Modell weiterarbeiten kann, muss ihm sagen, was los war. Jetzt
steht „Der Scheduling-Dienst antwortete 503“ in der Antwort, und ein Test
hält die Unterscheidung fest.

**Die Rauchprobe, die überzeugt hat.** Gegen das echte Backend: suchen
(1170 Slots geprüft, 462 wegen der Therapeutin ausgeschlossen, 236 wegen
der Raumausstattung, 6 wegen des Raums), prüfen (11 Regeln, FREI), buchen,
denselben Slot noch einmal — BLOCKIERT, mit beiden verletzten Regeln und
den Uhrzeiten einschließlich Rüstzeit: „T. Alpha hat von 14:10 bis 14:45
bereits Krankengymnastik“. Dann übersteuern mit Begründung: `UEBERSTEUERT`,
mit Kennung. Das ist Regel 3 in einem Durchlauf, von außen sichtbar.

**Zeitschätzung:** eine Stunde. Von Hand ein Tag, und der Mandant wäre ein
Parameter — weil es bequemer ist.

---

## 2026-09-12 — Stufe 4, Schritt 1: ein Befehl für alles

**Was delegiert wurde:** Container für alle drei Dienste und ein
`compose.yml`, das den ganzen Stapel hochfährt. Auslöser war der Blick auf
die Anforderungen: C4 — Kubernetes, Helm, ArgoCD, Terraform — ist der
einzige Must-have, der bei null stand.

**Was gut lief.** Alle drei Bilder haben beim ersten Versuch gebaut, und die
Rauchprobe durch nginx ging in einem Durchgang: Seite, SPA-Fallback für
`/kalender`, beide Dienste über `/api`, Prozesse als `aptum` statt Root.
Das liegt nicht an Glück, sondern daran, dass die Vorarbeit schon da war —
das `/api`-Präfix vom Vortag ist in `nginx.conf` dieselbe Regel wie im
Dev-Proxy, die Rolle `aptum_app` ist dasselbe Skript wie im Testcontainer,
und die Healthchecks fragen dieselben Endpunkte, die die CI schon abfragt.

**Die Entscheidung dahinter.** „Starten“ im README war eine halbe Seite:
Container für Postgres, Rolle per `docker exec`, Maven, Jar mit vier
Umgebungsvariablen, dann noch zwei Dienste — und ein Hinweis, dass ein
Volume-Mount unter Git Bash schweigt. Wer ein Portfolio-Projekt nicht in
einer Minute starten kann, liest es nicht weiter. Jetzt ist es ein Befehl,
und die Startreihenfolge ist eine Bedingung im Compose, keine Hoffnung.

**Was ehrlich bleibt.** Container sind der kleinste Teil von C4. Helm, ein
Cluster in der CI, Terraform und ArgoCD kommen als Nächstes — und zwar so,
dass die Pipeline bei jedem Push damit deployt. Ein Chart im Repo ist eine
Behauptung; ein Chart, mit dem die CI einen Cluster hochfährt und einen
Rauchtest fährt, ist ein Beleg, der brechen kann. Und selbst dann steht C4
nicht auf „belegt“, sondern auf „teilweise belegbar“, wie S5: Ein
Portfolio-Projekt ersetzt nicht, einen Cluster betrieben zu haben.

**Zeitschätzung:** eine Stunde. Von Hand ein halber Tag — und die Rolle würde
im Dockerfile des Backends angelegt, wo sie nicht hingehört.

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
