import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { components } from './schema';
import { components as aiKomponenten } from './schema-ai';

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
export type VerordnungAnlage = components['schemas']['VerordnungAnlage'];
export type VerordnungAngelegt = components['schemas']['VerordnungAngelegt'];

/** Aus dem zweiten Dienst, ebenso erzeugt: docs/api/ai-assist-openapi.json. */
export type Erfassung = aiKomponenten['schemas']['Erfassung'];
export type VerordnungVorschlag = aiKomponenten['schemas']['VerordnungVorschlag'];
export type UnsicheresFeld = NonNullable<VerordnungVorschlag['nicht_extrahierbar']>[number];

/**
 * Ein dünner Client: ein Aufruf je Operation, keine Logik.
 *
 * Alle Pfade liegen unter `/api`. Nicht aus Mode: Zweimal hat eine
 * Seitenroute denselben Namen getragen wie ein Endpunkt (`/kalender`,
 * `/erfassung`), und der Entwicklungs-Proxy hat den Seitenaufruf an den
 * Dienst geschickt. Seiten und Schnittstelle teilen sich jetzt keinen
 * Namensraum mehr; der Proxy schneidet `/api` wieder ab, und im Betrieb
 * tut das der Reverse Proxy.
 */
@Injectable({ providedIn: 'root' })
export class AptumApi {
  private readonly http = inject(HttpClient);

  suchen(suche: Suche): Observable<Suchantwort> {
    return this.http.post<Suchantwort>('/api/termine/suche', suche);
  }

  /** Dieselbe Prüfung wie beim Buchen, ohne zu buchen. */
  pruefen(buchung: Buchung): Observable<Buchungsantwort> {
    return this.http.post<Buchungsantwort>('/api/termine/pruefung', buchung);
  }

  /** 201 mit Kennung, oder 409 mit denselben Regeln - der Aufrufer fängt die 409. */
  buchen(buchung: Buchung): Observable<Buchungsantwort> {
    return this.http.post<Buchungsantwort>('/api/termine', buchung);
  }

  /** Freitext einer Verordnung in Felder - der AI-Dienst schlägt vor. */
  erfassen(text: string): Observable<Erfassung> {
    return this.http.post<Erfassung>('/api/erfassung', { text });
  }

  /** Legt die Verordnung an - hier prüft die Domäne, nicht das Modell. */
  verordnungAnlegen(anlage: VerordnungAnlage): Observable<VerordnungAngelegt> {
    return this.http.post<VerordnungAngelegt>('/api/verordnungen', anlage);
  }

  /** Die Woche, in der der Tag liegt. */
  woche(tag: string): Observable<Woche> {
    return this.http.get<Woche>('/api/kalender/woche', { params: { tag } });
  }
}
