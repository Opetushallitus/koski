import { type Page, expect, test } from '@playwright/test'

const sampleBaseUrl = process.env.OMADATA_URL || 'http://localhost:7051'

const gotoSample = (path: string) => `${sampleBaseUrl}${path}`

const loginKorhopankki = async (page: Page, hetu: string) => {
  await page.waitForURL('**/koski/login/oppija/**')
  await page.getByTestId('hetu').fill(hetu)
  await expect(
    page.getByRole('button', { name: 'Kirjaudu sisään' })
  ).toBeEnabled()
  await page.getByRole('button', { name: 'Kirjaudu sisään' }).click()
}

test('OAuth2 data access succeeds', async ({ page }) => {
  await page.goto(gotoSample('/api/openid-api-test'))

  await loginKorhopankki(page, '280618-402H')

  await page.waitForURL('**/koski/omadata-oauth2/authorize**')
  await expect(page.getByText('Nimi')).toBeVisible()
  await expect(page.getByText('Henkilötunnus')).toBeVisible()
  await expect(page.getByText('Suoritetut tutkinnot')).toBeVisible()
  await expect(page.getByLabel('Suostumuksen voimassaoloaika')).toBeVisible()

  await page.getByRole('button', { name: 'Hyväksy' }).click()

  await page.waitForURL('**/api/openid-api-test/form-post-response-cb**')
  await expect(page.locator('html')).toContainText('280618-402H')
})

test('Declining authorization surfaces an access_denied error', async ({
  page
}) => {
  await page.goto(gotoSample('/api/openid-api-test'))

  await loginKorhopankki(page, '280618-402H')

  await page.waitForURL('**/koski/omadata-oauth2/authorize**')
  await expect(
    page.getByRole('button', { name: 'Peruuta ja palaa' })
  ).toBeVisible()
  await page.getByRole('button', { name: 'Peruuta ja palaa' }).click()

  await page.waitForURL('**/api/openid-api-test/form-post-response-cb**')
  await expect(page.locator('html')).toContainText('"error":"access_denied"')
})

test('Invalid redirect_uri shows client error page', async ({ page }) => {
  await page.goto(gotoSample('/api/openid-api-test/invalid-redirect-uri'))

  await expect(page.getByTestId('error')).toBeVisible()
  await expect(page.locator('#error')).toContainText('invalid_client_data')
  await expect(page.getByLabel('Tapahtui virhe:')).toContainText(
    'omadataoauth2-error-'
  )
  await expect(page.getByLabel('Tapahtui virhe:')).toContainText(
    'invalid_client_data'
  )
})
