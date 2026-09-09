# Glossar

Das Vokabular der Domäne. Diese Begriffe werden im Code als deutsche Bezeichner
verwendet, nicht übersetzt. Eine Übersetzung erzeugt eine zweite Sprache, in der
die Regeln nicht mehr wörtlich nachschlagbar sind, und genau das ist die Quelle
subtiler Fachfehler.

## Verordnung und Fall

| Begriff | Bedeutung |
|---|---|
| Heilmittel | Persönlich zu erbringende medizinische Dienstleistung, die ärztlich verordnet und von zugelassenen Leistungserbringern abgegeben wird. |
| Verordnung | Das ärztliche Rezept auf dem Vordruck Muster 13. Legt Heilmittel, Anzahl der Behandlungseinheiten, Frequenz, Diagnosegruppe und Leitsymptomatik fest. |
| Behandlungseinheit | Eine einzelne verordnete Therapiesitzung. Die Zähleinheit für Höchstmenge, orientierende Menge und Abrechnung. |
| Diagnosegruppe | Der Kurzschlüssel des Heilmittelkatalogs, etwa WS, EX, ZN, LY in der Physiotherapie oder SB1, EN1, PS2 in der Ergotherapie. Bestimmt, welche Heilmittel, Mengen und Frequenzen verordnungsfähig sind. |
| Leitsymptomatik | Die katalogkodierte Schädigung von Körperfunktionen oder eine patientenindividuelle Freitextangabe, die die Behandlung begründet. |
| Verordnungsfall | Alle Behandlungen wegen derselben Diagnose nach den ersten drei Stellen des ICD-10-GM und derselben Diagnosegruppe bei derselben verordnenden Person. Endet, wenn sechs Monate lang keine weitere Verordnung ausgestellt wurde. |
| Blankoverordnung | Verordnung nach § 125a SGB V, bei der die Therapeutin oder der Therapeut Heilmittel, Menge, Dauer und Frequenz selbst festlegt. Gültig 16 Wochen, in der Podologie 40. |
| Entlassmanagement | Krankenhausverordnung über maximal sieben Tage Versorgung mit eigenen, kurzen Fristen. |

## Mengen und Frequenz

| Begriff | Bedeutung |
|---|---|
| Höchstmenge je Verordnung | Die maximale Anzahl Behandlungseinheiten auf einem einzelnen Rezept. In der Physiotherapie meist 6, in den ZNS-Gruppen 10. |
| Orientierende Behandlungsmenge | Die Summe an Einheiten, mit der das Therapieziel in der Regel erreicht wird. Ein Richtwert, keine harte Obergrenze. |
| Langfristiger Heilmittelbedarf | Anerkannter Dauerbedarf nach § 32 Abs. 1a SGB V. Je Verordnung bis zu zwölf Behandlungswochen, Höchstmenge nicht bindend. |
| Besonderer Verordnungsbedarf | Diagnosen, die von der ärztlichen Wirtschaftlichkeitsprüfung ausgenommen sind. Dieselben erweiterten Mengenregeln. |
| Frequenz | Die auf der Verordnung angegebene Behandlungshäufigkeit. Für die Therapeutin oder den Therapeuten bindend. |
| Frequenzspanne | Eine Frequenzangabe als Bereich, etwa 1–3x wöchentlich. Beim langfristigen Bedarf ist für die Mengenbemessung der höchste Wert maßgeblich. |

## Zeit

| Begriff | Bedeutung |
|---|---|
| Regelbehandlungszeit | Der Zeitrichtwert für die Durchführung **am Patienten**. Die Mindestdauer darf nur aus medizinischen Gründen unterschritten werden. |
| Regelleistungszeit | Der ergotherapeutische Begriff. Besteht ausdrücklich aus Therapiezeit plus 15 Minuten Vor- und Nachbereitung. |
| Rüstzeit | Praxisüblicher Begriff für Vor- und Nachbereitung. In der Ergotherapie explizit ausgewiesen, in der Physiotherapie nur als Bestandteil der Behandlung beschrieben. Im Modell ein Parameter, keine Konstante. |
| Nachruhe | Die nach Bädern, Unterwasserdruckstrahlmassage und Bewegungsbad vorgesehene Ruhephase, Richtwert 20 bis 25 Minuten. Blockiert Raum und Zeit, aber nicht die Therapeutin oder den Therapeuten. |
| Behandlungsunterbrechung | Eine Pause zwischen zwei Terminen. Ab mehr als 14 Kalendertagen verfällt die Verordnung, sofern kein dokumentierter Grund vorliegt. |
| Unterbrechungskennzeichen | Die Buchstaben auf dem Verordnungsblatt: T therapeutisch indiziert, K Krankheit, F Ferien oder Urlaub, in der Ergotherapie zusätzlich U. |
| Dringlicher Behandlungsbedarf | Ärztliches Kennzeichen, das den spätesten Behandlungsbeginn von 28 auf 14 Kalendertage verkürzt. |

## Erbringung

| Begriff | Bedeutung |
|---|---|
| Zertifikatsleistung | Eine Leistung, die nur mit nachgewiesener Zusatzqualifikation und personengebundener Abrechnungserlaubnis erbracht werden darf. Manuelle Therapie, Lymphdrainage, Gerät, KG-ZNS. |
| Abrechnungserlaubnis | Die personenbezogene Freigabe durch die Arbeitsgemeinschaft nach § 124 Abs. 2 SGB V, eine Zertifikatsleistung abzurechnen. |
| Praxisbesuch | Die Behandlung in den zugelassenen Praxisräumen. Der Regelfall. |
| Hausbesuch | Die Behandlung in der häuslichen Umgebung. Nur zulässig, wenn die Praxis aus medizinischen Gründen nicht aufgesucht werden kann. |
| Ausfallhonorar | Das privatrechtlich vereinbarte Entgelt für einen nicht rechtzeitig abgesagten Termin. Stützt sich auf Annahmeverzug nach § 615 BGB und ist in keiner Richtlinie geregelt. |
| Therapiebericht | Der auf dem Vordruck angeforderte schriftliche Bericht über den Therapieverlauf. |
| Heilmittelkatalog | Der zweite Teil der Heilmittel-Richtlinie. Ordnet jeder Diagnosegruppe die verordnungsfähigen Heilmittel, die Höchstmenge, die orientierende Menge und die Frequenzempfehlung zu. |

## Begriffe, die veraltet sind

Diese tauchen in älteren Quellen und in Praxissoftware auf. Sie werden im Code
**nicht** verwendet.

| Begriff | Warum veraltet |
|---|---|
| Regelfall | Systematik der alten Richtlinie. Seit 01.10.2020 abgelöst durch Verordnungsfall, orientierende Behandlungsmenge und Höchstmenge je Verordnung. |
| Außerhalb des Regelfalls | Ebenfalls seit 01.10.2020 entfallen. Weitere Verordnungen über die orientierende Menge hinaus sind heute ohne Genehmigung möglich. |

## Begriffe dieses Projekts

| Begriff | Bedeutung |
|---|---|
| Mandant | Eine Praxis. Daten eines Mandanten sind für andere Mandanten unsichtbar. |
| Slot | Ein buchbarer Zeitabschnitt. Ergebnis der Schnittmenge aus Therapeut, Raum, Patientenwunsch und Verordnung. |
| Pruefergebnis | Das Ergebnis einer Regelprüfung. Trägt immer eine Begründung, nie nur ein Ja oder Nein. |
