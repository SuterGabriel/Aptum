import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { components } from './schema';

/**
 * Die Typen der Schnittstelle, wie sie das Backend beschreibt.
 *
 * Aus docs/api/openapi.json erzeugt (npm run api:types). Ändert sich ein
 * Controller, ändert sich das Dokument, ändern sich diese Typen - und der
 * Compiler zeigt, wo das Frontend nachziehen muss.
 */
export type Suche = components['schemas']['Suche'];
export type Suchantwort = components['schemas']['Suchantwort'];
export type Terminvorschlag = components['schemas']['Terminvorschlag'];
export type Regel = components['schemas']['Regel'];
export type Wochentag = NonNullable<Suche['wochentage']>[number];
export type Woche = components['schemas']['Woche'];
export type Buchung = components['schemas']['Buchung'];
export type Buchungsantwort = components['schemas']['Buchungsantwort'];
export type Spalte = components['schemas']['Spalte'];
export type Belegung = components['schemas']['Belegung'];

/** Ein dünner Client: ein Aufruf je Operation, keine Logik. */
@Injectable({ providedIn: 'root' })
export class AptumApi {
  private readonly http = inject(HttpClient);

  suchen(suche: Suche): Observable<Suchantwort> {
    return this.http.post<Suchantwort>('/termine/suche', suche);
  }

  /** Dieselbe Prüfung wie beim Buchen, ohne zu buchen. */
  pruefen(buchung: Buchung): Observable<Buchungsantwort> {
    return this.http.post<Buchungsantwort>('/termine/pruefung', buchung);
  }

  /** 201 mit Kennung, oder 409 mit denselben Regeln - der Aufrufer fängt die 409. */
  buchen(buchung: Buchung): Observable<Buchungsantwort> {
    return this.http.post<Buchungsantwort>('/termine', buchung);
  }

  /** Die Woche, in der der Tag liegt. */
  woche(tag: string): Observable<Woche> {
    return this.http.get<Woche>('/kalender/woche', { params: { tag } });
  }
}
