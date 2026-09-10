import { HttpInterceptorFn } from '@angular/common/http';
import { InjectionToken, inject } from '@angular/core';

/**
 * Der Mandant, für den das Frontend spricht.
 *
 * Platzhalter für Authentifizierung, wie im Backend (OFFENE-PUNKTE, Punkt 8):
 * In einer echten Anwendung käme er aus einem signierten Token. Hier ist er
 * ein Wert, der an einer Stelle gesetzt und an einer Stelle angehängt wird.
 */
export const MANDANT = new InjectionToken<string>('MANDANT', {
  providedIn: 'root',
  factory: () => 'praxis-a',
});

/** Hängt X-Mandant an jede Anfrage - einmal, nicht in jedem Aufruf. */
export const mandantInterceptor: HttpInterceptorFn = (anfrage, weiter) =>
  weiter(anfrage.clone({ setHeaders: { 'X-Mandant': inject(MANDANT) } }));
