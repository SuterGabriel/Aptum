import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed, fakeAsync, tick } from '@angular/core/testing';
import axe from 'axe-core';
import { lokalDeutsch } from '../lokal';
import { TerminSuche } from './termin-suche';

const VERORDNUNG = '11111111-2222-4333-8444-555555555555';

const EIN_VORSCHLAG = {
  zusammenfassung: '1 Vorschlag',
  geprueft: 12,
  vorschlaege: [
    {
      beginn: '2026-03-02T09:00:00+01:00',
      ende: '2026-03-02T09:30:00+01:00',
      therapeut: 'T. Alpha',
      raum: 'Raum 1',
      warnungen: [{ regel: 'Frequenz', begruendung: 'knapp' }],
    },
  ],
  ausgeschlossen: { Verordnungsgültigkeit: 2 },
};

describe('TerminSuche', () => {
  let fixture: ComponentFixture<TerminSuche>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [TerminSuche],
      providers: [provideHttpClient(), provideHttpClientTesting(), ...lokalDeutsch],
    }).compileComponents();
    fixture = TestBed.createComponent(TerminSuche);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const html = (): HTMLElement => fixture.nativeElement;

  function kennungEintragen(): void {
    fixture.componentInstance.form.controls.verordnung.setValue(VERORDNUNG);
    tick(300);
  }

  it('gliedert die Kriterien sichtbar nach den vier Dimensionen', () => {
    const legenden = Array.from(html().querySelectorAll('form > fieldset > legend')).map((l) =>
      l.textContent?.replace(/\s+/g, ' ').trim(),
    );
    expect(legenden).toEqual(['1 Verordnung', '2 Therapeut', '3 Raum', '4 Wunschfenster']);
  });

  it('sucht nicht, solange die Kennung ungültig ist', fakeAsync(() => {
    fixture.componentInstance.form.controls.verordnung.setValue('keine-uuid');
    tick(300);
    http.expectNone('/termine/suche');
  }));

  it('zeigt Vorschläge und die ausgeschlossenen mit Begründung', fakeAsync(() => {
    kennungEintragen();
    http.expectOne('/termine/suche').flush(EIN_VORSCHLAG);
    fixture.detectChanges();

    const text = html().querySelector('.ergebnis')?.textContent ?? '';
    expect(text).toContain('Montag, 2. März');
    expect(text).toContain('09:00 bis 09:30');
    expect(text).toContain('30 min');
    expect(text).toContain('Frequenz: knapp');
    expect(text).toContain('2 wegen Verordnungsgültigkeit');
    expect(html().querySelector('[aria-live="polite"]')?.textContent).toContain(
      '1 Vorschläge gefunden',
    );
  }));

  it('meldet einen Fehler als Alert, nicht still', fakeAsync(() => {
    kennungEintragen();
    http
      .expectOne('/termine/suche')
      .flush({ fehler: 'Unbekannte Verordnung' }, { status: 400, statusText: 'Bad Request' });
    fixture.detectChanges();
    expect(html().querySelector('[role="alert"]')?.textContent).toContain('Unbekannte Verordnung');
  }));

  // axe läuft mit echten Timern; fakeAsync würde seine Promises nie auflösen.
  // Deshalb hier warten statt tick.
  it('hat keine axe-Verstöße, leer und mit Ergebnis', async () => {
    const leer = await axe.run(html());
    expect(leer.violations).toEqual([]);

    fixture.componentInstance.form.controls.verordnung.setValue(VERORDNUNG);
    await new Promise((weiter) => setTimeout(weiter, 350));
    http.expectOne('/termine/suche').flush(EIN_VORSCHLAG);
    fixture.detectChanges();
    const voll = await axe.run(html());
    expect(voll.violations).toEqual([]);
  });
});
