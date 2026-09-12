import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectionStrategy, Component, computed, inject, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import {
  FormsModule,
  NonNullableFormBuilder,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Router } from '@angular/router';
import { Subject, of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';
import { AptumApi, Erfassung, UnsicheresFeld } from '../api/aptum-api';
import { HEILMITTEL } from '../suche/heilmittel';

const DIAGNOSEGRUPPEN = ['WS', 'EX', 'CS', 'AT', 'GE', 'SO', 'LY', 'ZN', 'PN'] as const;

type Zustand =
  | { status: 'leer' }
  | { status: 'laedt' }
  | { status: 'fertig'; erfassung: Erfassung }
  | { status: 'fehler'; meldung: string };

const LEER: Zustand = { status: 'leer' };

/**
 * Die Felder, die das Formular braucht. Das Modell liest zwei weitere -
 * Heilmittel (gehört zur Terminsuche) und Hausbesuch (führt die Domäne
 * nicht). Beide werden gezeigt, aber nicht als Lücke gezählt: Sonst
 * verlangt die Seite, etwas zu ergänzen, wofür es kein Feld gibt.
 */
const FORMULARFELDER: readonly UnsicheresFeld[] = [
  'ausstellungsdatum',
  'diagnosegruppe',
  'verordnete_einheiten',
  'frequenz',
];

/**
 * Die Seite, auf der Regel 3 sichtbar wird.
 *
 * Ein Sprachmodell liest den Freitext einer Verordnung in Felder und sagt
 * dazu, was es *nicht* lesen konnte. Ein Mensch ergänzt und bestätigt. Erst
 * dann geht die Verordnung an die Domäne, und dort wird sie geprüft - nicht
 * weil das Modell schlecht wäre, sondern weil es nicht zuständig ist.
 *
 * Deshalb ist das Formular nach dem Vorschlag nicht gesperrt: Jedes Feld
 * bleibt änderbar, und die unsicheren sind leer und benannt.
 */
@Component({
  selector: 'app-verordnung-erfassen',
  imports: [ReactiveFormsModule, FormsModule],
  templateUrl: './verordnung-erfassen.html',
  styleUrl: './verordnung-erfassen.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
  host: { class: 'seite' },
})
export class VerordnungErfassen {
  private readonly fb = inject(NonNullableFormBuilder);
  private readonly api = inject(AptumApi);
  private readonly router = inject(Router);

  readonly heilmittel = HEILMITTEL;
  readonly diagnosegruppen = DIAGNOSEGRUPPEN;

  /** Der Freitext. Kein Strom mit Entprellung: Ein Modellaufruf kostet Geld. */
  readonly text = signal('');

  private readonly abschicken = new Subject<string>();

  readonly zustand = toSignal(
    this.abschicken.pipe(
      switchMap((text) =>
        this.api.erfassen(text).pipe(
          map((erfassung): Zustand => ({ status: 'fertig', erfassung })),
          catchError((fehler: HttpErrorResponse) =>
            of<Zustand>({ status: 'fehler', meldung: meldungAus(fehler) }),
          ),
          startWith<Zustand>({ status: 'laedt' }),
        ),
      ),
    ),
    { initialValue: LEER },
  );

  /** Was die Verordnung trägt. Sechs Felder - das Modell liest sieben. */
  readonly form = this.fb.group({
    ausstellungsdatum: ['', Validators.required],
    diagnosegruppe: ['', Validators.required],
    verordneteEinheiten: [0, [Validators.required, Validators.min(1)]],
    frequenzMin: [0, [Validators.required, Validators.min(1)]],
    frequenzMax: [0, [Validators.required, Validators.min(1)]],
    dringlicherBedarf: [false],
  });

  /** Das Heilmittel gehört nicht zur Verordnung; es wird bei der Suche gewählt. */
  readonly heilmittelVorschlag = signal<string>('');

  readonly unsicher = computed<UnsicheresFeld[]>(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? (z.erfassung.vorschlag.nicht_extrahierbar ?? []) : [];
  });

  readonly hinweise = computed<string[]>(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? (z.erfassung.vorschlag.hinweise ?? []) : [];
  });

  readonly pseudonymisiert = computed(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? z.erfassung.pseudonymisiert : 0;
  });

  readonly provider = computed(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? z.erfassung.provider : '';
  });

  readonly angelegt = signal<string>('');
  readonly anlageFehler = signal<string>('');

  /** Eine Zeile für die Live-Region - wer nicht sieht, hört, was passiert ist. */
  readonly statusText = computed(() => {
    const z = this.zustand();
    switch (z.status) {
      case 'laedt':
        return 'Der Freitext wird gelesen.';
      case 'fertig': {
        const offen = this.unsicher().filter((f) => FORMULARFELDER.includes(f)).length;
        return offen === 0
          ? 'Alle Felder erkannt. Bitte prüfen und bestätigen.'
          : `${offen} Felder konnten nicht gelesen werden und sind leer. Bitte ergänzen.`;
      }
      default:
        return '';
    }
  });

  vorschlagHolen(): void {
    const text = this.text().trim();
    if (!text) return;
    this.angelegt.set('');
    this.anlageFehler.set('');
    this.abschicken.next(text);
  }

  /** Übernimmt den Vorschlag ins Formular. Unsichere Felder bleiben leer. */
  uebernehmen(e: Erfassung): void {
    const v = e.vorschlag;
    const offen = new Set(v.nicht_extrahierbar ?? []);
    this.form.patchValue({
      ausstellungsdatum: offen.has('ausstellungsdatum') ? '' : (v.ausstellungsdatum ?? ''),
      diagnosegruppe: offen.has('diagnosegruppe') ? '' : (v.diagnosegruppe ?? ''),
      verordneteEinheiten: offen.has('verordnete_einheiten') ? 0 : (v.verordnete_einheiten ?? 0),
      frequenzMin: offen.has('frequenz') ? 0 : (v.frequenz?.min_pro_woche ?? 0),
      frequenzMax: offen.has('frequenz') ? 0 : (v.frequenz?.max_pro_woche ?? 0),
      dringlicherBedarf: v.dringlicher_bedarf ?? false,
    });
    this.heilmittelVorschlag.set(offen.has('heilmittel') ? '' : (v.heilmittel ?? ''));
  }

  istUnsicher(feld: UnsicheresFeld): boolean {
    return this.unsicher().includes(feld);
  }

  /** Der Mensch bestätigt, die Domäne entscheidet. */
  anlegen(): void {
    if (this.form.invalid) return;
    const w = this.form.getRawValue();
    this.api
      .verordnungAnlegen({
        ausstellungsdatum: w.ausstellungsdatum,
        diagnosegruppe: w.diagnosegruppe,
        verordneteEinheiten: w.verordneteEinheiten,
        frequenzMin: w.frequenzMin,
        frequenzMax: w.frequenzMax,
        dringlicherBedarf: w.dringlicherBedarf,
      })
      .subscribe({
        next: (a) => {
          this.angelegt.set(a.id ?? '');
          this.anlageFehler.set('');
        },
        // Die Domäne lehnt ab, wenn der Vorschlag fachlich nicht trägt -
        // etwa eine Menge über der Höchstmenge der Diagnosegruppe.
        error: (f: HttpErrorResponse) => this.anlageFehler.set(meldungAus(f)),
      });
  }

  zurSuche(): void {
    void this.router.navigate(['/suche'], {
      queryParams: { verordnung: this.angelegt(), heilmittel: this.heilmittelVorschlag() || null },
    });
  }
}

function meldungAus(fehler: HttpErrorResponse): string {
  if (fehler.status === 0) return 'Der Dienst ist nicht erreichbar.';
  const vomServer = (fehler.error as { fehler?: string; detail?: unknown } | null)?.fehler;
  return vomServer ?? `Die Anfrage ist fehlgeschlagen (Status ${fehler.status}).`;
}
