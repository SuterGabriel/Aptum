import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { verordnungAnlegen } from './api';

test.describe('Terminsuche', () => {
  test('findet Vorschläge für eine echte Verordnung und meldet sie', async ({ page, request }) => {
    const verordnung = await verordnungAnlegen(request);
    await page.goto('/suche');

    await page.getByLabel('Von').fill('2026-03-02');
    await page.getByLabel('Bis').fill('2026-03-06');
    await page.getByLabel('Kennung der Verordnung').fill(verordnung);

    await expect(page.locator('.vorschlag').first()).toBeVisible();
    await expect(page.locator('[aria-live="polite"]')).toContainText(/\d+ Vorschläge gefunden/);
    await expect(page.locator('.vorschlag').first()).toContainText(/T\. (Alpha|Beta)/);

    const ergebnis = await new AxeBuilder({ page }).analyze();
    expect(ergebnis.violations).toEqual([]);
  });

  test('meldet eine unbekannte Verordnung als Alert', async ({ page }) => {
    await page.goto('/suche');
    await page.getByLabel('Kennung der Verordnung').fill('00000000-0000-4000-8000-000000000000');
    await expect(page.getByRole('alert')).toBeVisible();
  });
});
