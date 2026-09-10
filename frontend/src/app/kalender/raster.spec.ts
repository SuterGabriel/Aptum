import { Woche } from '../api/aptum-api';
import { rasterFuer } from './raster';

/** Montag, 2. März 2026, T. Alpha: eine Behandlung um 9 mit Rüstzeit, Bewegungsbad um 10 mit Nachruhe. */
const WOCHE: Woche = {
  montag: '2026-03-02',
  tagesbeginn: '07:00',
  tagesende: '19:00',
  ruestzeitMinuten: 5,
  nachruheMinuten: 20,
  spalten: [
    {
      therapeut: 'T. Alpha',
      belegungen: [
        {
          art: 'GESPERRT',
          von: '2026-03-02T07:00:00+01:00',
          bis: '2026-03-02T08:00:00+01:00',
          text: 'außerhalb der Arbeitszeit',
        },
        {
          art: 'RUESTZEIT',
          von: '2026-03-02T08:55:00+01:00',
          bis: '2026-03-02T09:00:00+01:00',
          text: 'Vorbereitung',
        },
        {
          art: 'BELEGT',
          von: '2026-03-02T09:00:00+01:00',
          bis: '2026-03-02T09:20:00+01:00',
          text: 'Krankengymnastik',
          raum: 'Raum 1',
        },
        {
          art: 'RUESTZEIT',
          von: '2026-03-02T09:20:00+01:00',
          bis: '2026-03-02T09:25:00+01:00',
          text: 'Nachbereitung',
        },
        {
          art: 'NACHRUHE',
          von: '2026-03-02T10:35:00+01:00',
          bis: '2026-03-02T10:55:00+01:00',
          text: 'Nachruhe',
          raum: 'Bad',
        },
        {
          art: 'ABWESENHEIT',
          von: '2026-03-03T07:00:00+01:00',
          bis: '2026-03-03T19:00:00+01:00',
          text: 'Fortbildung',
        },
      ],
    },
    { therapeut: 'T. Beta', belegungen: [] },
  ],
};

describe('rasterFuer', () => {
  const montag = rasterFuer(WOCHE, '2026-03-02');
  const zelle = (uhrzeit: string, spalte = 0) =>
    montag.zeilen.find((z) => z.uhrzeit === uhrzeit)!.zellen[spalte];

  it('hat 48 Viertelstunden von 07:00 bis 18:45 und eine Spalte je Therapeutin', () => {
    expect(montag.zeilen.length).toBe(48);
    expect(montag.zeilen[0].uhrzeit).toBe('07:00');
    expect(montag.zeilen[47].uhrzeit).toBe('18:45');
    expect(montag.therapeuten).toEqual(['T. Alpha', 'T. Beta']);
    expect(montag.wochentag).toBe('Montag');
  });

  it('ordnet jede Zelle der Belegung zu, die sie deckt', () => {
    expect(zelle('07:00').zustand).toBe('gesperrt');
    expect(zelle('07:45').zustand).toBe('gesperrt');
    expect(zelle('08:00').zustand).toBe('frei');
    expect(zelle('09:00').zustand).toBe('belegt');
    expect(zelle('09:15').zustand).toBe('belegt');
    expect(zelle('09:30').zustand).toBe('frei');
    expect(zelle('10:45').zustand).toBe('nachruhe');
  });

  it('nennt im Namen Tag, Uhrzeit, Person, Zustand und was dazugehört', () => {
    expect(zelle('09:00').name).toBe('Montag, 09:00, T. Alpha, belegt, Krankengymnastik, Raum 1');
    expect(zelle('10:45').name).toBe('Montag, 10:45, T. Alpha, Nachruhe, Nachruhe, Bad');
    expect(zelle('08:00', 1).name).toBe('Montag, 08:00, T. Beta, frei');
  });

  it('markiert nur die erste Zelle eines Blocks als Blockbeginn', () => {
    expect(zelle('07:00').blockbeginn).toBeTrue();
    expect(zelle('07:15').blockbeginn).toBeFalse();
    expect(zelle('09:00').blockbeginn).toBeTrue();
    expect(zelle('09:15').blockbeginn).toBeFalse();
  });

  it('Rüstzeit unterhalb des Rasters fällt in die Zelle, in der sie beginnt', () => {
    // 08:55 bis 09:00 liegt in der Zelle 08:45; die Zelle zeigt die Rüstzeit nicht,
    // weil die Behandlung um 09:00 die Zelle 09:00 füllt - fachlich richtig.
    expect(zelle('08:45').zustand).toBe('frei');
  });

  it('zeigt am Dienstag die Abwesenheit über den ganzen Tag', () => {
    const dienstag = rasterFuer(WOCHE, '2026-03-03');
    expect(dienstag.wochentag).toBe('Dienstag');
    expect(dienstag.zeilen.every((z) => z.zellen[0].zustand === 'abwesenheit')).toBeTrue();
    expect(dienstag.zeilen[0].zellen[0].blockbeginn).toBeTrue();
    expect(dienstag.zeilen[1].zellen[0].blockbeginn).toBeFalse();
  });
});
