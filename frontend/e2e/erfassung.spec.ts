import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';

/**
 * Die ganze Kette in einem Test: Freitext, Vorschlag des Modells, ein Mensch
 * ergänzt, und erst dann prüft die Domäne. Der AI-Dienst läuft dabei mit
 * aufgezeichneten Antworten - kein Schlüssel, kein Netz, kein Zufall. Was
 * geprüft wird, ist die Kette, nicht das Modell.
 */
test.describe('Verordnung erfassen', () => {
  test('Freitext mit Lücke: das Modell schweigt, der Mensch ergänzt, die Domäne prüft', async ({
    page,
  }) => {
    await page.goto('/erfassung');

    await page
      .getByLabel('Text der Verordnung')
      .fill('Patient: Testfall Alpha, geb. 01.01.1900. MLD 6x, 2x/Woche, M54.5, 27.02.2026');
    await page.getByRole('button', { name: 'Vorschlag holen' }).click();

    // Der Dienst sagt, wie viel er ersetzt hat, bevor er gefragt hat.
    await expect(page.locator('.herkunft')).toContainText('2');
    await expect(page.locator('.herkunft')).toContainText('ersetzt');
    await expect(page.locator('.modellhinweis').first()).toContainText('MLD ohne Dauer');
    await expect(page.locator('.modellhinweis').last()).toContainText('ICD-Code');

    await page.getByRole('button', { name: /übernehmen/ }).click();

    // Das Heilmittel war nicht lesbar - und bleibt leer statt geraten.
    await expect(page.locator('.abseits')).toContainText('nicht gelesen');
    await expect(page.getByLabel('Verordnete Einheiten')).toHaveValue('6');

    // Anlegen ist gesperrt, solange die Lücken offen sind.
    const anlegen = page.getByRole('button', { name: 'Verordnung anlegen' });
    await expect(anlegen).toBeDisabled();

    await page.getByLabel('Diagnosegruppe').selectOption('WS');
    await expect(anlegen).toBeEnabled();

    await anlegen.click();
    await expect(page.locator('.angelegt')).toBeVisible();
    const kennung = await page.locator('.kennung').textContent();
    expect(kennung).toMatch(/^[0-9a-f-]{36}$/);

    const axeErgebnis = await new AxeBuilder({ page }).analyze();
    expect(axeErgebnis.violations).toEqual([]);

    // Und weiter zur Suche mit genau dieser Verordnung.
    await page.getByRole('button', { name: /Termine dafür suchen/ }).click();
    await expect(page).toHaveURL(/\/suche\?verordnung=/);
  });

  test('vollständiger Freitext: alles gelesen, nichts markiert', async ({ page }) => {
    await page.goto('/erfassung');
    await page
      .getByLabel('Text der Verordnung')
      .fill('KG 6x, 2x wöchentlich, Diagnosegruppe WS, ausgestellt am 27.02.2026');
    await page.getByRole('button', { name: 'Vorschlag holen' }).click();

    await expect(page.locator('[aria-live="polite"]')).toContainText('Alle Felder erkannt');
    await page.getByRole('button', { name: /übernehmen/ }).click();
    await expect(page.locator('.feld.unsicher')).toHaveCount(0);
    await expect(page.getByLabel('Diagnosegruppe')).toHaveValue('WS');
  });
});
