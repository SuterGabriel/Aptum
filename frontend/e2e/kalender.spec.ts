import AxeBuilder from '@axe-core/playwright';
import { expect, test } from '@playwright/test';
import { MONTAG, terminBuchen, verordnungAnlegen } from './api';

/**
 * Das Grid im echten Browser: Der über die API gebuchte Termin steht in der
 * Zelle, die Tastatur bewegt den Fokus, axe findet nichts. Das ist die
 * Merge-Bedingung aus dem Skill a11y-grid - als Lauf, nicht als Satz.
 */
test.describe('Kalender-Grid', () => {
  let gebucht: { therapeut: string; beginn: string };

  test.beforeAll(async ({ request }) => {
    gebucht = await terminBuchen(request, await verordnungAnlegen(request));
  });

  test('zeigt die Buchung mit Rüstzeit in der Spalte der Therapeutin', async ({ page }) => {
    const tag = gebucht.beginn.slice(0, 10);
    await page.goto(`/kalender?tag=${tag}`);
    await expect(page.getByRole('grid')).toBeVisible();

    const uhrzeit = gebucht.beginn.slice(11, 16);
    const belegt = page.getByRole('gridcell', {
      name: new RegExp(`${uhrzeit}, ${gebucht.therapeut}, belegt, Krankengymnastik`),
    });
    await expect(belegt).toHaveCount(1);
    await expect(belegt).toContainText('Krankengymnastik');
    await expect(
      page.getByRole('gridcell', { name: /Rüstzeit, Vorbereitung/ }).first(),
    ).toBeAttached();
  });

  test('lässt sich mit der Tastatur bedienen: eine tabbare Zelle, Pfeile bewegen', async ({
    page,
  }) => {
    await page.goto(`/kalender?tag=${MONTAG}`);
    await expect(page.getByRole('grid')).toBeVisible();

    await expect(page.locator('[role="gridcell"][tabindex="0"]')).toHaveCount(1);
    await page.locator('[role="gridcell"][tabindex="0"]').focus();
    await expect(page.locator(':focus')).toHaveAttribute('aria-label', /Montag, 07:00, /);

    await page.keyboard.press('ArrowDown');
    await expect(page.locator(':focus')).toHaveAttribute('aria-label', /Montag, 07:15, /);
    await page.keyboard.press('ArrowRight');
    await expect(page.locator(':focus')).toHaveAttribute('aria-label', /07:15, T\. Beta/);
    await page.keyboard.press('Control+End');
    await expect(page.locator(':focus')).toHaveAttribute('aria-label', /18:45, T\. Beta/);
    await expect(page.locator('[role="gridcell"][tabindex="0"]')).toHaveCount(1);

    // Eine gesperrte Zelle ist nicht wählbar; eine freie landet in der Statuszeile.
    await page.keyboard.press('Enter');
    await expect(page.locator('.status')).toHaveText('');

    // Die erste freie Zelle der Spalte suchen statt sie zu kennen: Nach vielen
    // Läufen gegen dieselbe Datenbank ist 08:00 nicht mehr frei.
    await page.keyboard.press('Control+Home');
    // Nach jedem Pfeildruck warten, bis der Fokus wirklich gewandert ist: Er
    // bewegt sich erst nach Angulars Render-Zyklus, und ein Lesen davor liefert
    // die alte Zelle. Auf dem CI-Runner war das eine Zelle Versatz - der Name
    // sagte 08:00, der Fokus stand auf 08:15.
    let name = '';
    for (let i = 0; i < 48; i++) {
      name = (await page.locator(':focus').getAttribute('aria-label')) ?? '';
      if (name.endsWith(', frei')) break;
      await page.keyboard.press('ArrowDown');
      await expect(page.locator(':focus')).not.toHaveAttribute('aria-label', name);
    }
    expect(name).toMatch(/, T\. Alpha, frei$/);
    await page.keyboard.press('Enter');
    await expect(page.locator('.status')).toContainText(`Gewählt: ${name}`);
  });

  test('Tagesreiter: Pfeile wechseln den Tag', async ({ page }) => {
    await page.goto(`/kalender?tag=${MONTAG}`);
    await page.getByRole('tab', { selected: true }).focus();
    await page.keyboard.press('ArrowRight');
    await expect(page.getByRole('tab', { selected: true })).toContainText('Di');
    await expect(page.getByRole('grid')).toHaveAttribute('aria-label', /^Dienstag/);
  });

  test('hat keine axe-Verstöße', async ({ page }) => {
    await page.goto(`/kalender?tag=${MONTAG}`);
    await expect(page.getByRole('grid')).toBeVisible();
    const ergebnis = await new AxeBuilder({ page }).analyze();
    expect(ergebnis.violations).toEqual([]);
  });
});
