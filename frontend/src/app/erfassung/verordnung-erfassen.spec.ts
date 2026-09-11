import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import axe from 'axe-core';
import { Erfassung } from '../api/aptum-api';
import { lokalDeutsch } from '../lokal';
import { VerordnungErfassen } from './verordnung-erfassen';

const TEXT = 'KG 6x, 2x wöchentlich, WS, ausgestellt 27.02.2026';

const VOLLSTAENDIG: Erfassung = {
  provider: 'anthropic:claude-sonnet-5',
  pseudonymisiert: 0,
  vorschlag: {
    heilmittel: 'KG_EINZEL',
    diagnosegruppe: 'WS',
    verordnete_einheiten: 6,
    frequenz: { min_pro_woche: 2, max_pro_woche: 2 },
    ausstellungsdatum: '2026-02-27',
    dringlicher_bedarf: false,
    hausbesuch: false,
    nicht_extrahierbar: [],
    hinweise: [],
  },
};

const LUECKENHAFT: Erfassung = {
  provider: 'anthropic:claude-sonnet-5',
  pseudonymisiert: 2,
  vorschlag: {
    heilmittel: 'KG_EINZEL',
    verordnete_einheiten: 6,
    ausstellungsdatum: '2026-02-27',
    nicht_extrahierbar: ['diagnosegruppe', 'frequenz', 'dringlicher_bedarf', 'hausbesuch'],
    hinweise: ['Diagnosegruppe nicht lesbar'],
  },
};

describe('VerordnungErfassen', () => {
  let fixture: ComponentFixture<VerordnungErfassen>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [VerordnungErfassen],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        ...lokalDeutsch,
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(VerordnungErfassen);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const html = (): HTMLElement => fixture.nativeElement;

  function vorschlagHolen(antwort: Erfassung): void {
    fixture.componentInstance.text.set(TEXT);
    fixture.componentInstance.vorschlagHolen();
    const anfrage = http.expectOne('/api/erfassung');
    expect(anfrage.request.body).toEqual({ text: TEXT });
    anfrage.flush(antwort);
    fixture.detectChanges();
  }

  function uebernehmen(antwort: Erfassung): void {
    fixture.componentInstance.uebernehmen(antwort);
    fixture.detectChanges();
  }

  it('schickt nichts los, solange kein Text da ist', () => {
    fixture.componentInstance.vorschlagHolen();
    http.expectNone('/api/erfassung');
    const knopf = html().querySelector<HTMLButtonElement>('.eingabe button');
    expect(knopf?.disabled).toBeTrue();
  });

  it('zeigt, von welchem Modell der Vorschlag kommt und wie viel ersetzt wurde', () => {
    vorschlagHolen(LUECKENHAFT);
    const text = html().querySelector('.herkunft')?.textContent ?? '';
    expect(text).toContain('anthropic:claude-sonnet-5');
    expect(text).toContain('2');
    expect(text).toContain('ersetzt');
  });

  it('markiert die nicht gelesenen Felder und lässt sie leer', () => {
    vorschlagHolen(LUECKENHAFT);
    uebernehmen(LUECKENHAFT);

    const w = fixture.componentInstance.form.getRawValue();
    expect(w.verordneteEinheiten).toBe(6, 'gelesenes Feld wird übernommen');
    expect(w.ausstellungsdatum).toBe('2026-02-27');
    expect(w.diagnosegruppe).toBe('', 'nicht gelesenes Feld bleibt leer');
    expect(w.frequenzMin).toBe(0);

    const markiert = Array.from(html().querySelectorAll('.feld.unsicher')).map((e) =>
      e.querySelector('label, .gruppenlabel')?.textContent?.trim(),
    );
    expect(markiert).toContain('Diagnosegruppe');
    expect(markiert).toContain('Frequenz je Woche');
    // Nie Farbe allein: jede Markierung trägt ein Wort.
    expect(html().querySelectorAll('.feld.unsicher .marke').length).toBe(markiert.length);
  });

  it('zählt nur die Felder, die das Formular auch hat', () => {
    // Vier Felder konnte das Modell nicht lesen, aber nur zwei davon gehören
    // zur Verordnung. Hausbesuch und dringlicher Bedarf zu zählen hieße, zum
    // Ergänzen aufzufordern, wofür es kein Pflichtfeld gibt.
    vorschlagHolen(LUECKENHAFT);
    expect(fixture.componentInstance.unsicher().length).toBe(4);
    const status = html().querySelector('[aria-live="polite"]')?.textContent ?? '';
    expect(status).toContain('2 Felder');
    expect(status).toContain('ergänzen');
  });

  it('legt erst an, wenn das Formular vollständig ist - der Mensch bestätigt', () => {
    vorschlagHolen(LUECKENHAFT);
    uebernehmen(LUECKENHAFT);
    expect(fixture.componentInstance.form.invalid).toBeTrue();

    fixture.componentInstance.anlegen();
    http.expectNone('/api/verordnungen');

    fixture.componentInstance.form.patchValue({
      diagnosegruppe: 'WS',
      frequenzMin: 1,
      frequenzMax: 3,
    });
    fixture.detectChanges();
    fixture.componentInstance.anlegen();

    const anfrage = http.expectOne('/api/verordnungen');
    expect(anfrage.request.body).toEqual({
      ausstellungsdatum: '2026-02-27',
      diagnosegruppe: 'WS',
      verordneteEinheiten: 6,
      frequenzMin: 1,
      frequenzMax: 3,
      dringlicherBedarf: false,
    });
    anfrage.flush({ id: 'abc-123' }, { status: 201, statusText: 'Created' });
    fixture.detectChanges();
    expect(html().querySelector('.angelegt')?.textContent).toContain('abc-123');
  });

  it('zeigt die Ablehnung der Domäne als Alert, nicht als Erfolg', () => {
    vorschlagHolen(VOLLSTAENDIG);
    uebernehmen(VOLLSTAENDIG);
    fixture.componentInstance.anlegen();
    http
      .expectOne('/api/verordnungen')
      .flush(
        { fehler: 'Verordnete Einheiten über der Höchstmenge der Diagnosegruppe' },
        { status: 400, statusText: 'Bad Request' },
      );
    fixture.detectChanges();
    expect(html().querySelector('[role="alert"]')?.textContent).toContain('Höchstmenge');
    expect(html().querySelector('.angelegt')).toBeNull();
  });

  it('hat keine axe-Verstöße, leer und mit Vorschlag', async () => {
    const leer = await axe.run(html());
    expect(leer.violations).toEqual([]);

    vorschlagHolen(LUECKENHAFT);
    uebernehmen(LUECKENHAFT);
    const voll = await axe.run(html());
    expect(voll.violations).toEqual([]);
  });
});
