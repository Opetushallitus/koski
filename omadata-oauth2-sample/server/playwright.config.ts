import { defineConfig, devices } from '@playwright/test'

const baseURL = process.env.OMADATA_URL || 'http://localhost:7051'

export default defineConfig({
  testDir: '.',
  testMatch: ['test/e2e/**/*.spec.ts', '../java/test/e2e/**/*.spec.ts'],
  timeout: 30 * 1000,
  expect: {
    timeout: 5 * 1000
  },
  fullyParallel: true,
  forbidOnly: !!process.env.CI,
  retries: process.env.CI ? 2 : 0,
  workers: process.env.CI ? 1 : undefined,
  reporter: 'html',
  use: {
    baseURL,
    trace: 'on-first-retry'
  },
  projects: [
    {
      name: 'chromium',
      use: { ...devices['Desktop Chrome'] }
    }
  ]
})
