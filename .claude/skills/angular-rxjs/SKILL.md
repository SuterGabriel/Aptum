---
name: angular-rxjs
description: Hausstil für Angular und RxJS in diesem Projekt - switchMap bei Suchanfragen, inneres catchError, kein manuelles subscribe in Komponenten, Signals nur für lokalen State, OnPush durchgehend. Nutze diesen Skill bei jeder Änderung unter frontend/, beim Anlegen von Komponenten oder Services und bei allen Fragen zu Observables, Streams, Change Detection oder State.
---

# Angular und RxJS

## Der kanonische Suchflow

Das zentrale Frontend-Artefakt dieses Projekts. Referenz:
`referenzen/slot-suche.ts`. Jede neue Suche folgt diesem Muster.

```
Filterformular als Stream
  -> combineLatest über die Filterdimensionen
  -> debounceTime(300) und distinctUntilChanged
  -> switchMap gegen die Slot-API        verhindert Race Conditions
  -> inneres catchError                  ein Fehler bricht den Stream nicht ab
  -> retry mit Backoff
  -> Ergebnis als Signal für die Komponente
```

## Die Regeln, jeweils mit Begründung

**switchMap bei Suchanfragen, nicht mergeMap.** Tippt jemand schnell, sind
mehrere Anfragen gleichzeitig unterwegs. Mit mergeMap laufen alle weiter, und
die langsamste Antwort gewinnt und überschreibt das korrekte Ergebnis.
switchMap bricht die vorherige ab. Bei Schreibvorgängen ist switchMap dagegen
falsch, dort gehört concatMap oder exhaustMap hin.

**catchError gehört nach innen.** Steht es am Ende der äußeren Kette, beendet
der erste Fehler den Stream, und die Suche ist bis zum Neuladen der Seite tot.

```ts
switchMap(filter => this.api.slots(filter).pipe(
  catchError(f => { this.fehler.set(f); return of([]); })   // innen
))
```

**Kein subscribe in Komponenten.** Ein manuelles subscribe ist ein Speicherleck,
das niemand sieht. Stattdessen toSignal oder die async-Pipe. Wenn ein subscribe
unvermeidlich ist, dann mit takeUntilDestroyed.

**Signals für lokalen State, RxJS für Ereignisse über die Zeit.** Ein
aufgeklappter Dialog ist ein Signal. Eine Suche mit Entprellung, Abbruch und
Wiederholung ist ein Stream. Die Grenze verläuft dort, wo Zeit eine Rolle
spielt.

**OnPush durchgehend.** Jede Komponente, ohne Ausnahme, damit die Ausnahme nicht
zur Regel wird.

**Standalone-Komponenten.** Keine NgModules in neuem Code.

## Negativbeispiel: die verschachtelte Subscription

```ts
// FALSCH
this.filter$.subscribe(f => {
  this.api.slots(f).subscribe(s => this.slots = s);
});
```

Zwei Lecks, keine Abbruchlogik, eine Race Condition eingebaut, und unter OnPush
läuft die Change Detection nicht an. Das ist genau der Code, nach dem im
Interview gefragt wird, und der Grund, warum die richtige Variante eine
Bildschirmseite wert ist.
