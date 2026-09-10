import { HttpErrorResponse } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { EMPTY, Observable, merge, of, throwError, timer } from 'rxjs';
import {
  catchError,
  debounceTime,
  distinctUntilChanged,
  map,
  retry,
  share,
  startWith,
  switchMap,
  withLatestFrom,
} from 'rxjs/operators';
import { AptumApi, Suchantwort, Suche } from '../api/aptum-api';

/** Was die Seite sehen darf: einer von vier Zuständen, nie zwei zugleich. */
export type Suchzustand =
  | { status: 'leer' }
  | { status: 'laedt' }
  | { status: 'fertig'; antwort: Suchantwort }
  | { status: 'fehler'; meldung: string };

export const LEER: Suchzustand = { status: 'leer' };

/**
 * Der kanonische Suchflow (Skill angular-rxjs). Eine Kette, sonst nichts.
 *
 * Eingabe ist der Strom der Suchanfragen, wie das Formular sie liefert.
 * Ausgabe ist der Strom der Zustände, den die Komponente als Signal hält.
 * Zwischen beiden: Entprellen, Abbrechen, Wiederholen, Fehler abfangen -
 * alles, wo Zeit eine Rolle spielt, gehört hierher und nicht in die Seite.
 */
@Injectable({ providedIn: 'root' })
export class TerminSucheService {
  private readonly api = inject(AptumApi);

  /**
   * @param anfragen was das Formular liefert
   * @param erneut   ein Tick, wenn dieselbe Suche noch einmal laufen soll -
   *                 etwa nach einer Buchung, die einen Vorschlag verbraucht hat
   */
  zustand(anfragen: Observable<Suche>, erneut: Observable<void> = EMPTY): Observable<Suchzustand> {
    const entprellt = anfragen.pipe(
      // Wer tippt, löst keine Anfrage je Taste aus.
      debounceTime(300),
      distinctUntilChanged((a, b) => JSON.stringify(a) === JSON.stringify(b)),
      share(),
    );
    const wiederholt = erneut.pipe(
      withLatestFrom(entprellt),
      map(([, suche]) => suche),
    );
    return merge(entprellt, wiederholt).pipe(
      // switchMap, nicht mergeMap: Die neue Anfrage bricht die alte ab. Sonst
      // gewinnt die langsamste Antwort und überschreibt das richtige Ergebnis.
      switchMap((suche) =>
        this.api.suchen(suche).pipe(
          // Wiederholen nur, wenn das Netz oder der Server hing. Eine 400 ist
          // eine Antwort - dieselbe Anfrage noch einmal ändert daran nichts.
          retry({
            count: 2,
            delay: (fehler: HttpErrorResponse, versuch) =>
              fehler.status === 0 || fehler.status >= 500
                ? timer(versuch * 500)
                : throwError(() => fehler),
          }),
          map((antwort): Suchzustand => ({ status: 'fertig', antwort })),
          // catchError innen: Ein Fehler beendet diese eine Suche, nicht den
          // Strom. Stünde es außen, wäre die Suche bis zum Neuladen tot.
          catchError((fehler: HttpErrorResponse) =>
            of<Suchzustand>({ status: 'fehler', meldung: meldungAus(fehler) }),
          ),
          startWith<Suchzustand>({ status: 'laedt' }),
        ),
      ),
    );
  }
}

function meldungAus(fehler: HttpErrorResponse): string {
  if (fehler.status === 0) {
    return 'Das Backend ist nicht erreichbar.';
  }
  const vomServer = (fehler.error as { fehler?: string } | null)?.fehler;
  return vomServer ?? `Die Suche ist fehlgeschlagen (Status ${fehler.status}).`;
}
