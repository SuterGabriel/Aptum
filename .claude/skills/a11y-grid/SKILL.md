---
name: a11y-grid
description: Barrierefreiheit für das Kalender-Grid und alle interaktiven Komponenten - Roving Tabindex, ARIA-Rollen fürs Zeitgitter, Live-Regions nach Buchungen, Focus Trap im Dialog, Kontrastberechnung aus Farbtoken. Nutze diesen Skill bei jeder neuen oder geänderten UI-Komponente, bei Tastaturnavigation, ARIA, Fokusverwaltung, Farben und bei allem was WCAG betrifft.
---

# Barrierefreiheit

WCAG steht bei SOLCOM als Nice-to-have. Fast niemand kann es belegen. Hier ist
es automatisiert. Das ist der Punkt, an dem dieses Projekt konkurrenzlos ist.

## Die harte Regel

> Jede neue interaktive Komponente braucht einen axe-Test, bevor sie gemerged
> wird. Kein Test, kein Merge.

## Kalender-Grid

Ein Zeitgitter ist keine Tabelle mit Klick-Handlern. Es ist ein Grid-Widget.

- **Rollen:** grid außen, row je Zeile, gridcell je Zelle, columnheader für
  die Überschriften.
- **Roving Tabindex:** Genau eine Zelle im Grid hat tabindex 0, alle anderen
  minus 1. Die Tabulatortaste springt ins Grid und wieder heraus, die Pfeile
  bewegen sich darin. Ein Grid mit fünfhundert tabbaren Zellen ist mit der
  Tastatur unbenutzbar.
- **Tastatur:** Pfeile bewegen, Home und End an Zeilenanfang und -ende, mit
  Steuerungstaste an Gitteranfang und -ende, Eingabetaste und Leertaste buchen,
  Escape schließt den Dialog.
- **Beschriftung:** Jede Zelle braucht einen zugänglichen Namen, der Zeit und
  Zustand nennt. "Dienstag, 14 Uhr, frei", nicht nur "14 Uhr".
- **Nie Farbe allein.** Belegt, frei und gesperrt unterscheiden sich zusätzlich
  durch Text oder Muster.

## Nach jeder Buchung

Eine höflich gesetzte Live-Region meldet das Ergebnis. Wer nicht sieht, dass
die Zelle sich gefärbt hat, erfährt sonst nichts. Bei Fehlern die
Alert-Rolle statt der höflichen Region.

## Dialoge

Dialog-Rolle mit modaler Kennzeichnung, Focus Trap, Fokus beim Öffnen auf das
erste sinnvolle Element, beim Schließen zurück auf den Auslöser. Der letzte
Teil wird fast immer vergessen.

## Farben

Kontrastwerte werden **aus den Farbtoken nachgerechnet, nicht geschätzt**. Der
CI-Job liest die Token und rechnet das Verhältnis aus. Mindestens 4.5 zu 1 für
Text, 3 zu 1 für große Schrift und für die Grenzen von Bedienelementen.

Das ist der seltene Teil. Fast alle Behauptungen von WCAG-Konformität beruhen
auf einem einmaligen manuellen Blick.

## Bewegung

Die Systemeinstellung für reduzierte Bewegung wird respektiert. Übergänge im
Grid werden dann abgeschaltet, nicht nur verkürzt.
