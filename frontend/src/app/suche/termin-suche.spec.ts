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
      // Bewusst nicht die Zone des Rechners: Die Anzeige muss die Zone des
      // Wertes nehmen, sonst zeigt die CI unter UTC eine andere Uhrzeit.
      beginn: '2026-03-02T09:00:00+05:00',
      ende: '2026-03-02T09:30:00+05:00',
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

  const AKTE = {
    posten: {
      verordnung: VERORDNUNG,
      ausstellungsdatum: '2026-02-27',
      diagnosegruppe: 'WS',
      therapieform: 'PHYSIOTHERAPIE',
      verordnet: 6,
      erbracht: 2,
      offen: 4,
      status: 'PRUEFFEST',
      begruendung: '5 Regeln geprüft, keine verletzt.',
      regeln: [],
    },
    dringlicherBedarf: true,
    frequenzMin: 1,
    frequenzMax: 3,
    frequenz: '1-3x wöchentlich',
  };

  /** Die Kennung löst zwei Anfragen aus: die Akte dahinter und die Suche. */
  function kennungEintragen(): void {
    fixture.componentInstance.form.controls.verordnung.setValue(VERORDNUNG);
    tick(300);
    http.expectOne(`/api/verordnungen/${VERORDNUNG}`).flush(AKTE);
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
    http.expectNone('/api/termine/suche');
    expect().nothing();
  }));

  it('zeigt Vorschläge und die ausgeschlossenen mit Begründung', fakeAsync(() => {
    kennungEintragen();
    http.expectOne('/api/termine/suche').flush(EIN_VORSCHLAG);
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

  it('zeigt die Verordnung hinter der Kennung: Einheiten, Frequenz, Stand, dringlich', fakeAsync(() => {
    kennungEintragen();
    http.expectOne('/api/termine/suche').flush(EIN_VORSCHLAG);
    fixture.detectChanges();
    const akte = html().querySelector('.akte')?.textContent?.replace(/\s+/g, ' ') ?? '';
    expect(akte).toContain('27.02.2026');
    expect(akte).toContain('dringlich');
    expect(akte).toContain('2 von 6 erbracht, 4 offen');
    expect(akte).toContain('1-3x wöchentlich');
    expect(akte).toContain('prüffest');
  }));

  it('bleibt still, wenn die Akte nicht zu laden ist - die Suche meldet', fakeAsync(() => {
    fixture.componentInstance.form.controls.verordnung.setValue(VERORDNUNG);
    tick(300);
    http
      .expectOne(`/api/verordnungen/${VERORDNUNG}`)
      .flush({ fehler: 'unbekannt' }, { status: 404, statusText: 'Not Found' });
    http.expectOne('/api/termine/suche').flush(EIN_VORSCHLAG);
    fixture.detectChanges();
    expect(html().querySelector('.akte')).toBeNull();
  }));

  it('meldet einen Fehler als Alert, nicht still', fakeAsync(() => {
    kennungEintragen();
    http
      .expectOne('/api/termine/suche')
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
    http.expectOne(`/api/verordnungen/${VERORDNUNG}`).flush(AKTE);
    http.expectOne('/api/termine/suche').flush(EIN_VORSCHLAG);
    fixture.detectChanges();
    const voll = await axe.run(html());
    expect(voll.violations).toEqual([]);
  });
});
