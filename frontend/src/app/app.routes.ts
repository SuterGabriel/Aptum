import { Routes } from '@angular/router';
import { VerordnungErfassen } from './erfassung/verordnung-erfassen';
import { KalenderSeite } from './kalender/kalender-seite';
import { TerminSuche } from './suche/termin-suche';

export const routes: Routes = [
  {
    path: 'erfassung',
    component: VerordnungErfassen,
    title: 'Verordnung erfassen - Aptum',
  },
  { path: 'suche', component: TerminSuche, title: 'Terminsuche - Aptum' },
  { path: 'kalender', component: KalenderSeite, title: 'Kalender - Aptum' },
  {
    path: 'abrechnung',
    // Lazy, und zwar nur diese Seite: ag-grid wiegt mehr als der Rest der
    // Anwendung zusammen. Wer Termine sucht, lädt es nicht.
    loadComponent: () =>
      import('./abrechnung/abrechnungs-uebersicht').then((m) => m.AbrechnungsUebersicht),
    title: 'Abrechnung - Aptum',
  },
  { path: '', redirectTo: 'suche', pathMatch: 'full' },
];
