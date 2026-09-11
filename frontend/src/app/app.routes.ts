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
  { path: '', redirectTo: 'suche', pathMatch: 'full' },
];
