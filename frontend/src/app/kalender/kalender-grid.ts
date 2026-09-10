import { LiveAnnouncer } from '@angular/cdk/a11y';
import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterRenderEffect,
  computed,
  inject,
  input,
  output,
  signal,
} from '@angular/core';
import { Raster, Zelle } from './raster';

/** Ein Slot, den jemand mit Eingabe, Leertaste oder Klick gewählt hat. */
export interface SlotWahl {
  readonly therapeut: string;
  readonly beginn: string;
  readonly name: string;
}

/**
 * Das Zeitgitter eines Tages: Therapeut:innen als Spalten, Viertelstunden
 * als Zeilen.
 *
 * Ein Grid-Widget, keine Tabelle mit Klick-Handlern (Skill a11y-grid):
 * genau eine Zelle ist tabbar, die Pfeile bewegen sich, jede Zelle hat
 * einen Namen, der Tag, Uhrzeit und Zustand nennt. Die Zustände kommen aus
 * dem Raster; das Grid selbst rechnet nichts.
 */
@Component({
  selector: 'app-kalender-grid',
  templateUrl: './kalender-grid.html',
  styleUrl: './kalender-grid.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KalenderGrid {
  readonly raster = input.required<Raster>();
  readonly slotGewaehlt = output<SlotWahl>();

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);
  private readonly ansage = inject(LiveAnnouncer);

  /** Roving Tabindex: die eine Zelle, die den Fokus tragen darf. */
  readonly fokus = signal({ zeile: 0, spalte: 0 });
  private fokusAnfordern = false;

  readonly zeilenAnzahl = computed(() => this.raster().zeilen.length);
  readonly spaltenAnzahl = computed(() => this.raster().therapeuten.length);

  constructor() {
    // Erst nach dem Rendern trägt die neue Zelle tabindex 0; dann fokussieren.
    // Nur nach einer Tastatur- oder Mausaktion, nie beim ersten Zeichnen.
    afterRenderEffect(() => {
      this.fokus();
      if (!this.fokusAnfordern) return;
      this.fokusAnfordern = false;
      this.host.nativeElement
        .querySelector<HTMLElement>('[role="gridcell"][tabindex="0"]')
        ?.focus();
    });
  }

  istFokus(zeile: number, spalte: number): boolean {
    const f = this.fokus();
    return f.zeile === zeile && f.spalte === spalte;
  }

  klick(zeile: number, spalte: number, zelle: Zelle): void {
    this.setzeFokus(zeile, spalte);
    this.waehle(zelle);
  }

  tastatur(ereignis: KeyboardEvent): void {
    const { zeile, spalte } = this.fokus();
    const letzteZeile = this.zeilenAnzahl() - 1;
    const letzteSpalte = this.spaltenAnzahl() - 1;
    let ziel: { zeile: number; spalte: number } | undefined;

    switch (ereignis.key) {
      case 'ArrowDown':
        ziel = { zeile: Math.min(zeile + 1, letzteZeile), spalte };
        break;
      case 'ArrowUp':
        ziel = { zeile: Math.max(zeile - 1, 0), spalte };
        break;
      case 'ArrowRight':
        ziel = { zeile, spalte: Math.min(spalte + 1, letzteSpalte) };
        break;
      case 'ArrowLeft':
        ziel = { zeile, spalte: Math.max(spalte - 1, 0) };
        break;
      case 'Home':
        ziel = ereignis.ctrlKey ? { zeile: 0, spalte: 0 } : { zeile, spalte: 0 };
        break;
      case 'End':
        ziel = ereignis.ctrlKey
          ? { zeile: letzteZeile, spalte: letzteSpalte }
          : { zeile, spalte: letzteSpalte };
        break;
      case 'Enter':
      case ' ':
        ereignis.preventDefault();
        this.waehle(this.raster().zeilen[zeile].zellen[spalte]);
        return;
      default:
        return;
    }
    ereignis.preventDefault();
    this.setzeFokus(ziel.zeile, ziel.spalte);
  }

  private setzeFokus(zeile: number, spalte: number): void {
    this.fokusAnfordern = true;
    this.fokus.set({ zeile, spalte });
  }

  private waehle(zelle: Zelle): void {
    if (zelle.zustand !== 'frei') {
      // Antwort auf eine Aktion: sofort, nicht erst wenn der Screenreader Pause hat.
      void this.ansage.announce(`${zelle.name}. Nicht buchbar.`, 'assertive');
      return;
    }
    this.slotGewaehlt.emit({ therapeut: zelle.therapeut, beginn: zelle.beginn, name: zelle.name });
  }
}
