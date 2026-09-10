import { defineConfig, devices } from '@playwright/test';

/**
 * Ende-zu-Ende gegen das echte Backend.
 *
 * Der Dev-Server leitet /termine, /verordnungen und /kalender an :8080
 * weiter; das Backend muss laufen (README, „Starten"). In der CI startet
 * der Job e2e beides. Die Tests bauen ihre Daten selbst über die API auf.
 */
export default defineConfig({
  testDir: 'e2e',
  fullyParallel: false,
  forbidOnly: !!process.env['CI'],
  retries: 0,
  reporter: process.env['CI'] ? [['list'], ['html', { open: 'never' }]] : 'list',
  use: {
    baseURL: 'http://localhost:4200',
    trace: 'retain-on-failure',
  },
  projects: [{ name: 'chromium', use: { ...devices['Desktop Chrome'] } }],
  webServer: {
    command: 'npm start',
    url: 'http://localhost:4200',
    reuseExistingServer: !process.env['CI'],
    timeout: 120_000,
  },
});
