import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { terminBuchen, verordnungAnlegen } from './api';

/**
 * Die Abrechnung im echten Browser: Eine über die API gebuchte Behandlung
 * steht als erbracht und prüffest in der Tabelle, die Tabelle lässt sich
 * sortieren und mit der Tastatur begehen, axe findet nichts. ag-grid bringt
 * das Verhalten mit - geprüft wird trotzdem, weil eine Bibliothek, die man
 * nicht misst, eine Behauptung ist.
 */
test.describe('Abrechnungsübersicht', () => {
  let verordnung: string;

  test.beforeAll(async ({ request }) => {
    verordnung = await verordnungAnlegen(request);
    await terminBuchen(request, verordnung);
    // Eine zweite, ohne Termin: Der Tastaturtest braucht zwei Zeilen, und in
    // der frischen CI-Datenbank läuft diese Datei als erste - dort gäbe es
    // sonst nur eine. Lokal fiel das nicht auf, weil die Tabelle voll war.
    await verordnungAnlegen(request);
  });

  test('zeigt die gebuchte Behandlung als erbracht und prüffest, mit Regelname in der Zeile', async ({
    page,
  }) => {
    await page.goto('/abrechnung');
    await expect(page.getByRole('grid')).toBeVisible();
    await expect(page.locator('.summe')).toContainText(/\d+ Verordnungen/);

    // Erst filtern, dann lesen: Nach vielen Läufen hat die Tabelle Hunderte Zeilen.
    await page.getByLabel('Tabelle filtern').fill(verordnung.slice(0, 8));
    const zeile = page.locator('[role="row"][row-index]');
    await expect(zeile).toHaveCount(1);
    await expect(zeile.locator('[col-id="erbracht"]')).toHaveText('1');
    await expect(zeile.locator('[col-id="offen"]')).toHaveText('5');
    await expect(zeile.locator('[col-id="status"]')).toHaveText('prüffest');
    await expect(zeile.locator('[col-id="begruendung"]')).toContainText('keine verletzt');

    const ergebnis = await new AxeBuilder({ page }).analyze();
    expect(ergebnis.violations).toEqual([]);
  });

  test('sortiert per Kopfzeile und bewegt den Fokus mit den Pfeiltasten durch die Zellen', async ({
    page,
  }) => {
    await page.goto('/abrechnung');
    await expect(page.getByRole('grid')).toBeVisible();

    // Zweimal klicken: aufsteigend, dann absteigend - die meisten offenen Einheiten zuerst.
    const kopf = page.getByRole('columnheader', { name: /^Offen/ });
    await kopf.click();
    await kopf.click();
    await expect(kopf).toHaveAttribute('aria-sort', 'descending');

    const erste = page.locator('[role="row"][row-index]').first();
    await erste.locator('[col-id="verordnung"]').click();
    await expect(page.locator(':focus')).toHaveAttribute('col-id', 'verordnung');
    await page.keyboard.press('ArrowRight');
    await expect(page.locator(':focus')).toHaveAttribute('col-id', 'ausstellungsdatum');
    await page.keyboard.press('ArrowDown');
    // row-index steht an der Zeile, nicht an der Zelle: die Zelle in Zeile 1 hat den Fokus.
    await expect(
      page.locator('[role="row"][row-index="1"] [col-id="ausstellungsdatum"]'),
    ).toBeFocused();
  });
});
