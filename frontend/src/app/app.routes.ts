import { Routes } from '@angular/router';
import { KalenderSeite } from './kalender/kalender-seite';
import { TerminSuche } from './suche/termin-suche';

export const routes: Routes = [
  { path: 'suche', component: TerminSuche, title: 'Terminsuche - Aptum' },
  { path: 'kalender', component: KalenderSeite, title: 'Kalender - Aptum' },
  { path: '', redirectTo: 'suche', pathMatch: 'full' },
];
