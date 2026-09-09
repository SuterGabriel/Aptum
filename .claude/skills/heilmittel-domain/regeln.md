# Regeltabelle

Belegt gegen die Primärquellen. Recherche vom 09.09.2026.

**Rechtsstände:** Heilmittel-Richtlinie zuletzt geändert 15.05.2025, in Kraft
seit 05.08.2025 · Vertrag nach § 125 Abs. 1 SGB V Physiotherapie, Lesefassung
04.04.2022, Anlage 1 gültig ab 01.12.2024, Anlage 5 vom 21.07.2021, Anlage 7
Fassung 13.04.2026 wirksam ab 01.06.2026 · Vertrag Ergotherapie, Lesefassung
16.03.2026.

Status: `BELEGT` mit Fundstelle · `UNSICHER` widersprüchlich oder ohne
Primärquelle · `OFFEN` nicht recherchiert.

> **Vor jeder Implementierung:** Nur Zeilen mit Status BELEGT dürfen als Zahl in
> den Code. Bei UNSICHER wird die Regel als konfigurierbarer Parameter
> modelliert, nicht als Konstante.

---

## 1. Fristen für den Behandlungsbeginn

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Behandlungsbeginn, Regelfall | innerhalb **28 Kalendertagen** nach Ausstellung | § 15 Abs. 1 S. 1 HeilM-RL | BELEGT |
| Behandlungsbeginn, dringlicher Bedarf | spätestens **14 Kalendertage**, Kennzeichen muss auf der Verordnung stehen | § 15 Abs. 1 S. 2–3 HeilM-RL | BELEGT |
| Folge der Fristversäumnis | Verordnung **verliert ihre Gültigkeit** | § 15 Abs. 2 HeilM-RL | BELEGT |
| Entlassmanagement | Beginn **≤ 7 Kalendertage** nach Entlassung, Abschluss **≤ 12 Kalendertage**, nicht genutzte Einheiten verfallen | § 16a Abs. 3 HeilM-RL | BELEGT |
| Folgeverordnung im laufenden Fall (Ergo) | Beginn **≤ 28 Tage nach dem letzten Behandlungstermin** der Vorverordnung | § 7 Abs. 5 Vertrag Ergo | BELEGT |

## 2. Unterbrechung

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Grundregel | Unterbrechung **> 14 Kalendertage** ohne Begründung lässt die Verordnung für die Restmenge verfallen | § 16 Abs. 4 S. 1 HeilM-RL | BELEGT |
| Zulässige Gründe | **T** therapeutisch indiziert · **K** Krankheit auf einer der beiden Seiten · **F** Ferien und Urlaub, in der Ergotherapie zusätzlich **U** | § 7 Abs. 3a Verträge Physio und Ergo | BELEGT |
| Obergrenze **Ergotherapie** | begründete Unterbrechungen in Summe **max. 70 Kalendertage**, gezählt werden nur Unterbrechungen von je über 14 Tagen | § 7 Abs. 3a Vertrag Ergo | BELEGT |
| Obergrenze **Physiotherapie** | **keine** Summenobergrenze im Vertragstext | § 7 Abs. 3a Vertrag Physio, Nichtfund | BELEGT als Nichtfund |
| Absolute Gültigkeit **Physiotherapie** | ≤ 6 Behandlungseinheiten: **3 Monate** · > 6 Einheiten: **6 Monate**, jeweils ab dem **ersten Behandlungstag** | § 7 Abs. 3a S. 1–2 Vertrag Physio | BELEGT |
| Absolute Gültigkeit **Ergotherapie** | keine entsprechende Regel gefunden | Vertrag Ergo §§ 6, 7 | UNSICHER, Nichtfund ohne Positivbeleg |
| Unterbrechung ist keine Frequenzabweichung | wörtlich so im Vertrag | § 7 Abs. 3a Verträge | BELEGT |
| Blankoverordnung | Unterbrechung > 14 Tage führt **nicht** zum Verlust, verlängert die 16-Wochen-Frist aber nicht | § 3 Nr. 3 Vertrag § 125a Physio | BELEGT |

> **Die wichtigste Asymmetrie des Projekts:** Physio und Ergo haben
> gegenläufige Regeln. Ergo begrenzt die Summe der Unterbrechungen, Physio die
> Gesamtlaufzeit. Das muss getrennt modelliert werden, nicht in eine Regel
> gefaltet. Guter Kandidat für einen Testfall.

## 3. Frequenz

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Bindungswirkung | Die Frequenzangabe der Verordnung ist für die Therapeutin oder den Therapeuten **bindend** | § 16 Abs. 3 S. 1 HeilM-RL | BELEGT |
| Abweichung | nur nach **vorheriger Absprache** mit der verordnenden Person, einvernehmliche Änderung ist auf dem Vordruck zu dokumentieren | § 16 Abs. 3 S. 2–4 HeilM-RL | BELEGT |
| Spanne zulässig | Frequenz darf als Bereich angegeben werden, etwa 1–3x wöchentlich | § 13 Abs. 2 lit. i HeilM-RL | BELEGT |
| Katalogempfehlung | Regelwert der meisten Diagnosegruppen: **1–3x wöchentlich** | Heilmittelkatalog, Spalte Frequenzempfehlung | BELEGT |
| Prozentuale Toleranz | **existiert nicht** | HeilM-RL, Nichtfund | BELEGT als Nichtfund |

> **Konsequenz für das Domänenmodell:** Es gibt keine Karenzregel, die man
> implementieren könnte. Verbindlich ist allein die konkrete Angabe auf der
> Verordnung. Die Regel `FrequenzGrenze` prüft also gegen die Verordnung, nicht
> gegen den Katalog, und liefert bei Verletzung eine Begründung, keine
> automatische Ablehnung. Über die Abweichung entscheidet ein Mensch nach
> Rücksprache mit der verordnenden Person.

## 4. Mengen

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Höchstmenge je Verordnung, Physio | i. d. R. **6** Einheiten, ZNS-Gruppen ZN und PN **10** | Heilmittelkatalog | BELEGT |
| Höchstmenge je Verordnung, Ergo | SB1, SB2, EN1, EN2, PS4: **10** · PS2, PS3: **20** | Heilmittelkatalog | BELEGT |
| Orientierende Behandlungsmenge, Physio | WS, EX, CS, AT, GE, SO: **18** · LY: **30** · ZN, PN: **30** | Heilmittelkatalog | BELEGT |
| Orientierende Behandlungsmenge, Ergo | SB1 **20** · SB2 **30** · EN1 **40**, bis 60 · EN2 **40** · PS2, PS3, PS4 **40** | Heilmittelkatalog | BELEGT |
| Bedeutung der orientierenden Menge | Richtwert, **keine** verbindliche Obergrenze, weitere Verordnungen im selben Verordnungsfall möglich | § 7 Abs. 2 und 4 HeilM-RL | BELEGT |
| Verordnungsfall | gleiche Diagnose nach den ersten **drei Stellen des ICD-10-GM** und gleiche Diagnosegruppe, neuer Fall nach **6 Monaten** ohne weitere Verordnung | § 7 Abs. 1 HeilM-RL | BELEGT |
| Langfristiger Heilmittelbedarf | Verordnung über bis zu **12 Behandlungswochen**, Höchstmenge nicht bindend, bei einer Frequenzspanne ist der **höchste Wert** maßgeblich | § 7 Abs. 6, § 8 HeilM-RL | BELEGT |
| Blankoverordnung | Gültigkeit **max. 16 Wochen** ab Verordnungsdatum, Podologie 40 Wochen | § 13a Abs. 2 HeilM-RL | BELEGT |
| Reihenfolge mehrerer Verordnungen | Eine Verordnung ist **abzuschließen**, bevor mit einer später ausgestellten zur selben Diagnose begonnen wird | § 7 Abs. 5 Verträge | BELEGT |

## 5. Qualifikation der Therapeutin oder des Therapeuten

Zertifikatsleistungen der Physiotherapie. Ohne nachgewiesene Weiterbildung und
personengebundene Abrechnungserlaubnis darf die Leistung nicht erbracht werden.

| Heilmittel | Anforderung | Fundstelle | Status |
|---|---|---|---|
| Manuelle Therapie | **260 Stunden** bzw. 260 Unterrichtseinheiten à 45 Min., mit Abschlussprüfung | Anlage 1 Pos. X1201, Anlage 7 Ziff. 3.2.1 | BELEGT |
| Manuelle Lymphdrainage | Anlage 1: **170 Stunden** · Anlage 7 ab 01.06.2026: **140 Unterrichtseinheiten** | Anlage 1 Pos. X0205/X0201/X0202, Anlage 7 Ziff. 2.2.1 | **UNSICHER — Widerspruch, siehe unten** |
| Krankengymnastik am Gerät | **40 Unterrichtseinheiten** à 45 Min., mindestens 60 % Praxis | Anlage 1 Pos. X0507, Anlage 7 Ziff. 1.2 | BELEGT |
| KG-ZNS Erwachsene, Bobath, Vojta, PNF | **120 Stunden** im jeweiligen Verfahren mit Abschlussprüfung | Anlage 1 Pos. X0710–X0712 | BELEGT |
| KG-ZNS Kinder, Bobath, Vojta | **300 Stunden** mit Abschlussprüfung | Anlage 1 Pos. X0708/X0709 | BELEGT |
| Abrechnungserlaubnis | **personengebunden**, erteilt durch die Arbeitsgemeinschaft nach § 124 Abs. 2 SGB V | § 6 Abs. 2a Vertrag Physio | BELEGT |
| Ergotherapie | **keine** Zertifikatspositionen, nur allgemeine Fortbildungspflicht von 60 Fortbildungspunkten in 4 Jahren | Anlage 4 Ergo | BELEGT |

## 6. Raum und Ausstattung

| Heilmittel | Anforderung | Fundstelle | Status |
|---|---|---|---|
| Physio, Grundausstattung | Therapiefläche gesamt **≥ 23 m²**, je zusätzlich gleichzeitig tätiger Person **+8 m²**, zwei höhenverstellbare Liegen, Kurzzeituhren, Notrufanlage | Anlage 5 Physio Ziff. 2.1–2.4, 3.1 | BELEGT |
| Krankengymnastik am Gerät | eigener Bereich **≥ 30 m²**, je weiterem Gerät **+4 m²**, Sicherheitsabstand 1 m, vier Pflichtgeräte | Anlage 5 Ziff. 4.2.1 | BELEGT |
| Bewegungsbad | Wasseroberfläche **≥ 12 m²**, kleinste Seitenlänge ≥ 3 m, Wassertiefe ≤ 1,35 m, Wassertemperatur 28–36 °C, Dusche | Anlage 5 Ziff. 4.2.5/4.2.7 | BELEGT |
| Unterwasserdruckstrahlmassage | Spezialwanne **≥ 600 l**, Aggregat ≥ 100 l/min, eigener Raum, je Wanne eine Ruheliege | Anlage 5 Ziff. 4.2.2 | BELEGT |
| Traktion | Gerät für **HWS und LWS** | Anlage 5 Ziff. 4.2.10 | BELEGT |
| Schlingentisch | **keine** eigene Ausstattungsposition, nur im Leistungstext der Traktion erwähnt | Anlage 5, Nichtfund | BELEGT als Nichtfund |
| Ergotherapie | Therapiefläche gesamt **≥ 20 m²**, je Leistungserbringerin oder Leistungserbringer ein Raum **≥ 12 m²**, entfällt bei nicht überschneidenden Zeiten | Anlage 5 Ergo Ziff. 2 und 3 | BELEGT |

## 7. Behandlungszeiten und Rüstzeit

Die für die Slot-Länge maßgeblichen Werte.

| Leistung | Zeit | Status |
|---|---|---|
| Physio, KG Einzel (X0501) | 15–25 Min. | BELEGT |
| Physio, KG Gruppe (X0601) | 20–30 Min. | BELEGT |
| Physio, Manuelle Therapie (X1201) | 15–25 Min. | BELEGT |
| Physio, Klassische Massage (X0106) | 15–20 Min. | BELEGT |
| Physio, MLD (X0205/X0201/X0202) | 30 / 45 / 60 Min. | BELEGT |
| Physio, KG-ZNS Erwachsene | 25–35 Min. | BELEGT |
| Physio, KG-ZNS Kinder | 30–45 Min. | BELEGT |
| Physio, KG am Gerät (X0507) | **60 Min. je Patient, bis zu 3 Patienten gleichzeitig**, Start nicht zeitgleich nötig | BELEGT |
| Physio, KG im Bewegungsbad | 20–30 Min. zuzüglich Nachruhe | BELEGT |
| Physio, Warmpackung (X1501) | 20–30 Min. | BELEGT |
| Ergo, motorisch-funktionell (54102) | **45 Min. = 30 Therapie + 15 Vor- und Nachbereitung** | BELEGT |
| Ergo, sensomotorisch-perzeptiv (54103) | 60 Min. = 45 + 15 | BELEGT |
| Ergo, Hirnleistungstraining (54104) | 45 Min. = 30 + 15 | BELEGT |
| Ergo, psychisch-funktionell (54105) | 75 Min. = 60 + 15 | BELEGT |
| Ergo, Beratung des Umfelds (54107–54112) | 120 Min. = 105 + 15 | BELEGT |
| **Nachruhe Physio** | Richtwert **20–25 Min.** nach Bädern, Unterwasserdruckstrahlmassage und Bewegungsbad | BELEGT |

**Zur Rüstzeit, der wichtigste Modellierungshinweis dieser Tabelle:**

In der Ergotherapie ist die Rüstzeit **explizit Teil der Regelleistungszeit**,
15 Minuten je Einheit. In der Physiotherapie heißt es nur, Vor- und
Nachbereitung seien Bestandteil der Behandlung, ohne eigene Zeitangabe. Die
Zeitrichtwerte der Physiotherapie beziehen sich auf die Durchführung **am
Patienten**.

Ob bei Physio zusätzlich Rüstzeit einzuplanen ist, lässt der Vertragstext offen.
Für das Domänenmodell folgt daraus: Rüstzeit ist ein **Parameter je Praxis und
Heilmittel**, keine Konstante. Und die Nachruhe blockiert Raum und Zeit, ohne
Therapeutenbindung. Das sind zwei verschiedene Dinge und gehören getrennt
modelliert.

## 8. Hausbesuch und Sonstiges

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Hausbesuch | nur zulässig, wenn die Praxis aus **medizinischen Gründen** nicht aufgesucht werden kann | § 11 Abs. 1 HeilM-RL | BELEGT |
| Annahmepflicht | verordnete Hausbesuche im üblichen Praxisbereich können grundsätzlich **nicht abgelehnt** werden | § 4 Abs. 1 Vertrag Physio | BELEGT |
| Praxisöffnung | mindestens **3 Tage** und **25 Stunden** je Woche für gesetzlich Versicherte | § 12 Abs. 1 Vertrag Physio | BELEGT |
| Wechsel Einzel zu Gruppe | nur mit Zustimmung der versicherten Person und im Einvernehmen mit der verordnenden Person | § 16 Abs. 6 HeilM-RL | BELEGT |

## 9. Ausfallhonorar

| Regel | Wert | Fundstelle | Status |
|---|---|---|---|
| Regelung im Sozialrecht | **existiert nicht** in HeilM-RL oder den Verträgen nach § 125 | Volltextsuche, Nichtfund | BELEGT als Nichtfund |
| Rechtsgrundlage | rein zivilrechtlich, Annahmeverzug nach **§ 615 BGB** | § 615 BGB | BELEGT |
| Gesetzliche 24-Stunden-Frist | **existiert nicht.** 24 Stunden sind Praxis- und AGB-Konvention. Starre Klauseln ohne Differenzierung nach Absagegrund gelten als angreifbar | nur Sekundärquellen | UNSICHER |
| Bei gesetzlich Versicherten | die Kasse zahlt nicht, die Forderung richtet sich privat gegen die versicherte Person | abgeleitet | UNSICHER |

> **Konsequenz:** Die Ausfallregel wird als **Praxiseinstellung** modelliert, mit
> Frist und Ausnahmegründen als Parameter. Eine fest verdrahtete
> 24-Stunden-Regel wäre fachlich falsch. Das ist ein gutes Beispiel dafür, warum
> Fachregeln benannte, parametrisierte Klassen sind und keine `if`-Blöcke.

---

## Widersprüche und offene Punkte

Diese Liste ist Teil des Belegs. Wer eine Domäne recherchiert und *keine*
Widersprüche findet, hat nicht genau genug gelesen.

1. **Manuelle Lymphdrainage, 170 Stunden gegen 140 Unterrichtseinheiten.**
   Anlage 1 gilt seit 01.12.2024, Anlage 7 in der Fassung vom 13.04.2026 wird
   am 01.06.2026 wirksam. Vermutlich eine Neuregelung, bei der Anlage 1 noch
   nicht nachgezogen wurde. Im Code nicht auf eine Zahl festlegen.
2. **Anlage 7 Physiotherapie ist eine Übergangskonstruktion.** Die neue Fassung
   gilt ab 01.06.2026 nur für Lymphdrainage, Manuelle Therapie und Gerät. Für
   KG-ZNS gilt weiter die alte Fassung von 2021.
3. **Absolute Verordnungsgültigkeit bei Ergotherapie.** Keine Regel gefunden.
   Ein Nichtfund ist kein Beweis der Abwesenheit.
4. **Rüstzeit bei Physiotherapie.** Der Vertragstext lässt offen, ob sie
   zusätzlich zum Zeitrichtwert einzuplanen ist.
5. **Ausfallhonorar.** Rechtsprechung uneinheitlich, keine Primärquelle für die
   verbreitete 24-Stunden-Praxis.
6. **Stand der Verträge ist ungleich.** Der Physio-Vertrag liegt als Lesefassung
   vom 04.04.2022 vor, der Ergo-Vertrag vom 16.03.2026.

## Quellen

- Heilmittel-Richtlinie: https://www.g-ba.de/downloads/62-492-3865/HeilM-RL_2025-05-15_iK-2025-08-05.pdf
- Heilmittelkatalog: https://www.g-ba.de/downloads/17-98-3064/HeilM-RL_2024-05-16_Heilmittelkatalog.pdf
- Vertrag § 125 Physiotherapie, Lesefassung 04.04.2022: https://www.gkv-spitzenverband.de/media/dokumente/krankenversicherung_1/ambulante_leistungen/heilmittel/vertraege_125abs1/physiotherapie/20220331_Vertrag__Lesefassung_Stand_04.04.2022_telemedizinische_Leistungen_Physiotherapie.pdf
- Anlage 1 Physiotherapie, Leistungsbeschreibung: https://www.gkv-spitzenverband.de/media/dokumente/krankenversicherung_1/ambulante_leistungen/heilmittel/vertraege_125abs1/physiotherapie/20241202_Lesefassung_Anlage_1_gueltig_ab_01.12.2024_Leistungsbeschreibung_Physio.pdf
- Anlage 5 Physiotherapie, Zulassungsvoraussetzungen: https://www.gkv-spitzenverband.de/media/dokumente/krankenversicherung_1/ambulante_leistungen/heilmittel/vertraege_125abs1/physiotherapie/20210721_Physiotherapie_Anlage_5_Zulassungsvoraussetzungen_bf.pdf
- Vertrag § 125 Ergotherapie, Lesefassung 16.03.2026: https://www.gkv-spitzenverband.de/media/dokumente/krankenversicherung_1/ambulante_leistungen/heilmittel/vertraege_125abs1/ergotherapie/20260316_Ergotherapie_125_Absatz_1_SGB_V_Vertrag_final.pdf
- Vertrag § 125a Physiotherapie, Blankoverordnung: https://www.gkv-spitzenverband.de/media/dokumente/krankenversicherung_1/ambulante_leistungen/heilmittel/vertraege_nach_125a/physio/20240830_Vertrag_nach__125a_SGB_V_Physiotherapie.pdf
- § 615 BGB: https://www.gesetze-im-internet.de/bgb/__615.html
