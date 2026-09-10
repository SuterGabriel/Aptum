import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { Suche } from '../api/aptum-api';
import { Suchzustand, TerminSucheService } from './termin-suche.service';

/**
 * Die Zusagen des Suchflows, jede einzeln nachgewiesen: entprellt,
 * abgebrochen, überlebt, wiederholt nur wo es Sinn hat.
 */
describe('TerminSucheService', () => {
  let service: TerminSucheService;
  let http: HttpTestingController;
  let anfragen: Subject<Suche>;
  let zustaende: Suchzustand[];

  const suche = (verordnung: string): Suche => ({ verordnung, heilmittel: 'KG_EINZEL' });
  const letzter = () => zustaende[zustaende.length - 1];

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(TerminSucheService);
    http = TestBed.inject(HttpTestingController);
    anfragen = new Subject<Suche>();
    zustaende = [];
    service.zustand(anfragen).subscribe((z) => zustaende.push(z));
  });

  afterEach(() => http.verify());

  it('entprellt: drei Eingaben in 300 ms sind eine Anfrage', fakeAsync(() => {
    anfragen.next(suche('a'));
    tick(100);
    anfragen.next(suche('ab'));
    tick(100);
    anfragen.next(suche('abc'));
    tick(300);

    const offen = http.match('/termine/suche');
    expect(offen.length).toBe(1);
    expect(offen[0].request.body.verordnung).toBe('abc');
    offen[0].flush({ vorschlaege: [] });
  }));

  it('bricht ab: die neue Anfrage lässt die alte nicht mehr ankommen', fakeAsync(() => {
    anfragen.next(suche('erste'));
    tick(300);
    const erste = http.expectOne((r) => r.body.verordnung === 'erste');

    anfragen.next(suche('zweite'));
    tick(300);
    const zweite = http.expectOne((r) => r.body.verordnung === 'zweite');

    expect(erste.cancelled).toBeTrue();
    zweite.flush({ vorschlaege: [], zusammenfassung: 'zweite' });

    const z = letzter();
    expect(z.status).toBe('fertig');
    expect(z.status === 'fertig' && z.antwort.zusammenfassung).toBe('zweite');
  }));

  it('überlebt: nach einem Fehler funktioniert die nächste Suche', fakeAsync(() => {
    anfragen.next(suche('kaputt'));
    tick(300);
    http
      .expectOne('/termine/suche')
      .flush({ fehler: 'Unbekannte Verordnung' }, { status: 400, statusText: 'Bad Request' });
    expect(letzter()).toEqual({ status: 'fehler', meldung: 'Unbekannte Verordnung' });

    anfragen.next(suche('heil'));
    tick(300);
    http.expectOne('/termine/suche').flush({ vorschlaege: [{}] });
    expect(letzter().status).toBe('fertig');
  }));

  it('wiederholt bei 503 mit Abstand, aber nicht bei 400', fakeAsync(() => {
    anfragen.next(suche('wackelig'));
    tick(300);
    http.expectOne('/termine/suche').flush(null, { status: 503, statusText: 'Unavailable' });
    tick(500);
    http.expectOne('/termine/suche').flush({ vorschlaege: [] });
    expect(letzter().status).toBe('fertig');

    anfragen.next(suche('falsch'));
    tick(300);
    http.expectOne('/termine/suche').flush(null, { status: 400, statusText: 'Bad Request' });
    tick(2000);
    http.expectNone('/termine/suche');
    expect(letzter().status).toBe('fehler');
  }));

  it('meldet den Ladezustand vor der Antwort', fakeAsync(() => {
    anfragen.next(suche('x'));
    tick(300);
    expect(letzter().status).toBe('laedt');
    http.expectOne('/termine/suche').flush({ vorschlaege: [] });
    expect(letzter().status).toBe('fertig');
  }));
});
