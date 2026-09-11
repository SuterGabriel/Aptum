import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { provideRouter } from '@angular/router';
import axe from 'axe-core';
import { lokalDeutsch } from '../lokal';
import { KalenderSeite } from './kalender-seite';

describe('KalenderSeite', () => {
  let fixture: ComponentFixture<KalenderSeite>;
  let http: HttpTestingController;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [KalenderSeite],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        ...lokalDeutsch,
      ],
    }).compileComponents();
    fixture = TestBed.createComponent(KalenderSeite);
    http = TestBed.inject(HttpTestingController);
    fixture.componentInstance.tag.set('2026-03-04');
    fixture.detectChanges();
  });

  afterEach(() => http.verify());

  const html = (): HTMLElement => fixture.nativeElement;

  function wocheLiefern(): void {
    http
      .expectOne((r) => r.url === '/api/kalender/woche' && r.params.get('tag') === '2026-03-02')
      .flush({
        montag: '2026-03-02',
        tagesbeginn: '08:00',
        tagesende: '09:00',
        spalten: [{ therapeut: 'T. Alpha', belegungen: [] }],
      });
    fixture.detectChanges();
  }

  it('lädt die Woche des Montags, zeigt den gewählten Tag und die Legende', () => {
    wocheLiefern();
    expect(html().querySelector('[role="grid"]')?.getAttribute('aria-label')).toBe(
      'Mittwoch, 2026-03-04',
    );
    expect(html().querySelector('[role="tab"][aria-selected="true"]')?.textContent).toContain('Mi');
    expect(html().querySelectorAll('.legende li').length).toBe(6);
  });

  it('blättert eine Woche weiter und lädt neu', () => {
    wocheLiefern();
    html().querySelector<HTMLButtonElement>('[aria-label="Nächste Woche"]')!.click();
    fixture.detectChanges();
    http
      .expectOne((r) => r.params.get('tag') === '2026-03-09')
      .flush({ montag: '2026-03-09', spalten: [] });
    fixture.detectChanges();
    expect(html().querySelector('[role="tab"][aria-selected="true"]')?.textContent).toContain(
      '11.3.',
    );
  });

  it('Reiter: Pfeil rechts wechselt den Tag und trägt den Fokus weiter', () => {
    wocheLiefern();
    const aktiv = () => html().querySelector<HTMLElement>('[role="tab"][tabindex="0"]')!;
    aktiv().dispatchEvent(new KeyboardEvent('keydown', { key: 'ArrowRight', bubbles: true }));
    fixture.detectChanges();
    expect(aktiv().textContent).toContain('Do');
    expect(html().querySelectorAll('[role="tab"][tabindex="0"]').length).toBe(1);
    aktiv().dispatchEvent(new KeyboardEvent('keydown', { key: 'End', bubbles: true }));
    fixture.detectChanges();
    expect(aktiv().textContent).toContain('Sa');
  });

  it('meldet einen gewählten Slot in der Statuszeile', () => {
    wocheLiefern();
    html().querySelector<HTMLElement>('[role="gridcell"]')!.click();
    fixture.detectChanges();
    expect(html().querySelector('.status')?.textContent).toContain(
      'Gewählt: Mittwoch, 08:00, T. Alpha, frei',
    );
  });

  it('hat keine axe-Verstöße', async () => {
    wocheLiefern();
    const ergebnis = await axe.run(html());
    expect(ergebnis.violations).toEqual([]);
  });
});
