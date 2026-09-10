import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { DatePipe } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  inject,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { Subject, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';
import { AptumApi, Buchung, Buchungsantwort, Regel } from '../api/aptum-api';

/** Was der Dialog braucht: den Vorschlag und die Verordnung, für die er gilt. */
export interface BuchungsVorhaben {
  readonly verordnung: string;
  readonly heilmittel: string;
  readonly heilmittelBezeichnung: string;
  readonly therapeut: string;
  readonly raum: string;
  readonly beginn: string;
  readonly ende: string;
}

/** Womit der Dialog schließt: die Antwort des Backends, oder nichts bei Abbruch. */
export type BuchungsErgebnis = Buchungsantwort | undefined;

type Pruefzustand =
  | { status: 'laedt' }
  | { status: 'fertig'; antwort: Buchungsantwort }
  | { status: 'fehler'; meldung: string };

/**
 * Der wichtigste Screen: die Regelprüfung als benannte Liste, bevor jemand
 * entscheidet.
 *
 * Beim Öffnen fragt der Dialog dieselbe Prüfung ab, die die Buchung
 * durchläuft - Regel 3 aus CLAUDE.md, sichtbar gemacht. Ist eine Regel
 * verletzt, ist Buchen gesperrt; wer trotzdem will, begründet (ADR-009).
 * Focus Trap, Escape und Fokus zurück auf den Auslöser kommen vom CDK.
 */
@Component({
  selector: 'app-buchungs-dialog',
  imports: [DatePipe, FormsModule],
  templateUrl: './buchungs-dialog.html',
  styleUrl: './buchungs-dialog.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class BuchungsDialog {
  readonly vorhaben = inject<BuchungsVorhaben>(DIALOG_DATA);
  private readonly ref = inject<DialogRef<BuchungsErgebnis>>(DialogRef);
  private readonly api = inject(AptumApi);

  /** Jede Prüfung und jede Buchung läuft durch denselben Strom; die Antwort ersetzt die Liste. */
  private readonly aktionen = new Subject<Buchung | undefined>();

  readonly zustand = toSignal(
    this.aktionen.pipe(
      startWith(undefined),
      switchMap((buchung) =>
        (buchung ? this.api.buchen(buchung) : this.api.pruefen(this.buchung())).pipe(
          map((antwort): Pruefzustand => ({ status: 'fertig', antwort })),
          catchError((fehler: HttpErrorResponse) =>
            // 409 ist eine Antwort mit Regeln, kein Fehler: blockiert, mit Begründung.
            fehler.status === 409 && fehler.error?.regeln
              ? of<Pruefzustand>({ status: 'fertig', antwort: fehler.error as Buchungsantwort })
              : of<Pruefzustand>({ status: 'fehler', meldung: meldungAus(fehler) }),
          ),
          startWith<Pruefzustand>({ status: 'laedt' }),
        ),
      ),
    ),
    { initialValue: { status: 'laedt' } as Pruefzustand },
  );

  readonly regeln = computed<Regel[]>(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? (z.antwort.regeln ?? []) : [];
  });

  readonly blockiert = computed(() => {
    const z = this.zustand();
    return z.status === 'fertig' && z.antwort.ausgang === 'BLOCKIERT';
  });

  readonly buchbar = computed(() => {
    const z = this.zustand();
    return z.status === 'fertig' && z.antwort.ausgang !== 'BLOCKIERT' && !z.antwort.termin;
  });

  readonly begruendung = signal('');
  readonly darfUebersteuern = computed(() => this.begruendung().trim().length >= 10);

  readonly zusammenfassung = computed(() => {
    const anzahl = { ERFUELLT: 0, WARNUNG: 0, VERLETZT: 0 } as Record<string, number>;
    for (const r of this.regeln()) anzahl[r.ausgang ?? ''] = (anzahl[r.ausgang ?? ''] ?? 0) + 1;
    return anzahl;
  });

  constructor() {
    // Sobald eine Antwort eine Kennung trägt, ist gebucht: Der Dialog
    // schließt mit ihr, und die Seite meldet und lädt neu.
    effect(() => {
      const z = this.zustand();
      if (z.status === 'fertig' && z.antwort.termin) this.ref.close(z.antwort);
    });
  }

  buchen(): void {
    this.aktionen.next(this.buchung());
  }

  trotzdemBuchen(): void {
    this.aktionen.next({
      ...this.buchung(),
      uebersteuerung: { begruendung: this.begruendung().trim(), von: 'Rezeption' },
    });
  }

  abbrechen(): void {
    this.ref.close(undefined);
  }

  ausgangText(r: Regel): string {
    return AUSGANG_TEXT[r.ausgang ?? ''] ?? r.ausgang ?? '';
  }

  /** Die Zone des Wertes, nicht des Browsers - wie in der Terminsuche. */
  zone(): string {
    return this.vorhaben.beginn.match(/([+-]\d{2}:\d{2}|Z)$/)?.[1] ?? '+01:00';
  }

  private buchung(): Buchung {
    const v = this.vorhaben;
    return {
      verordnung: v.verordnung,
      heilmittel: v.heilmittel,
      therapeut: v.therapeut,
      raum: v.raum,
      beginn: v.beginn,
    };
  }
}

const AUSGANG_TEXT: Record<string, string> = {
  ERFUELLT: 'erfüllt',
  WARNUNG: 'Warnung',
  VERLETZT: 'verletzt',
};

function meldungAus(fehler: HttpErrorResponse): string {
  if (fehler.status === 0) return 'Das Backend ist nicht erreichbar.';
  return (
    (fehler.error as { fehler?: string } | null)?.fehler ??
    `Die Prüfung ist fehlgeschlagen (Status ${fehler.status}).`
  );
}
