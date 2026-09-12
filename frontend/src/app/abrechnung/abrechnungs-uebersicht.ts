import { AG_GRID_LOCALE_DE } from '@ag-grid-community/locale';
import { formatDate } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectionStrategy,
  Component,
  ViewEncapsulation,
  computed,
  inject,
  isDevMode,
  signal,
} from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { AgGridAngular } from 'ag-grid-angular';
import {
  CellStyleModule,
  ClientSideRowModelModule,
  ColDef,
  ColumnAutoSizeModule,
  DateFilterModule,
  GetRowIdParams,
  LocaleModule,
  ModuleRegistry,
  NumberFilterModule,
  QuickFilterModule,
  RowAutoHeightModule,
  TextFilterModule,
  TooltipModule,
  ValidationModule,
  ValueFormatterParams,
  themeQuartz,
} from 'ag-grid-community';
import { of } from 'rxjs';
import { catchError, map, startWith } from 'rxjs/operators';
import { Abrechnungsposten, Abrechnungsuebersicht, AptumApi } from '../api/aptum-api';

// Nur, was die Seite braucht - nicht AllCommunityModule. Fehlt ein Modul,
// sagt ag-grid im Entwicklungsmodus, welches; das Validierungsmodul dafür
// bleibt aus dem Produktionsbündel draußen.
ModuleRegistry.registerModules([
  ClientSideRowModelModule,
  TextFilterModule,
  NumberFilterModule,
  DateFilterModule,
  QuickFilterModule,
  CellStyleModule,
  RowAutoHeightModule,
  TooltipModule,
  LocaleModule,
  ColumnAutoSizeModule,
  ...(isDevMode() ? [ValidationModule] : []),
]);

type Ladezustand =
  | { status: 'laedt' }
  | { status: 'fertig'; uebersicht: Abrechnungsuebersicht }
  | { status: 'fehler'; meldung: string };

/** Die drei Zustände aus der Domäne, für Menschen. Der Code bleibt der Schlüssel für die Klasse. */
export const STATUS_LABEL: Record<string, string> = {
  PRUEFFEST: 'prüffest',
  BEANSTANDET: 'beanstandet',
  NICHT_BEGONNEN: 'nicht begonnen',
};

const THERAPIEFORM_LABEL: Record<string, string> = {
  PHYSIOTHERAPIE: 'Physiotherapie',
  ERGOTHERAPIE: 'Ergotherapie',
};

/**
 * Das Thema kommt aus den Token, nicht aus ag-grid: Jede Farbe hier ist eine
 * Custom Property aus tokens.css, deren Kontrast die CI prüft. ag-grid liefert
 * das Verhalten - Sortieren, Filtern, Tastatur -, die Gestaltung bleibt die
 * der übrigen Anwendung (ADR-006).
 */
const THEMA = themeQuartz.withParams({
  accentColor: 'var(--accent)',
  backgroundColor: 'var(--surface-raised)',
  foregroundColor: 'var(--text-primary)',
  headerBackgroundColor: 'var(--surface-sunken)',
  headerTextColor: 'var(--text-primary)',
  borderColor: 'var(--border-subtle)',
  wrapperBorder: { color: 'var(--border-strong)' },
  wrapperBorderRadius: 'var(--radius-klein)',
  fontFamily: 'var(--schrift-text)',
  focusShadow: '0 0 0 var(--focus-ring-breite) var(--focus-ring)',
});

const datum = (p: ValueFormatterParams<Abrechnungsposten, string | undefined>): string =>
  p.value ? formatDate(p.value, 'dd.MM.yyyy', 'de') : '–';

/**
 * Die Abrechnungsübersicht: eine Zeile je Verordnung, sortier- und filterbar.
 *
 * Hier sitzt ag-grid - und nur hier (ADR-006, ADR-011). Das Kalender-Grid ist
 * ein Zeitgitter und selbst gebaut; das hier ist eine Tabelle über Daten, die
 * bereits stimmen, und genau dafür ist eine Datentabelle das richtige
 * Werkzeug: Sortieren nach Frist, Filtern auf „beanstandet", Tastatur im
 * Raster, ohne dass wir das noch einmal schreiben.
 *
 * Die Zeile sagt nicht nur „beanstandet", sondern welche Regel - dieselben
 * Namen wie im Buchungsdialog, weil das Backend dieselben Regeln über das
 * Erbrachte laufen lässt.
 */
@Component({
  selector: 'app-abrechnungs-uebersicht',
  imports: [AgGridAngular],
  templateUrl: './abrechnungs-uebersicht.html',
  styleUrl: './abrechnungs-uebersicht.css',
  // Die Zellen zeichnet ag-grid, nicht dieses Template; Angulars Kapselung
  // erreicht sie nicht. Alle Selektoren sind deshalb mit .abrechnung geklammert.
  encapsulation: ViewEncapsulation.None,
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AbrechnungsUebersicht {
  private readonly api = inject(AptumApi);

  readonly thema = THEMA;
  readonly sprache = AG_GRID_LOCALE_DE;

  /** Der Schnellfilter über alle Spalten; ag-grid macht daraus die Suche. */
  readonly filter = signal('');

  readonly zustand = toSignal(
    this.api.abrechnung().pipe(
      map((uebersicht): Ladezustand => ({ status: 'fertig', uebersicht })),
      catchError((fehler: HttpErrorResponse) =>
        of<Ladezustand>({
          status: 'fehler',
          meldung:
            fehler.status === 0
              ? 'Das Backend ist nicht erreichbar.'
              : `Die Abrechnung konnte nicht geladen werden (Status ${fehler.status}).`,
        }),
      ),
      startWith<Ladezustand>({ status: 'laedt' }),
    ),
    { initialValue: { status: 'laedt' } as Ladezustand },
  );

  readonly posten = computed<Abrechnungsposten[]>(() => {
    const z = this.zustand();
    return z.status === 'fertig' ? (z.uebersicht.posten ?? []) : [];
  });

  /** Die Summen über alle Zeilen - aus dem Backend, nicht hier nachgezählt. */
  readonly summe = computed(() => {
    const z = this.zustand();
    if (z.status !== 'fertig') return '';
    const u = z.uebersicht;
    return (
      `${u.posten?.length ?? 0} Verordnungen, ${u.erbracht ?? 0} Einheiten erbracht, ` +
      `${u.prueffest ?? 0} prüffest, ${u.beanstandet ?? 0} beanstandet.`
    );
  });

  readonly spalten: ColDef<Abrechnungsposten>[] = [
    {
      field: 'verordnung',
      headerName: 'Verordnung',
      valueFormatter: (p) => (p.value ?? '').slice(0, 8),
      tooltipField: 'verordnung',
      cellClass: 'zahl',
      minWidth: 120,
      maxWidth: 140,
    },
    {
      field: 'ausstellungsdatum',
      headerName: 'Ausgestellt',
      valueFormatter: datum,
      cellClass: 'zahl',
      minWidth: 130,
    },
    {
      field: 'therapieform',
      headerName: 'Therapieform',
      valueFormatter: (p) => THERAPIEFORM_LABEL[p.value ?? ''] ?? p.value ?? '',
      minWidth: 140,
    },
    { field: 'diagnosegruppe', headerName: 'Gruppe', maxWidth: 110 },
    {
      field: 'verordnet',
      headerName: 'Verordnet',
      type: 'numericColumn',
      cellClass: 'zahl',
      minWidth: 120,
    },
    {
      field: 'erbracht',
      headerName: 'Erbracht',
      type: 'numericColumn',
      cellClass: 'zahl',
      minWidth: 120,
    },
    {
      field: 'offen',
      headerName: 'Offen',
      type: 'numericColumn',
      cellClass: 'zahl',
      minWidth: 120,
    },
    {
      field: 'ersteBehandlung',
      headerName: 'Erste Behandlung',
      valueFormatter: datum,
      cellClass: 'zahl',
      minWidth: 130,
    },
    {
      field: 'letzteBehandlung',
      headerName: 'Letzte Behandlung',
      valueFormatter: datum,
      cellClass: 'zahl',
      minWidth: 130,
    },
    {
      field: 'status',
      headerName: 'Status',
      valueFormatter: (p) => STATUS_LABEL[p.value ?? ''] ?? p.value ?? '',
      // Nie Farbe allein: Die Klasse färbt, der Text sagt es.
      cellClassRules: {
        'status-prueffest': (p) => p.value === 'PRUEFFEST',
        'status-beanstandet': (p) => p.value === 'BEANSTANDET',
        'status-offen': (p) => p.value === 'NICHT_BEGONNEN',
      },
      minWidth: 140,
    },
    {
      field: 'begruendung',
      headerName: 'Begründung',
      flex: 2,
      minWidth: 260,
      wrapText: true,
      autoHeight: true,
      sortable: false,
    },
  ];

  readonly standard: ColDef<Abrechnungsposten> = {
    sortable: true,
    filter: true,
    resizable: true,
    minWidth: 100,
    flex: 1,
    // Kopfzeilen brechen um, statt zu „Ausge…“ zu werden.
    wrapHeaderText: true,
    autoHeaderHeight: true,
  };

  zeilenId(p: GetRowIdParams<Abrechnungsposten>): string {
    return p.data.verordnung ?? '';
  }

  filtern(ereignis: Event): void {
    this.filter.set((ereignis.target as HTMLInputElement).value);
  }
}
