#!/usr/bin/env node
// Regel-Check
//
// Der wichtigste Befund aus Stufe 0 lautet: Falscher Code fällt im Test auf,
// falsche Fachlogik nicht. Ein Test, der prüft, ob 28 Tage richtig gerechnet
// werden, ist grün, auch wenn die 28 erfunden ist.
//
// Der Skill heilmittel-domain zieht daraus eine harte Regel: Steht eine Zahl
// nicht mit Quelle und Status BELEGT in regeln.md, gehört sie nicht in den
// Code. Bisher war das eine Absichtserklärung. Dieses Skript macht daraus ein
// Gate.
//
// Es prüft zwei Dinge, und beide laufen unabhängig voneinander:
//
//   Teil A, der Katalog. Trägt jede Zeile in regeln.md eine eindeutige ID im
//   richtigen Format, und ist ihr Status einer der drei erlaubten?
//
//   Teil B, der Code. Im Domänenkern steht keine nackte Zahl. Jede fachliche
//   Zahl ist eine benannte Konstante mit `@fundstelle HM-...`; die genannte
//   Zeile existiert, ihr Status ist BELEGT, und die Zahl kommt in der Zeile
//   auch vor.
//
// Dieses Gate hat eine eigene Testsuite: `node --test scripts/regel-check.test.mjs`.
// Sie existiert, weil zwei Lücken in diesem Skript nichts gemeldet haben und
// nur durch Gegenproben von Hand aufgefallen sind. Ein Gate braucht keine
// Tests, weil es Fehler findet, sondern weil es sie übersehen kann, ohne dass
// jemand es merkt.
//
// Grundlage: `docs/adr/ADR-007-fundstellen-id-im-domaenenmodell.md`
//
// Aufruf:
//   node scripts/regel-check.mjs                 Katalog und der ganze Baum
//   node scripts/regel-check.mjs datei.java ...  Katalog und diese Dateien
//   node scripts/regel-check.mjs --nur-katalog   nur Teil A
//   REGEL_KATALOG=pfad/zu/katalog.md ...         anderer Katalog (für Tests)

import { readFileSync, readdirSync, statSync, existsSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join, relative, resolve, extname, basename } from 'node:path';

const wurzel = join(dirname(fileURLToPath(import.meta.url)), '..');
const katalogDatei = process.env.REGEL_KATALOG
  ? resolve(process.env.REGEL_KATALOG)
  : join(wurzel, '.claude', 'skills', 'heilmittel-domain', 'regeln.md');
const katalogName = basename(katalogDatei);

const ID_FORM = /^HM-[A-Z]+-\d{2}$/;
const ERLAUBTE_STATUS = ['BELEGT', 'UNSICHER', 'OFFEN'];

// Zahlen, die in jedem Code vorkommen und nie fachlich sind. Der Kompromiss
// ist benannt, nicht versehentlich: Ohne ihn meldet das Gate jede Schleife
// und jeden Indexzugriff, und nach der dritten Fehlmeldung wird es
// abgeschaltet.
const UNVERDAECHTIG = new Set(['0', '1', '2']);

const befunde = [];
function befund(ort, text, hinweis) {
  befunde.push({ ort, text, hinweis });
}

// --- Teil A: der Katalog ----------------------------------------------------

function katalogLesen() {
  const katalog = new Map();
  const inhalt = readFileSync(katalogDatei, 'utf8');
  const zeilen = inhalt.split(/\r?\n/);

  let abschnitt = '';
  let imCodeblock = false;

  zeilen.forEach((zeile, index) => {
    if (/^\s*```/.test(zeile)) {
      imCodeblock = !imCodeblock;
      return;
    }
    if (imCodeblock) return;

    const ueberschrift = /^##\s+(.*)$/.exec(zeile);
    if (ueberschrift) {
      abschnitt = ueberschrift[1].trim();
      return;
    }
    if (!zeile.startsWith('|')) return;
    if (/^\|[-: |]*\|$/.test(zeile)) return;

    const spalten = zeile.split('|').slice(1, -1).map((s) => s.trim());
    if (spalten.length < 3) return;

    const erste = spalten[0].replace(/`/g, '').trim();
    if (erste === 'ID') return;                    // Kopfzeile
    if (!/^HM-/.test(erste)) {
      // Eine Tabelle ohne IDs. Das sind die erklärenden Tabellen im Anhang,
      // die keine Regeln tragen — nur Zeilen in nummerierten Abschnitten
      // sind Regeln.
      if (/^\d+\./.test(abschnitt)) {
        befund(
          `${katalogName}:${index + 1}`,
          'Regelzeile ohne ID',
          `Abschnitt "${abschnitt}" — jede Regelzeile braucht eine ID HM-<BEREICH>-<NN>`,
        );
      }
      return;
    }

    if (!ID_FORM.test(erste)) {
      befund(`${katalogName}:${index + 1}`, `ID "${erste}" hat nicht die Form HM-<BEREICH>-<NN>`, '');
      return;
    }
    if (katalog.has(erste)) {
      befund(
        `${katalogName}:${index + 1}`,
        `ID ${erste} ist doppelt vergeben`,
        `zuerst in Zeile ${katalog.get(erste).zeile}. IDs werden nie neu vergeben.`,
      );
      return;
    }

    // Der Status steht in der letzten Spalte. Er trägt oft einen Zusatz:
    // "BELEGT als Nichtfund", "UNSICHER — Widerspruch, siehe unten".
    // Maßgeblich ist das erste Wort.
    const roh = spalten[spalten.length - 1].replace(/[*`]/g, '').trim();
    const status = (roh.split(/[\s,—–-]+/)[0] || '').toUpperCase();

    if (!ERLAUBTE_STATUS.includes(status)) {
      befund(
        `${katalogName}:${index + 1}`,
        `${erste} hat den Status "${roh}"`,
        `erlaubt sind ${ERLAUBTE_STATUS.join(', ')}`,
      );
      return;
    }

    // Die Zahlen der Zeile, für den Wertabgleich in Teil B. Nur aus den
    // Spalten Regel und Wert — die Fundstelle trägt Paragraphennummern, die
    // keine Werte sind: "§ 15 Abs. 1" darf eine 15 im Code nicht decken.
    const zahlen = new Set(
      [...`${spalten[1]} ${spalten[2]}`.matchAll(/(?<!\d)(\d+)(?!\d)/g)].map((t) => t[1]),
    );

    katalog.set(erste, { zeile: index + 1, status, roh, abschnitt, regel: spalten[1], zahlen });
  });

  return katalog;
}

// --- Teil B: der Code -------------------------------------------------------

const UEBERSPRINGEN = new Set(['.git', 'node_modules', 'target', 'build', 'out']);

function javaSammeln(verzeichnis, gesammelt = []) {
  if (!existsSync(verzeichnis)) return gesammelt;
  for (const eintrag of readdirSync(verzeichnis)) {
    if (UEBERSPRINGEN.has(eintrag)) continue;
    const pfad = join(verzeichnis, eintrag);
    if (statSync(pfad).isDirectory()) javaSammeln(pfad, gesammelt);
    else if (extname(pfad) === '.java') gesammelt.push(pfad);
  }
  return gesammelt;
}

/**
 * Das ganze Domain-Modul, nicht nur der Ordner `regel`.
 *
 * Der erste Zuschnitt sah nur `/domain/regel/` an. Die Höchstmengen des
 * Heilmittelkatalogs liegen aber an der Diagnosegruppe, also im Modell — und
 * wären damit als einzige Fachzahlen ungeprüft geblieben. Eine Zahl aus der
 * Richtlinie ist überall im Domänenkern belegpflichtig, nicht nur dort, wo
 * der Ordner danach heißt.
 *
 * Testquellen sind ausgenommen, und das ist kein Schlupfloch: Die Grenzwerte
 * in einem Grenzfalltest — Tag 28 zulässig, Tag 29 nicht — sind der Zweck
 * des Tests. Eine Fundstelle daneben zu verlangen, wäre Lärm, und Testcode
 * wird nicht ausgeliefert.
 */
function istRegelklasse(pfad) {
  const p = pfad.replace(/\\/g, '/');
  return p.includes('/domain/') && !p.includes('/src/test/');
}

// Eine Fundstelle steht oft mitten in Javadoc: `{@code @fundstelle HM-X}`,
// in Klammern, am Satzende. Die Auszeichnung drumherum gehört nicht zur ID —
// sonst zwingt das Gate dazu, Javadoc schlechter zu schreiben.
function idAusZeile(roh) {
  return roh.replace(/^[`*("'[{<]+/, '').replace(/[`*.,;:)\]}>"']+$/, '');
}

function codePruefen(pfad, katalog) {
  const inhalt = readFileSync(pfad, 'utf8');
  const zeilen = inhalt.split(/\r?\n/);
  const ort = relative(wurzel, pfad).replace(/\\/g, '/');

  // Jede genannte ID muss es geben. Der Status wird hier absichtlich nicht
  // geprüft: ADR-007 verbietet, eine unsichere Quelle als *Konstante* in den
  // Code zu nehmen — sie zu nennen, um zu erklären, warum dort keine Zahl
  // steht, ist genau das gewünschte Verhalten. Der Status wird deshalb erst
  // unten geprüft, wo eine Fundstelle tatsächlich eine Zahl begründet.
  zeilen.forEach((zeile, index) => {
    for (const treffer of zeile.matchAll(/@fundstelle\s+(\S+)/g)) {
      const id = idAusZeile(treffer[1]);
      if (!katalog.has(id)) {
        befund(
          `${ort}:${index + 1}`,
          `@fundstelle ${id} steht nicht in ${katalogName}`,
          'Entweder die ID ist falsch geschrieben, oder die Regel ist nicht recherchiert.',
        );
      }
    }
  });

  // Nackte Zahlen. Eine Deklaration ist erlaubt, wenn über ihr eine Fundstelle
  // steht — und die muss BELEGT sein und die Zahl auch tragen.
  let imBlockkommentar = false;
  zeilen.forEach((zeile, index) => {
    const nurCode = zeile.replace(/"[^"]*"/g, '""');

    if (/\/\*/.test(nurCode) && !/\*\//.test(nurCode)) imBlockkommentar = true;
    else if (/\*\//.test(nurCode)) { imBlockkommentar = false; return; }
    if (imBlockkommentar || /^\s*(\*|\/\/)/.test(nurCode)) return;

    const zahlen = [...nurCode.matchAll(/(?<![\w.])(\d+)(?![\w.])/g)]
      .map((t) => t[1])
      .filter((z) => !UNVERDAECHTIG.has(z));
    if (zahlen.length === 0) return;

    // Eine Fundstelle gilt für die Deklaration, die auf sie folgt — und nur
    // bis zur nächsten Grenze darüber. Grenzen sind das Ende der vorherigen
    // Anweisung (`;` oder `,`) und der Beginn eines Blocks (`{`).
    //
    // Beide sind teuer erkauft. Ohne die erste borgt sich in einer dicht
    // gepackten Aufzählung jede Konstante die Quelle ihres Vorgängers. Ohne die
    // zweite borgt sich die erste Konstante einer Klasse eine Fundstelle aus
    // dem Klassenkommentar, wo sie oft nur zitiert wird. Beide Lücken waren da,
    // beide haben nichts gemeldet, und beide sind nur durch eine Gegenprobe
    // aufgefallen. Die Testsuite hält sie fest.
    const umgebung = [];
    for (let zurueck = index - 1; zurueck >= 0 && index - zurueck <= 10; zurueck--) {
      const vorher = zeilen[zurueck].replace(/"[^"]*"/g, '""').trim();
      if (/[,;{]$/.test(vorher)) break;
      umgebung.unshift(zeilen[zurueck]);
    }
    umgebung.push(zeile);
    const genannt = [...umgebung.join('\n').matchAll(/@fundstelle\s+(\S+)/g)]
      .map((t) => idAusZeile(t[1]));

    if (genannt.length === 0) {
      befund(
        `${ort}:${index + 1}`,
        `Zahl ohne Fundstelle: ${zahlen.join(', ')}`,
        'Im Domänenkern steht keine nackte Zahl. Benannte Konstante mit '
        + '/** @fundstelle HM-... */ darüber — oder die Zahl gehört nicht hierher.',
      );
      return;
    }

    const bekannt = genannt.filter((id) => katalog.has(id));
    if (bekannt.length === 0) return; // die unbekannte ID ist oben schon gemeldet

    for (const id of bekannt) {
      const eintrag = katalog.get(id);
      if (eintrag.status !== 'BELEGT') {
        befund(
          `${ort}:${index + 1}`,
          `Zahl ${zahlen.join(', ')} stützt sich auf ${id} mit Status ${eintrag.roh}`,
          'Nur BELEGT darf als Konstante in den Code. Bei UNSICHER wird die Regel '
          + 'ein Parameter je Praxis — siehe die Ausfallregel als Vorbild.',
        );
        return;
      }
    }

    // Der Wertabgleich. Er ist bewusst grob: Die Zahl muss als ganzes Wort in
    // der Regel- oder Wertspalte der genannten Zeile vorkommen. Das fängt den
    // Zahlendreher, den Verweis auf die falsche Zeile und die Drift nach einem
    // neuen Rechtsstand — nicht aber eine Zeile, die zufällig dieselbe Zahl in
    // anderem Zusammenhang trägt. Grob ist besser als gar nicht: Vor diesem
    // Abgleich ging eine 82 mit der Fundstelle "28 Kalendertage" durch.
    for (const zahl of zahlen) {
      const gedeckt = bekannt.some((id) => katalog.get(id).zahlen.has(zahl));
      if (!gedeckt) {
        const zeilenText = bekannt.map((id) => `${id} („${katalog.get(id).regel}")`).join(', ');
        befund(
          `${ort}:${index + 1}`,
          `Zahl ${zahl} kommt in ${zeilenText} nicht vor`,
          'Entweder ist der Wert falsch, oder die Fundstelle zeigt auf die falsche Zeile, '
          + 'oder der Wert ist umgerechnet — dann gehört die Umrechnung in den Katalog.',
        );
      }
    }
  });
}

// --- Lauf -------------------------------------------------------------------

console.log('');
console.log('Regel-Check');
console.log('===========');
console.log('');

const katalog = katalogLesen();
const nachStatus = { BELEGT: 0, UNSICHER: 0, OFFEN: 0 };
for (const { status } of katalog.values()) nachStatus[status] += 1;

console.log(
  `Katalog: ${katalog.size} Regeln — ${nachStatus.BELEGT} belegt, `
  + `${nachStatus.UNSICHER} unsicher, ${nachStatus.OFFEN} offen.`,
);

const argumente = process.argv.slice(2);
const nurKatalog = argumente.includes('--nur-katalog');
const dateiArgumente = argumente.filter((a) => !a.startsWith('--'));
const alleJava = nurKatalog
  ? []
  : dateiArgumente.length > 0
    ? dateiArgumente.map((p) => resolve(p)).filter((p) => extname(p) === '.java')
    : javaSammeln(join(wurzel, 'services'));
const regelklassen = alleJava.filter(istRegelklasse);

for (const pfad of regelklassen) codePruefen(pfad, katalog);

if (regelklassen.length === 0) {
  console.log('Code:    keine Klassen unter services/**/domain/ — Teil B hat nichts zu');
  console.log('         prüfen. Das ist kein grünes Ergebnis, sondern ein leerer Baum.');
} else {
  console.log(`Code:    ${regelklassen.length} Klassen im Domänenkern geprüft.`);
}
console.log('');

if (befunde.length === 0) {
  console.log(`Alle ${katalog.size} Regeln tragen eine eindeutige ID mit gültigem Status.`);
  process.exit(0);
}

for (const { ort, text, hinweis } of befunde) {
  console.log(`  BEFUND ${ort}`);
  console.log(`         ${text}`);
  if (hinweis) console.log(`         ${hinweis}`);
}

console.log('');
console.log('-----------');
console.log(`${befunde.length} ${befunde.length === 1 ? 'Befund' : 'Befunde'}.`);
console.log('');
console.log('Falscher Code fällt im Test auf, falsche Fachlogik nicht. Ein Test über');
console.log('eine erfundene Frist ist grün. Deshalb steht die Fundstelle im Code und');
console.log('nicht nur im Kopf dessen, der ihn geschrieben hat.');
process.exit(1);
