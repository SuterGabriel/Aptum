import { DatePipe } from '@angular/common';
import { ChangeDetectionStrategy, Component, computed, inject } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  AbstractControl,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { filter, map } from 'rxjs/operators';
import { Suche, Terminvorschlag, Wochentag } from '../api/aptum-api';
import { WOCHENTAGE } from '../zeit';
import { HEILMITTEL } from './heilmittel';
import { LEER, TerminSucheService } from './termin-suche.service';

const UUID = /^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$/i;

/** Ein Wunschfenster ist gültig, wenn es nicht vor sich selbst endet. */
function fensterGueltig(gruppe: AbstractControl) {
  const w = gruppe.value;
  const tage = w.von <= w.bis;
  const zeit = w.fruehestens < w.spaetestens;
  return tage && zeit ? null : { fenster: { tage, zeit } };
}

/**
 * Die erste Seite: Suchkriterien links, nach den vier Dimensionen gegliedert,
 * Vorschläge rechts.
 *
 * Die Komponente hält keinen Suchzustand selbst. Sie macht aus dem Formular
 * einen Strom, gibt ihn dem Service und zeigt an, was zurückkommt.
 */
@Component({
  selector: 'app-termin-suche',
  imports: [ReactiveFormsModule, DatePipe],
  templateUrl: './termin-suche.html',
  styleUrl: './termin-suche.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class TerminSuche {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly suche = inject(TerminSucheService);

  readonly heilmittel = HEILMITTEL;
  readonly wochentage = WOCHENTAGE;

  readonly form = this.fb.group({
    verordnung: ['', [Validators.required, Validators.pattern(UUID)]],
    heilmittel: ['KG_EINZEL', Validators.required],
    fenster: this.fb.group(
      {
        von: [heute(0), Validators.required],
        bis: [heute(14), Validators.required],
        fruehestens: ['08:00', Validators.required],
        spaetestens: ['18:00', Validators.required],
        tage: this.fb.group({
          MONDAY: true,
          TUESDAY: true,
          WEDNESDAY: true,
          THURSDAY: true,
          FRIDAY: true,
          SATURDAY: false,
        }),
      },
      { validators: fensterGueltig },
    ),
  });

  /** Das Ergebnis als Signal. Kein subscribe, kein Leck, OnPush läuft an. */
  readonly zustand = toSignal(
    this.suche.zustand(
      this.form.valueChanges.pipe(
        filter(() => this.form.valid),
        map(() => this.alsSuche()),
      ),
    ),
    { initialValue: LEER },
  );

  /** Eine Zeile für die Live-Region: Wer nicht sieht, hört so, was passiert ist. */
  readonly statusText = computed(() => {
    const z = this.zustand();
    switch (z.status) {
      case 'laedt':
        return 'Suche läuft.';
      case 'fertig':
        return `${z.antwort.vorschlaege?.length ?? 0} Vorschläge gefunden.`;
      default:
        return '';
    }
  });

  /** Ausgeschlossene Vorschläge je Regel, als Liste für die Anzeige. */
  readonly ausgeschlossen = computed(() => {
    const z = this.zustand();
    if (z.status !== 'fertig') return [];
    return Object.entries(z.antwort.ausgeschlossen ?? {}).map(([regel, anzahl]) => ({
      regel,
      anzahl,
    }));
  });

  fensterFehler(): { tage: boolean; zeit: boolean } | undefined {
    return this.form.controls.fenster.errors?.['fenster'];
  }

  /**
   * Die Zone, in der ein Zeitstempel geliefert wurde - die der Praxis. Das
   * Backend schreibt sie in jeden Wert; die Anzeige soll sie zeigen und nicht
   * die des Browsers. Sonst steht in einer CI unter UTC 08:00 statt 09:00.
   */
  zone(iso: string | undefined): string {
    return iso?.match(/([+-]\d{2}:\d{2}|Z)$/)?.[1] ?? '+01:00';
  }

  dauerMinuten(v: Terminvorschlag): number {
    if (!v.beginn || !v.ende) return 0;
    return Math.round((Date.parse(v.ende) - Date.parse(v.beginn)) / 60_000);
  }

  private alsSuche(): Suche {
    const w = this.form.getRawValue();
    const tage = Object.entries(w.fenster.tage)
      .filter(([, gewaehlt]) => gewaehlt)
      .map(([tag]) => tag as Wochentag);
    return {
      verordnung: w.verordnung,
      heilmittel: w.heilmittel,
      von: w.fenster.von,
      bis: w.fenster.bis,
      fruehestens: w.fenster.fruehestens,
      spaetestens: w.fenster.spaetestens,
      wochentage: tage,
    };
  }
}

function heute(plusTage: number): string {
  const d = new Date();
  d.setDate(d.getDate() + plusTage);
  return d.toISOString().slice(0, 10);
}
