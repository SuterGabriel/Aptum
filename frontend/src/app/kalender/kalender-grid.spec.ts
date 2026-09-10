import { Component, signal } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import axe from 'axe-core';
import { Woche } from '../api/aptum-api';
import { KalenderGrid, SlotWahl } from './kalender-grid';
import { rasterFuer } from './raster';

const WOCHE: Woche = {
  montag: '2026-03-02',
  tagesbeginn: '08:00',
  tagesende: '10:00',
  spalten: [
    {
      therapeut: 'T. Alpha',
      belegungen: [
        {
          art: 'BELEGT',
          von: '2026-03-02T09:00:00+01:00',
          bis: '2026-03-02T09:30:00+01:00',
          text: 'Krankengymnastik',
          raum: 'Raum 1',
        },
      ],
    },
    { therapeut: 'T. Beta', belegungen: [] },
  ],
};

@Component({
  imports: [KalenderGrid],
  template: `<app-kalender-grid [raster]="raster()" (slotGewaehlt)="gewaehlt.push($event)" />`,
})
class Gastgeber {
  readonly raster = signal(rasterFuer(WOCHE, '2026-03-02'));
  readonly gewaehlt: SlotWahl[] = [];
}

describe('KalenderGrid', () => {
  let fixture: ComponentFixture<Gastgeber>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({ imports: [Gastgeber] }).compileComponents();
    fixture = TestBed.createComponent(Gastgeber);
    fixture.detectChanges();
  });

  const html = (): HTMLElement => fixture.nativeElement;
  const zellen = () => Array.from(html().querySelectorAll<HTMLElement>('[role="gridcell"]'));
  const tabbar = () => html().querySelector<HTMLElement>('[role="gridcell"][tabindex="0"]')!;
  const taste = (key: string, ctrlKey = false) => {
    // Wie im Browser: Die Taste trifft die Zelle, die den Fokus trägt.
    tabbar().dispatchEvent(new KeyboardEvent('keydown', { key, ctrlKey, bubbles: true }));
    fixture.detectChanges();
  };

  it('ist ein Grid mit Zeilen, Spaltenköpfen und Zellen', () => {
    expect(html().querySelector('[role="grid"]')).not.toBeNull();
    expect(html().querySelectorAll('[role="columnheader"]').length).toBe(3);
    expect(html().querySelectorAll('[role="row"]').length).toBe(9);
    expect(zellen().length).toBe(16);
  });

  it('Roving Tabindex: genau eine Zelle ist tabbar', () => {
    expect(zellen().filter((z) => z.tabIndex === 0).length).toBe(1);
    expect(tabbar().getAttribute('aria-label')).toBe('Montag, 08:00, T. Alpha, frei');
  });

  it('Pfeile bewegen den Fokus, Home und End springen, Strg an den Rand', () => {
    taste('ArrowDown');
    expect(tabbar().getAttribute('aria-label')).toContain('08:15, T. Alpha');
    taste('ArrowRight');
    expect(tabbar().getAttribute('aria-label')).toContain('08:15, T. Beta');
    taste('ArrowRight');
    expect(tabbar().getAttribute('aria-label')).toContain('T. Beta');
    taste('Home');
    expect(tabbar().getAttribute('aria-label')).toContain('08:15, T. Alpha');
    taste('End');
    expect(tabbar().getAttribute('aria-label')).toContain('08:15, T. Beta');
    taste('End', true);
    expect(tabbar().getAttribute('aria-label')).toContain('09:45, T. Beta');
    taste('Home', true);
    expect(tabbar().getAttribute('aria-label')).toContain('08:00, T. Alpha');
    expect(zellen().filter((z) => z.tabIndex === 0).length).toBe(1);
  });

  it('Eingabe auf einer freien Zelle wählt sie, auf einer belegten nicht', () => {
    taste('Enter');
    expect(fixture.componentInstance.gewaehlt.length).toBe(1);
    expect(fixture.componentInstance.gewaehlt[0].beginn).toBe('2026-03-02T08:00');

    for (let i = 0; i < 4; i++) taste('ArrowDown'); // 09:00, belegt
    taste(' ');
    expect(fixture.componentInstance.gewaehlt.length).toBe(1);
  });

  it('Fokus ist keine Auswahl: kein aria-selected auf den Zellen', () => {
    expect(html().querySelector('[role="gridcell"][aria-selected]')).toBeNull();
  });

  it('Zellen tragen Zustand und Text, nicht nur Farbe', () => {
    const belegt = zellen().find((z) => z.getAttribute('aria-label')?.includes('09:00, T. Alpha'))!;
    expect(belegt.classList).toContain('belegt');
    expect(belegt.textContent).toContain('Krankengymnastik');
    expect(belegt.textContent).toContain('Raum 1');
  });

  it('hat keine axe-Verstöße', async () => {
    const ergebnis = await axe.run(html());
    expect(ergebnis.violations).toEqual([]);
  });
});
