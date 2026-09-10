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

    // Buchen: Der Dialog prüft die Regeln, bucht, schließt; die Seite meldet
    // und sucht neu, der Vorschlag ist weg. Fokus zurück auf dem Auslöser.
    const erster = page.locator('.vorschlag').first();
    // Person eingeschlossen: Nach der Buchung bei einer Person kann dieselbe
    // Uhrzeit bei der anderen der neue erste Vorschlag sein.
    const vorher = (await erster.textContent())?.replace(/\s+/g, ' ').trim() ?? '';
    await erster.getByRole('button', { name: /^Buchen/ }).click();

    const dialog = page.getByRole('dialog');
    await expect(dialog).toBeVisible();
    await expect(dialog.locator('.regel').first()).toBeVisible();
    await expect(dialog.locator('.regel.erfuellt').first()).toBeVisible();
    await expect(dialog.locator('.regel.verletzt')).toHaveCount(0);
    const imDialog = await new AxeBuilder({ page }).include('[role="dialog"]').analyze();
    expect(imDialog.violations).toEqual([]);

    await page.keyboard.press('Escape');
    await expect(dialog).toBeHidden();
    await expect(page.locator(':focus')).toHaveText(/Buchen/);

    await erster.getByRole('button', { name: /^Buchen/ }).click();
    await page.getByRole('dialog').getByRole('button', { name: 'Buchen' }).click();
    await expect(page.getByRole('dialog')).toBeHidden();
    await expect(page.getByRole('status')).toContainText('Gebucht:');
    // Die Seite sucht neu; der verbrauchte Vorschlag ist weg. Wartend, weil die
    // Suche nach der Meldung noch unterwegs sein kann.
    await expect(page.locator('.vorschlag').first()).not.toHaveText(vorher);
  });

  test('meldet eine unbekannte Verordnung als Alert', async ({ page }) => {
    await page.goto('/suche');
    await page.getByLabel('Kennung der Verordnung').fill('00000000-0000-4000-8000-000000000000');
    await expect(page.getByRole('alert')).toBeVisible();
  });
});
