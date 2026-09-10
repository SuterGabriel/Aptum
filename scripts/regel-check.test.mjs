// Tests für den Regel-Check.
//
// Warum ein Gate Tests braucht: Zwei Lücken in regel-check.mjs haben nichts
// gemeldet. Eine Konstante borgte sich die Fundstelle ihres Nachbarn, eine
// andere die aus dem Klassenkommentar. Beide Läufe waren grün. Aufgefallen
// sind sie nur, weil jemand von Hand kaputtgemacht hat, was hätte gemeldet
// werden müssen — und einmal fast nicht, weil ein Filter die Ausgabe
// abgeschnitten hatte.
//
// Ein Gate, das nichts meldet, sieht aus wie ein Gate, das zufrieden ist.
// Diese Datei macht aus der Gegenprobe von Hand eine, die bei jedem Lauf
// mitläuft. Jeder Fall unter fixtures/regel-check/ ist eine Lücke, die es
// gab, oder ein Verhalten, das nicht verloren gehen darf.
//
// Aufruf: node --test scripts/regel-check.test.mjs

import { test } from 'node:test';
import assert from 'node:assert/strict';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const hier = dirname(fileURLToPath(import.meta.url));
const SKRIPT = join(hier, 'regel-check.mjs');
const FIXTURES = join(hier, 'fixtures', 'regel-check');
const KATALOGE = join(FIXTURES, 'katalog');
const DOMAIN = join(FIXTURES, 'domain');

// Ohne Dateien wird nur der Katalog geprüft. Sonst liefe der echte Domänencode
// gegen den Fixture-Katalog — und jede echte Fundstelle wäre dort unbekannt.
function lauf({ katalog = 'gut.md', dateien = [] } = {}) {
  const ziele = dateien.length > 0 ? dateien.map((d) => join(DOMAIN, d)) : ['--nur-katalog'];
  const ergebnis = spawnSync(
    process.execPath,
    [SKRIPT, ...ziele],
    {
      env: { ...process.env, REGEL_KATALOG: join(KATALOGE, katalog) },
      encoding: 'utf8',
    },
  );
  return { status: ergebnis.status, aus: `${ergebnis.stdout}${ergebnis.stderr}` };
}

function erwarteBefund({ katalog, dateien }, ...erwartet) {
  const { status, aus } = lauf({ katalog, dateien });
  assert.equal(status, 1, `Exit-Code 1 erwartet, Ausgabe:\n${aus}`);
  for (const text of erwartet) {
    assert.match(aus, new RegExp(text), `erwartet "${text}" in:\n${aus}`);
  }
  return aus;
}

function erwarteGruen({ katalog, dateien }) {
  const { status, aus } = lauf({ katalog, dateien });
  assert.equal(status, 0, `Exit-Code 0 erwartet, Ausgabe:\n${aus}`);
  assert.doesNotMatch(aus, /BEFUND/, `kein Befund erwartet, Ausgabe:\n${aus}`);
  return aus;
}

// --- Teil A: der Katalog ----------------------------------------------------

test('Katalog: ein gültiger Katalog ist grün', () => {
  const aus = erwarteGruen({});
  assert.match(aus, /3 Regeln — 2 belegt, 1 unsicher/);
});

test('Katalog: eine Tabelle in einem unnummerierten Abschnitt ist keine Regeltabelle', () => {
  // gut.md hat unter "Quellen" eine Tabelle ohne IDs. Sie darf nicht gemeldet werden.
  const aus = erwarteGruen({});
  assert.doesNotMatch(aus, /Regelzeile ohne ID/);
});

test('Katalog: doppelte ID', () => {
  erwarteBefund({ katalog: 'doppelte-id.md' }, 'HM-FRIST-01 ist doppelt vergeben');
});

test('Katalog: unbekannter Status', () => {
  erwarteBefund({ katalog: 'falscher-status.md' }, 'hat den Status "VIELLEICHT"');
});

test('Katalog: Regelzeile ohne ID in einem nummerierten Abschnitt', () => {
  erwarteBefund({ katalog: 'zeile-ohne-id.md' }, 'Regelzeile ohne ID');
});

test('Katalog: ID mit falscher Form', () => {
  erwarteBefund({ katalog: 'falsche-id-form.md' }, 'hat nicht die Form');
});

// --- Teil B: der Code, Fälle die gemeldet werden müssen ---------------------

test('Code: nackte Zahl ohne Fundstelle', () => {
  erwarteBefund({ dateien: ['Nackt.java'] }, 'Zahl ohne Fundstelle: 99');
});

test('Code: Nachbarkonstante borgt sich keine Fundstelle (stumme Lücke 1)', () => {
  const aus = erwarteBefund({ dateien: ['NachbarBorgt.java'] }, 'Zahl ohne Fundstelle: 77');
  assert.doesNotMatch(aus, /Fundstelle: 28/, 'die belegte 28 darf nicht gemeldet werden');
});

test('Code: Klassenkommentar deckt keine Konstante (stumme Lücke 2)', () => {
  erwarteBefund({ dateien: ['KlassenkommentarBorgt.java'] }, 'Zahl ohne Fundstelle: 77');
});

test('Code: unsichere Quelle als Konstante', () => {
  erwarteBefund(
    { dateien: ['UnsichereKonstante.java'] },
    'Zahl 170 stützt sich auf HM-QUAL-02 mit Status UNSICHER',
  );
});

test('Code: unbekannte ID', () => {
  erwarteBefund({ dateien: ['UnbekannteId.java'] }, 'HM-FRIST-99 steht nicht in gut.md');
});

test('Code: Wert passt nicht zur Fundstelle (Zahlendreher)', () => {
  erwarteBefund({ dateien: ['FalscherWert.java'] }, 'Zahl 82 kommt in HM-FRIST-01 .* nicht vor');
});

// --- Teil B: der Code, Fälle die still bleiben müssen -----------------------

test('Code: belegte Konstante ist grün', () => {
  erwarteGruen({ dateien: ['Belegt.java'] });
});

test('Code: mehrzeilige Deklaration findet ihre Fundstelle', () => {
  erwarteGruen({ dateien: ['Mehrzeilig.java'] });
});

test('Code: eine unsichere Zeile darf zitiert werden', () => {
  erwarteGruen({ dateien: ['UnsicherZitiert.java'] });
});

test('Code: 0, 1 und 2 sind ausgenommen', () => {
  erwarteGruen({ dateien: ['Ausgenommen.java'] });
});

test('Code: Zahlen in Strings und Kommentaren sind keine Konstanten', () => {
  erwarteGruen({ dateien: ['StringUndKommentar.java'] });
});

test('Code: Fundstelle in Javadoc-Auszeichnung wird erkannt', () => {
  erwarteGruen({ dateien: ['JavadocAuszeichnung.java'] });
});

test('Code: Testquellen werden nicht geprüft', () => {
  const aus = erwarteGruen({ dateien: ['src/test/java/Testquelle.java'] });
  assert.match(aus, /keine Klassen/, 'die Testquelle darf gar nicht erst gezählt werden');
});

// --- Alle Fälle zusammen: jeder Befund genau einmal -------------------------

test('Code: alle Fixtures auf einmal ergeben genau die erwarteten Befunde', () => {
  const alle = [
    'Nackt.java', 'Belegt.java', 'NachbarBorgt.java', 'KlassenkommentarBorgt.java',
    'Mehrzeilig.java', 'UnsichereKonstante.java', 'UnsicherZitiert.java',
    'UnbekannteId.java', 'FalscherWert.java', 'Ausgenommen.java',
    'StringUndKommentar.java', 'JavadocAuszeichnung.java',
  ];
  const { status, aus } = lauf({ dateien: alle });
  assert.equal(status, 1);
  const anzahl = (aus.match(/^\s+BEFUND /gm) || []).length;
  assert.equal(anzahl, 6, `sechs Befunde erwartet, Ausgabe:\n${aus}`);
});

// --- Und der Ernstfall: das echte Repo --------------------------------------

test('Repo: der echte Katalog und der echte Domänenkern sind grün', () => {
  const ergebnis = spawnSync(process.execPath, [SKRIPT], { encoding: 'utf8' });
  const aus = `${ergebnis.stdout}${ergebnis.stderr}`;
  assert.equal(ergebnis.status, 0, aus);
  assert.match(aus, /65 Regeln/);
  assert.match(aus, /Klassen im Domänenkern geprüft/);
});
