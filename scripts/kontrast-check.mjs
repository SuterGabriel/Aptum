#!/usr/bin/env node
// Kontrast-Check
//
// Der Skill a11y-grid verspricht: "Kontrastwerte werden aus den Farbtoken
// nachgerechnet, nicht geschätzt." Dieses Skript löst das Versprechen ein.
//
// Es liest frontend/src/styles/tokens.css, sammelt die Farbtoken und prüft
// die Paare, die in derselben Datei als Kommentar stehen:
//
//   /* @kontrast --text-secondary auf --surface-page >= 4.5 */
//
// Die Paare stehen bewusst in der Token-Datei und nicht in einer zweiten
// Konfiguration: Wer eine Farbe ändert, sieht die Anforderung daneben.

import { readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { dirname, join } from 'node:path';

const wurzel = join(dirname(fileURLToPath(import.meta.url)), '..');
const tokenDatei = join(wurzel, 'frontend', 'src', 'styles', 'tokens.css');

/** Ein Farbkanal von sRGB in den linearen Raum, nach WCAG 2.1. */
function linear(kanal) {
  const c = kanal / 255;
  return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4;
}

/** Relative Leuchtdichte einer Farbe, nach WCAG 2.1. */
function leuchtdichte({ r, g, b }) {
  return 0.2126 * linear(r) + 0.7152 * linear(g) + 0.0722 * linear(b);
}

/** Kontrastverhältnis zweier Farben, zwischen 1 und 21. */
function verhaeltnis(farbeA, farbeB) {
  const a = leuchtdichte(farbeA);
  const b = leuchtdichte(farbeB);
  const hell = Math.max(a, b);
  const dunkel = Math.min(a, b);
  return (hell + 0.05) / (dunkel + 0.05);
}

function hexZuRgb(hex) {
  const kurz = /^#([0-9a-f])([0-9a-f])([0-9a-f])$/i.exec(hex);
  if (kurz) {
    const [, r, g, b] = kurz;
    return { r: Number.parseInt(r + r, 16), g: Number.parseInt(g + g, 16), b: Number.parseInt(b + b, 16) };
  }
  const lang = /^#([0-9a-f]{2})([0-9a-f]{2})([0-9a-f]{2})$/i.exec(hex);
  if (!lang) return null;
  return { r: Number.parseInt(lang[1], 16), g: Number.parseInt(lang[2], 16), b: Number.parseInt(lang[3], 16) };
}

let quelle;
try {
  quelle = readFileSync(tokenDatei, 'utf8');
} catch {
  console.error(`Token-Datei nicht gefunden: ${tokenDatei}`);
  process.exit(1);
}

// Farbtoken sammeln. Nur Werte, die eine Hex-Farbe sind — Muster, Abstände
// und Schriftgrößen sind ebenfalls Token, aber hier nicht zu prüfen.
const farben = new Map();
for (const treffer of quelle.matchAll(/(--[\w-]+):\s*(#[0-9a-fA-F]{3,6});/g)) {
  const rgb = hexZuRgb(treffer[2]);
  if (rgb) farben.set(treffer[1], { hex: treffer[2].toLowerCase(), rgb });
}

const paare = [...quelle.matchAll(
  /@kontrast\s+(--[\w-]+)\s+auf\s+(--[\w-]+)\s*>=\s*([\d.]+)/g
)].map(([, vordergrund, hintergrund, schwelle]) => ({
  vordergrund,
  hintergrund,
  schwelle: Number.parseFloat(schwelle),
}));

console.log('');
console.log('Kontrast-Check');
console.log('==============');
console.log('');
console.log(`${farben.size} Farbtoken, ${paare.length} geforderte Paare.`);
console.log('');

if (paare.length === 0) {
  console.error('Keine @kontrast-Angaben gefunden. Ohne Anforderung prüft dieses');
  console.error('Skript nichts und wäre nur Dekoration.');
  process.exit(1);
}

let fehler = 0;
const spalte = Math.max(...paare.map((p) => p.vordergrund.length + p.hintergrund.length)) + 6;

for (const { vordergrund, hintergrund, schwelle } of paare) {
  const vg = farben.get(vordergrund);
  const hg = farben.get(hintergrund);
  const beschreibung = `${vordergrund} auf ${hintergrund}`.padEnd(spalte);

  if (!vg || !hg) {
    const fehlend = !vg ? vordergrund : hintergrund;
    console.log(`  FEHLER ${beschreibung} Token ${fehlend} ist nicht definiert`);
    fehler += 1;
    continue;
  }

  const wert = verhaeltnis(vg.rgb, hg.rgb);
  const gerundet = Math.floor(wert * 100) / 100;
  if (gerundet >= schwelle) {
    console.log(`  ok     ${beschreibung} ${gerundet.toFixed(2)} : 1  (gefordert ${schwelle})`);
  } else {
    console.log(`  ZU WENIG ${beschreibung} ${gerundet.toFixed(2)} : 1  (gefordert ${schwelle})`);
    fehler += 1;
  }
}

console.log('');
console.log('--------------');
if (fehler > 0) {
  console.log(`${fehler} von ${paare.length} Paaren halten die Anforderung nicht ein.`);
  console.log('');
  console.log('Eine Farbe ändern oder die Anforderung begründet senken — aber nicht');
  console.log('stillschweigend beides lassen. Der Skill a11y-grid verspricht, dass');
  console.log('diese Zahlen stimmen.');
  process.exit(1);
}
console.log(`Alle ${paare.length} Paare halten die Anforderung ein.`);
