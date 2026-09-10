import { HttpErrorResponse } from '@angular/common/http';
import { DatePipe } from '@angular/common';
import {
  ChangeDetectionStrategy,
  Component,
  ElementRef,
  afterRenderEffect,
  computed,
  inject,
  signal,
} from '@angular/core';
import { toObservable, toSignal } from '@angular/core/rxjs-interop';
import { ActivatedRoute } from '@angular/router';
import { of } from 'rxjs';
import { catchError, map, startWith, switchMap } from 'rxjs/operators';
import { AptumApi, Woche } from '../api/aptum-api';
import { WOCHENTAGE, heute, montagVon, tagePlus } from '../zeit';
import { KalenderGrid, SlotWahl } from './kalender-grid';
import { ZUSTAND_LABEL, Zustand, rasterFuer } from './raster';

type Ladezustand =
  { status: 'laedt' } | { status: 'fertig'; woche: Woche } | { status: 'fehler'; meldung: string };

/** Die Legende erklärt die Muster; dieselben Klassen wie im Gitter. */
const LEGENDE: readonly { zustand: Zustand; erklaerung: string }[] = [
  { zustand: 'frei', erklaerung: 'buchbar' },
  { zustand: 'belegt', erklaerung: 'Behandlung, mit Heilmittel und Raum' },
  { zustand: 'ruestzeit', erklaerung: 'Vor- und Nachbereitung, gehört zum Termin' },
  { zustand: 'nachruhe', erklaerung: 'bindet den Raum, nicht die Therapeutin' },
  { zustand: 'abwesenheit', erklaerung: 'Urlaub, Fortbildung' },
  { zustand: 'gesperrt', erklaerung: 'außerhalb der Arbeitszeit' },
];

/**
 * Die Wochenansicht: ein Tag im Gitter, Reiter für die Tage, Pfeile für die
 * Wochen. Die Woche kommt aus dem Backend; frei ist, was dort nicht steht.
 */
@Component({
  selector: 'app-kalender-seite',
  imports: [KalenderGrid, DatePipe],
  templateUrl: './kalender-seite.html',
  styleUrl: './kalender-seite.css',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class KalenderSeite {
  private readonly api = inject(AptumApi);

  readonly wochentage = WOCHENTAGE;
  readonly legende = LEGENDE;
  readonly label = ZUSTAND_LABEL;

  /** Der gewählte Tag; der Montag seiner Woche bestimmt, was geladen wird. */
  /** Startwert aus ?tag=, damit Links und Tests in eine bekannte Woche springen. */
  readonly tag = signal(inject(ActivatedRoute).snapshot.queryParamMap.get('tag') ?? heute());
  readonly montag = computed(() => montagVon(this.tag()));

  readonly zustand = toSignal(
    toObservable(this.montag).pipe(
      // switchMap: Wer schnell blättert, sieht die Woche, bei der er ankommt.
      switchMap((montag) =>
        this.api.woche(montag).pipe(
          map((woche): Ladezustand => ({ status: 'fertig', woche })),
          catchError((fehler: HttpErrorResponse) =>
            of<Ladezustand>({
              status: 'fehler',
              meldung:
                fehler.status === 0
                  ? 'Das Backend ist nicht erreichbar.'
                  : `Die Woche konnte nicht geladen werden (Status ${fehler.status}).`,
            }),
          ),
          startWith<Ladezustand>({ status: 'laedt' }),
        ),
      ),
    ),
    { initialValue: { status: 'laedt' } as Ladezustand },
  );

  readonly raster = computed(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? rasterFuer(z.woche, this.tag()) : undefined;
  });

  /** Die Tage der Woche für die Reiter: Datum und Name. */
  readonly tage = computed(() =>
    this.wochentage.map((w, i) => ({ ...w, datum: tagePlus(this.montag(), i) })),
  );

  /** Die Statuszeile am unteren Rand - und die Live-Region. */
  readonly status = signal('');

  waehleTag(datum: string): void {
    this.tag.set(datum);
  }

  /**
   * Reiter nach dem Tabs-Muster: Tab springt auf den aktiven Reiter, die
   * Pfeile wechseln den Tag, Home und End an den Rand. Ohne das wären die
   * inaktiven Reiter mit tabindex -1 per Tastatur unerreichbar - ein
   * Befund des a11y-Audits, den kein axe-Lauf zeigt.
   */
  reiterTastatur(ereignis: KeyboardEvent): void {
    const tage = this.tage();
    const i = tage.findIndex((t) => t.datum === this.tag());
    let ziel: number;
    switch (ereignis.key) {
      case 'ArrowRight':
        ziel = (i + 1) % tage.length;
        break;
      case 'ArrowLeft':
        ziel = (i - 1 + tage.length) % tage.length;
        break;
      case 'Home':
        ziel = 0;
        break;
      case 'End':
        ziel = tage.length - 1;
        break;
      default:
        return;
    }
    ereignis.preventDefault();
    this.tag.set(tage[ziel].datum);
    this.reiterFokussieren = true;
  }

  private reiterFokussieren = false;

  constructor() {
    afterRenderEffect(() => {
      this.tag();
      if (!this.reiterFokussieren) return;
      this.reiterFokussieren = false;
      this.host.nativeElement.querySelector<HTMLElement>('[role="tab"][tabindex="0"]')?.focus();
    });
  }

  private readonly host = inject<ElementRef<HTMLElement>>(ElementRef);

  blaettere(wochen: number): void {
    this.tag.set(tagePlus(this.tag(), wochen * 7));
  }

  slotGewaehlt(wahl: SlotWahl): void {
    // Der Buchungsdialog folgt; bis dahin sagt die Statuszeile, was gewählt ist.
    this.status.set(`Gewählt: ${wahl.name}. Die Buchung folgt mit dem Dialog.`);
  }
}
