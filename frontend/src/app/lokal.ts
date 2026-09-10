import { registerLocaleData } from '@angular/common';
import localeDe from '@angular/common/locales/de';
import { LOCALE_ID, Provider } from '@angular/core';

registerLocaleData(localeDe);

/** Datum und Zahlen auf Deutsch - für die Anwendung und für die Tests. */
export const lokalDeutsch: Provider[] = [{ provide: LOCALE_ID, useValue: 'de' }];
