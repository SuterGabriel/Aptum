import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { Abrechnungsuebersicht } from '../api/aptum-api';
import { lokalDeutsch } from '../lokal';
import { AbrechnungsUebersicht } from './abrechnungs-uebersicht';

const ANTWORT: Abrechnungsuebersicht = {
  posten: [
    {
      verordnung: '11111111-0000-4000-8000-000000000001',
      ausstellungsdatum: '2026-02-27',
      diagnosegruppe: 'WS',
      therapieform: 'PHYSIOTHERAPIE',
      verordnet: 6,
      erbracht: 3,
      offen: 3,
      ersteBehandlung: '2026-03-02',
      letzteBehandlung: '2026-03-09',
      status: 'PRUEFFEST',
      begruendung: '5 Regeln geprüft, keine verletzt.',
      regeln: [],
    },
    {
      verordnung: '22222222-0000-4000-8000-000000000002',
      ausstellungsdatum: '2026-01-05',
      diagnosegruppe: 'ZN',
      therapieform: 'PHYSIOTHERAPIE',
      verordnet: 10,
      erbracht: 1,
      offen: 9,
      ersteBehandlung: '2026-02-20',
      letzteBehandlung: '2026-02-20',
      status: 'BEANSTANDET',
      begruendung:
        'Verordnungsfrist: Behandlungsbeginn 46 Tage nach Ausstellung, zulässig sind 28. Die Verordnung hat ihre Gültigkeit verloren.',
      regeln: [],
    },
    {
      verordnung: '33333333-0000-4000-8000-000000000003',
      ausstellungsdatum: '2026-03-10',
      diagnosegruppe: 'SB1',
      therapieform: 'ERGOTHERAPIE',
      verordnet: 10,
      erbracht: 0,
      offen: 10,
      status: 'NICHT_BEGONNEN',
      begruendung: 'Noch keine Behandlung erbracht, nichts abzurechnen.',
      regeln: [],
    },
  ],
  erbracht: 4,
  prueffest: 1,
  beanstandet: 1,
};

describe('AbrechnungsUebersicht', () => {
  let fixture: ComponentFixture<AbrechnungsUebersicht>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [AbrechnungsUebersicht],
      providers: [provideHttpClient(), provideHttpClientTesting(), ...lokalDeutsch],
    }).compileComponents();
    fixture = TestBed.createComponent(AbrechnungsUebersicht);
    http = TestBed.inject(HttpTestingController);
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const html = (): HTMLElement => fixture.nativeElement;

  /** ag-grid zeichnet die Zeilen nach dem Rendern, nicht darin - deshalb warten. */
  async function geliefert(antwort: Abrechnungsuebersicht = ANTWORT): Promise<void> {
    http.expectOne('/api/abrechnung/uebersicht').flush(antwort);
    fixture.detectChanges();
    await fixture.whenStable();
    await new Promise((r) => setTimeout(r, 50));
    fixture.detectChanges();
  }

  const zeilen = (): HTMLElement[] =>
    Array.from(html().querySelectorAll<HTMLElement>('[role="row"][row-index]'));

  it('zeigt eine Zeile je Verordnung, den Status als Wort und die Summe aus dem Backend', async () => {
    await geliefert();
    expect(html().querySelector('[role="grid"]')).withContext('ag-grid als Grid').toBeTruthy();
    expect(zeilen().length).toBe(3);
    const statusZellen = Array.from(
      html().querySelectorAll<HTMLElement>('.ag-cell[col-id="status"]'),
    ).map((z) => z.textContent?.trim());
    expect(statusZellen).toContain('prüffest');
    expect(statusZellen).toContain('beanstandet');
    expect(statusZellen).toContain('nicht begonnen');
    expect(html().querySelector('.ag-cell.status-beanstandet')?.textContent).toContain(
      'beanstandet',
    );
    expect(html().querySelector('.summe')?.textContent).toContain(
      '3 Verordnungen, 4 Einheiten erbracht, 1 prüffest, 1 beanstandet.',
    );
  });

  it('formatiert Daten deutsch und lässt leere Daten als Strich stehen', async () => {
    await geliefert();
    const ausgestellt = Array.from(
      html().querySelectorAll<HTMLElement>('.ag-cell[col-id="ausstellungsdatum"]'),
    ).map((z) => z.textContent?.trim());
    expect(ausgestellt).toContain('27.02.2026');
    const erste = Array.from(
      html().querySelectorAll<HTMLElement>('.ag-cell[col-id="ersteBehandlung"]'),
    ).map((z) => z.textContent?.trim());
    expect(erste).toContain('–');
  });

  it('der Schnellfilter lässt nur passende Zeilen stehen', async () => {
    await geliefert();
    const feld = html().querySelector<HTMLInputElement>('#abrechnung-filter')!;
    feld.value = 'beanstandet';
    feld.dispatchEvent(new Event('input'));
    fixture.detectChanges();
    await new Promise((r) => setTimeout(r, 50));
    fixture.detectChanges();
    expect(zeilen().length).toBe(1);
    expect(zeilen()[0].textContent).toContain('Verordnungsfrist');
  });

  it('meldet einen Fehler als Alert statt einer leeren Tabelle', async () => {
    http
      .expectOne('/api/abrechnung/uebersicht')
      .flush('kaputt', { status: 500, statusText: 'Server Error' });
    fixture.detectChanges();
    expect(html().querySelector('[role="alert"]')?.textContent).toContain('Status 500');
    expect(html().querySelector('[role="grid"]')).toBeNull();
  });

  it('axe findet nichts - auch nicht in den Zellen, die ag-grid zeichnet', async () => {
    await geliefert();
    const ergebnis = await axe.run(html(), {
      rules: { region: { enabled: false } },
    });
    expect(ergebnis.violations)
      .withContext(JSON.stringify(ergebnis.violations, null, 2))
      .toEqual([]);
  });
});
