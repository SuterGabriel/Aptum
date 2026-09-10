import { Wochentag } from './api/aptum-api';

/** Wochentage in der Reihenfolge der Woche, mit Kurz- und Langform. */
export const WOCHENTAGE: readonly { code: Wochentag; kurz: string; lang: string }[] = [
  { code: 'MONDAY', kurz: 'Mo', lang: 'Montag' },
  { code: 'TUESDAY', kurz: 'Di', lang: 'Dienstag' },
  { code: 'WEDNESDAY', kurz: 'Mi', lang: 'Mittwoch' },
  { code: 'THURSDAY', kurz: 'Do', lang: 'Donnerstag' },
  { code: 'FRIDAY', kurz: 'Fr', lang: 'Freitag' },
  { code: 'SATURDAY', kurz: 'Sa', lang: 'Samstag' },
];

/** ISO-Datum (JJJJ-MM-TT) um Tage verschoben, ohne Zeitzone - reine Kalenderrechnung. */
export function tagePlus(isoDatum: string, tage: number): string {
  const [j, m, t] = isoDatum.split('-').map(Number);
  const d = new Date(Date.UTC(j, m - 1, t + tage));
  return d.toISOString().slice(0, 10);
}

/** Der Montag der Woche, in der das Datum liegt. */
export function montagVon(isoDatum: string): string {
  const [j, m, t] = isoDatum.split('-').map(Number);
  const wochentag = new Date(Date.UTC(j, m - 1, t)).getUTCDay(); // 0 = Sonntag
  const zurueck = wochentag === 0 ? 6 : wochentag - 1;
  return tagePlus(isoDatum, -zurueck);
}

/** Das heutige Datum als ISO, in der Zone des Browsers - nur als Startwert. */
export function heute(): string {
  const d = new Date();
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')}`;
}
