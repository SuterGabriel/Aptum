import { Belegung, Spalte, Woche } from '../api/aptum-api';
import { WOCHENTAGE, tagePlus } from '../zeit';

/** Die sechs Zustände einer Zelle. Frei ist der einzige, den das Backend nicht liefert. */
export type Zustand = 'frei' | 'belegt' | 'ruestzeit' | 'nachruhe' | 'abwesenheit' | 'gesperrt';

export const ZUSTAND_LABEL: Record<Zustand, string> = {
  frei: 'frei',
  belegt: 'belegt',
  ruestzeit: 'Rüstzeit',
  nachruhe: 'Nachruhe',
  abwesenheit: 'abwesend',
  gesperrt: 'gesperrt',
};

export interface Zelle {
  /** ISO-Zeitpunkt der Praxis, z. B. 2026-03-02T09:15 - der Schlüssel der Zelle. */
  readonly beginn: string;
  readonly uhrzeit: string;
  readonly therapeut: string;
  readonly zustand: Zustand;
  readonly text: string;
  readonly raum?: string;
  /** Erste Zelle eines Blocks: Nur sie trägt den sichtbaren Text. */
  readonly blockbeginn: boolean;
  /** Zugänglicher Name: Tag, Uhrzeit, Zustand, und was dazu gehört. */
  readonly name: string;
}

export interface Zeile {
  readonly uhrzeit: string;
  readonly volleStunde: boolean;
  readonly zellen: readonly Zelle[];
}

export interface Raster {
  readonly tag: string;
  readonly wochentag: string;
  readonly therapeuten: readonly string[];
  readonly zeilen: readonly Zeile[];
}

const RASTER_MINUTEN = 15;

/**
 * Aus der Woche des Backends das Gitter eines Tages.
 *
 * Keine Datumsrechnung: Das Backend liefert jede Belegung in der Wandzeit
 * der Praxis, und ISO-Zeitstempel sind als Text vergleichbar. Eine Zelle
 * gehört zu einer Belegung, wenn ihr Schlüssel zwischen deren von und bis
 * liegt - lexikografisch, nicht als Date. So kann die Zone des Browsers
 * nichts verschieben.
 */
export function rasterFuer(woche: Woche, tag: string): Raster {
  const spalten = woche.spalten ?? [];
  const index = Math.max(0, Math.round(tageZwischen(woche.montag ?? tag, tag)));
  const wochentag = WOCHENTAGE[Math.min(index, WOCHENTAGE.length - 1)].lang;
  const zeiten = uhrzeiten(woche.tagesbeginn ?? '07:00', woche.tagesende ?? '19:00');

  const zeilen = zeiten.map((uhrzeit) => ({
    uhrzeit,
    volleStunde: uhrzeit.endsWith(':00'),
    zellen: spalten.map((s) => zelle(s, tag, uhrzeit, wochentag)),
  }));

  return { tag, wochentag, therapeuten: spalten.map((s) => s.therapeut ?? ''), zeilen };
}

function zelle(spalte: Spalte, tag: string, uhrzeit: string, wochentag: string): Zelle {
  const beginn = `${tag}T${uhrzeit}`;
  const therapeut = spalte.therapeut ?? '';
  const belegung = (spalte.belegungen ?? []).find((b) => deckt(b, beginn));
  if (!belegung) {
    return {
      beginn,
      uhrzeit,
      therapeut,
      zustand: 'frei',
      text: '',
      blockbeginn: true,
      name: `${wochentag}, ${uhrzeit}, ${therapeut}, frei`,
    };
  }
  const zustand = zustandVon(belegung.art);
  const blockbeginn =
    wandzeit(belegung.von) <= beginn && wandzeit(belegung.von) > vorher(tag, uhrzeit);
  const raum = belegung.raum ?? undefined;
  const teile = [wochentag, uhrzeit, therapeut, ZUSTAND_LABEL[zustand]];
  if (belegung.text) teile.push(belegung.text);
  if (raum) teile.push(raum);
  return {
    beginn,
    uhrzeit,
    therapeut,
    zustand,
    text: belegung.text ?? '',
    raum,
    blockbeginn,
    name: teile.join(', '),
  };
}

function deckt(b: Belegung, beginn: string): boolean {
  return wandzeit(b.von) <= beginn && beginn < wandzeit(b.bis);
}

/** 2026-03-02T09:00:00+01:00 wird zu 2026-03-02T09:00 - Minutengenauigkeit reicht dem Raster. */
function wandzeit(iso: string | undefined): string {
  return (iso ?? '').slice(0, 16);
}

function vorher(tag: string, uhrzeit: string): string {
  const [h, m] = uhrzeit.split(':').map(Number);
  const gesamt = h * 60 + m - RASTER_MINUTEN;
  if (gesamt < 0) return `${tag}T00:00`;
  return `${tag}T${String(Math.floor(gesamt / 60)).padStart(2, '0')}:${String(gesamt % 60).padStart(2, '0')}`;
}

function zustandVon(art: string | undefined): Zustand {
  switch (art) {
    case 'BELEGT':
      return 'belegt';
    case 'RUESTZEIT':
      return 'ruestzeit';
    case 'NACHRUHE':
      return 'nachruhe';
    case 'ABWESENHEIT':
      return 'abwesenheit';
    default:
      return 'gesperrt';
  }
}

function uhrzeiten(von: string, bis: string): string[] {
  const [vh, vm] = von.split(':').map(Number);
  const [bh, bm] = bis.split(':').map(Number);
  const liste: string[] = [];
  for (let t = vh * 60 + vm; t < bh * 60 + bm; t += RASTER_MINUTEN) {
    liste.push(`${String(Math.floor(t / 60)).padStart(2, '0')}:${String(t % 60).padStart(2, '0')}`);
  }
  return liste;
}

function tageZwischen(von: string, bis: string): number {
  let n = 0;
  let t = von;
  while (t < bis && n < 7) {
    t = tagePlus(t, 1);
    n++;
  }
  return n;
}
