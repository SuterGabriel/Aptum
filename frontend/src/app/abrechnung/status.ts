/**
 * Die drei Zustände aus der Domäne, für Menschen. Der Code bleibt der
 * Schlüssel für die Klasse. Eigene Datei, damit die Suche das Wort kennt,
 * ohne ag-grid zu laden.
 */
export const STATUS_LABEL: Record<string, string> = {
  PRUEFFEST: 'prüffest',
  BEANSTANDET: 'beanstandet',
  NICHT_BEGONNEN: 'nicht begonnen',
};
