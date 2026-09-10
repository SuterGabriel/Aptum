/**
 * Die Heilmittel, die das Backend kennt, mit Bezeichnung für die Auswahl.
 *
 * Die Codes sind die Konstanten des Domänen-Enums. Die Schnittstelle trägt
 * sie als String; diese Liste ist die Übersetzung für Menschen.
 */
export const HEILMITTEL: readonly { code: string; bezeichnung: string }[] = [
  { code: 'KG_EINZEL', bezeichnung: 'Krankengymnastik' },
  { code: 'KG_GRUPPE', bezeichnung: 'Krankengymnastik in der Gruppe' },
  { code: 'MANUELLE_THERAPIE', bezeichnung: 'Manuelle Therapie' },
  { code: 'KLASSISCHE_MASSAGE', bezeichnung: 'Klassische Massagetherapie' },
  { code: 'MLD_TEILBEHANDLUNG', bezeichnung: 'Manuelle Lymphdrainage, Teilbehandlung' },
  { code: 'MLD_GROSSBEHANDLUNG', bezeichnung: 'Manuelle Lymphdrainage, Großbehandlung' },
  { code: 'MLD_GANZBEHANDLUNG', bezeichnung: 'Manuelle Lymphdrainage, Ganzbehandlung' },
  { code: 'KG_ZNS_ERWACHSENE', bezeichnung: 'KG-ZNS, Erwachsene' },
  { code: 'KG_ZNS_KINDER', bezeichnung: 'KG-ZNS, Kinder' },
  { code: 'KG_GERAET', bezeichnung: 'Krankengymnastik am Gerät' },
  { code: 'KG_BEWEGUNGSBAD', bezeichnung: 'Krankengymnastik im Bewegungsbad' },
  { code: 'WARMPACKUNG', bezeichnung: 'Warmpackung' },
  { code: 'ERGO_MOTORISCH_FUNKTIONELL', bezeichnung: 'Motorisch-funktionelle Behandlung' },
  { code: 'ERGO_SENSOMOTORISCH', bezeichnung: 'Sensomotorisch-perzeptive Behandlung' },
  { code: 'ERGO_HIRNLEISTUNGSTRAINING', bezeichnung: 'Hirnleistungstraining' },
  { code: 'ERGO_PSYCHISCH_FUNKTIONELL', bezeichnung: 'Psychisch-funktionelle Behandlung' },
  { code: 'ERGO_BERATUNG_UMFELD', bezeichnung: 'Beratung des Umfelds' },
];
