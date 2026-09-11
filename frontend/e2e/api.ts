import { APIRequestContext, expect } from '@playwright/test';

/** Montag, 2. März 2026 - die Woche, in der die Tests buchen. */
export const MONTAG = '2026-03-02';

/**
 * Bis wohin gesucht wird. Nicht bis Freitag: Gegen eine Datenbank, die
 * zwischen den Läufen bleibt, ist eine einzelne Woche irgendwann voll, und
 * der Test scheitert an den Daten statt am Code. In der CI mit frischer
 * Datenbank wäre das nie aufgefallen. Das Ende ist die Frist der Verordnung.
 */
const BIS = '2026-03-25';

const KOPF = { 'Content-Type': 'application/json', 'X-Mandant': 'praxis-a' };

/** Legt eine Verordnung an und gibt ihre Kennung zurück. Testdaten, erkennbar synthetisch. */
export async function verordnungAnlegen(api: APIRequestContext): Promise<string> {
  const antwort = await api.post('/api/verordnungen', {
    headers: KOPF,
    data: {
      ausstellungsdatum: '2026-02-27',
      dringlicherBedarf: false,
      diagnosegruppe: 'WS',
      verordneteEinheiten: 6,
      frequenzMin: 1,
      frequenzMax: 3,
    },
  });
  expect(antwort.status()).toBe(201);
  return (await antwort.json()).id as string;
}

/** Sucht in der Testwoche und bucht den ersten Vorschlag. */
export async function terminBuchen(
  api: APIRequestContext,
  verordnung: string,
): Promise<{ therapeut: string; beginn: string }> {
  const suche = await api.post('/api/termine/suche', {
    headers: KOPF,
    data: {
      verordnung,
      heilmittel: 'KG_EINZEL',
      von: MONTAG,
      bis: BIS,
      fruehestens: '09:00',
      spaetestens: '12:00',
      wochentage: ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY'],
    },
  });
  expect(suche.status()).toBe(200);
  const erster = (await suche.json()).vorschlaege[0];
  expect(erster, 'die Testwoche hat keinen freien Slot mehr').toBeTruthy();

  const buchung = await api.post('/api/termine', {
    headers: KOPF,
    data: {
      verordnung,
      heilmittel: 'KG_EINZEL',
      therapeut: erster.therapeut,
      raum: erster.raum,
      beginn: erster.beginn,
    },
  });
  expect(buchung.status()).toBe(201);
  return { therapeut: erster.therapeut, beginn: erster.beginn };
}
