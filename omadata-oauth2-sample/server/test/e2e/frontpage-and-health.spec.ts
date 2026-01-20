import { expect, test } from '@playwright/test'

const sampleBaseUrl = process.env.OMADATA_URL || 'http://localhost:7051'

const gotoSample = (path: string) => `${sampleBaseUrl}${path}`

test('Front page renders', async ({ page }) => {
  await page.goto(gotoSample('/'))
  await expect(page.getByText('OmaDataOAuth2 Sample app')).toBeVisible()
})

test('Healthcheck returns ok', async ({ page }) => {
  await page.goto(gotoSample('/api/healthcheck'))
  await page.waitForURL('**/api/healthcheck**')
  await expect(page.getByText('Ok')).toBeVisible()
})
