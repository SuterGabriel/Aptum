import { Routes } from '@angular/router';
import { TerminSuche } from './suche/termin-suche';

export const routes: Routes = [
  { path: 'suche', component: TerminSuche, title: 'Terminsuche - Aptum' },
  { path: '', redirectTo: 'suche', pathMatch: 'full' },
];
