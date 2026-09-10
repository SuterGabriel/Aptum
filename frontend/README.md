# Aptum Frontend

Angular 20 auf dem Angular CDK, ohne Komponenten-Set (ADR-006). Die
Gestaltungstoken liegen in `src/styles/tokens.css` und werden in der CI auf
Kontrast geprüft; das Kalender-Grid wird selbst gebaut (ADR-003).

```bash
npm install
npm start              # http://localhost:4200, leitet /termine und /verordnungen an :8080 weiter
npm test               # Unit-Tests, einmalig, headless
npm run lint
npm run api:types      # TypeScript-Typen aus ../docs/api/openapi.json erzeugen
npm run build
```

Die erste Seite ist die Terminsuche unter `/suche`. Sie braucht die Kennung einer
Verordnung — die gibt `POST /verordnungen` zurück, siehe „Starten“ im
Haupt-README. Der Mandant ist ein Platzhalter (`src/app/api/mandant.ts`).

Die API-Typen unter `src/app/api/schema.d.ts` sind erzeugt, nicht geschrieben.
Sie werden aus dem OpenAPI-Dokument abgeleitet, das der Backend-Test gegen die
laufende Anwendung prüft — ändert sich die Schnittstelle, ändern sich die Typen,
und die CI meldet, wenn die eingecheckte Fassung veraltet ist.
