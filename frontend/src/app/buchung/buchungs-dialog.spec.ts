import { Dialog, DialogRef } from '@angular/cdk/dialog';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { Buchungsantwort } from '../api/aptum-api';
import { lokalDeutsch } from '../lokal';
import { BuchungsDialog, BuchungsErgebnis, BuchungsVorhaben } from './buchungs-dialog';

const VORHABEN: BuchungsVorhaben = {
  verordnung: '11111111-2222-4333-8444-555555555555',
  heilmittel: 'KG_EINZEL',
  heilmittelBezeichnung: 'Krankengymnastik',
  therapeut: 'T. Alpha',
  raum: 'Raum 1',
  beginn: '2026-03-02T09:00:00+01:00',
  ende: '2026-03-02T09:20:00+01:00',
};

const FREI: Buchungsantwort = {
  ausgang: 'FREI',
  regeln: [
    { regel: 'Behandlungsbeginn', ausgang: 'ERFUELLT', begruendung: 'innerhalb von 28 Tagen' },
    { regel: 'Frequenz', ausgang: 'WARNUNG', begruendung: 'obere Grenze erreicht' },
  ],
};

const BLOCKIERT: Buchungsantwort = {
  ausgang: 'BLOCKIERT',
  regeln: [
    { regel: 'Therapeut verfügbar', ausgang: 'VERLETZT', begruendung: 'bereits belegt' },
    { regel: 'Frequenz', ausgang: 'ERFUELLT', begruendung: 'im Rahmen' },
  ],
};

describe('BuchungsDialog', () => {
  let http: HttpTestingController;
  let ref: DialogRef<BuchungsErgebnis, BuchungsDialog>;
  let geschlossenMit: BuchungsErgebnis | 'offen';

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting(), ...lokalDeutsch],
    });
    http = TestBed.inject(HttpTestingController);
    geschlossenMit = 'offen';
    ref = TestBed.inject(Dialog).open<BuchungsErgebnis, BuchungsVorhaben, BuchungsDialog>(
      BuchungsDialog,
      { data: VORHABEN, ariaLabelledBy: 'buchung-titel', ariaModal: true },
    );
    ref.closed.subscribe((e) => (geschlossenMit = e));
    TestBed.tick();
  });

  afterEach(() => {
    if (geschlossenMit === 'offen') ref.close();
    http.verify();
  });

  const dialog = (): HTMLElement => document.querySelector('[role="dialog"]')!;
  const pruefungLiefern = (antwort: Buchungsantwort) => {
    http.expectOne('/termine/pruefung').flush(antwort);
    TestBed.tick();
  };

  it('öffnet modal, prüft sofort und zeigt jede Regel mit Ausgang und Begründung', () => {
    expect(dialog().getAttribute('aria-modal')).toBe('true');
    expect(dialog().getAttribute('aria-labelledby')).toBe('buchung-titel');
    pruefungLiefern(FREI);

    const regeln = Array.from(dialog().querySelectorAll('.regel'));
    expect(regeln.length).toBe(2);
    expect(regeln[0].classList).toContain('erfuellt');
    expect(regeln[0].textContent).toContain('Behandlungsbeginn');
    expect(regeln[0].textContent).toContain('innerhalb von 28 Tagen');
    expect(regeln[1].classList).toContain('warnung');
    expect(dialog().querySelector('[aria-live="polite"]')?.textContent).toContain(
      '1 erfüllt, 1 Warnungen, 0 verletzt',
    );
  });

  it('bucht bei FREI und schließt mit der Antwort', () => {
    pruefungLiefern(FREI);
    const buchen = dialog().querySelector<HTMLButtonElement>('button.primaer')!;
    expect(buchen.textContent?.trim()).toBe('Buchen');
    expect(buchen.disabled).toBeFalse();
    buchen.click();
    TestBed.tick();

    const anfrage = http.expectOne('/termine');
    expect(anfrage.request.body.uebersteuerung).toBeUndefined();
    anfrage.flush({ ...FREI, termin: 'abc' }, { status: 201, statusText: 'Created' });
    TestBed.tick();
    expect(geschlossenMit).toEqual(jasmine.objectContaining({ termin: 'abc' }));
  });

  it('bei BLOCKIERT ist Buchen gesperrt, bis eine Begründung steht - dann übersteuert', () => {
    pruefungLiefern(BLOCKIERT);
    const trotzdem = dialog().querySelector<HTMLButtonElement>('button.primaer')!;
    expect(trotzdem.textContent?.trim()).toBe('Trotzdem buchen');
    expect(trotzdem.disabled).toBeTrue();

    const feld = dialog().querySelector<HTMLTextAreaElement>('#begruendung')!;
    feld.value = 'Absprache mit der Praxisleitung';
    feld.dispatchEvent(new Event('input'));
    TestBed.tick();
    expect(trotzdem.disabled).toBeFalse();

    trotzdem.click();
    TestBed.tick();
    const anfrage = http.expectOne('/termine');
    expect(anfrage.request.body.uebersteuerung.begruendung).toBe('Absprache mit der Praxisleitung');
    anfrage.flush(
      { ausgang: 'UEBERSTEUERT', regeln: BLOCKIERT.regeln, termin: 'xyz' },
      { status: 201, statusText: 'Created' },
    );
    TestBed.tick();
    expect(geschlossenMit).toEqual(jasmine.objectContaining({ ausgang: 'UEBERSTEUERT' }));
  });

  it('eine 409 beim Buchen ist eine Antwort mit Regeln, kein Fehler', () => {
    pruefungLiefern(FREI);
    dialog().querySelector<HTMLButtonElement>('button.primaer')!.click();
    TestBed.tick();
    http.expectOne('/termine').flush(BLOCKIERT, { status: 409, statusText: 'Conflict' });
    TestBed.tick();
    expect(dialog().querySelector('#begruendung')).not.toBeNull();
    expect(dialog().querySelector('[role="alert"]')).toBeNull();
    expect(geschlossenMit).toBe('offen');
  });

  it('Abbrechen schließt ohne Ergebnis', () => {
    pruefungLiefern(FREI);
    dialog().querySelector<HTMLButtonElement>('button.sekundaer')!.click();
    TestBed.tick();
    expect(geschlossenMit).toBeUndefined();
  });

  it('hat keine axe-Verstöße, frei und blockiert', async () => {
    pruefungLiefern(BLOCKIERT);
    const ergebnis = await axe.run(dialog());
    expect(ergebnis.violations).toEqual([]);
  });
});
