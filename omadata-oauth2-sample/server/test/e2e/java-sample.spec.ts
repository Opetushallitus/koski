import { expect, test } from '@playwright/test'

const javaBaseUrl = process.env.JAVA_SAMPLE_URL || 'http://localhost:7052'

const gotoJava = (path: string) => `${javaBaseUrl}${path}`

test('Java sample front page renders', async ({ page }) => {
  await page.goto(gotoJava('/'))
  await expect(
    page.getByText('You have NOT authorized use of your data.')
  ).toBeVisible()
})

test('Java sample OAuth2 flow works', async ({ page }) => {
  await page.goto(gotoJava('/oauth2/authorization/koski'))

  await page.waitForURL('**/koski/login/oppija/**')
  await page.getByTestId('hetu').fill('280618-402H')
  await expect(
    page.getByRole('button', { name: 'Kirjaudu sisään' })
  ).toBeEnabled()
  await page.getByRole('button', { name: 'Kirjaudu sisään' }).click()

  await page.waitForURL('**/koski/omadata-oauth2/authorize**')
  await expect(page.getByText('Henkilötunnus')).toBeVisible()
  await expect(page.getByLabel('Suostumuksen voimassaoloaika')).toBeVisible()
  await page.getByRole('button', { name: 'Hyväksy' }).click()

  await page.waitForURL('**/api/openid-api-test/form-post-response-cb**')
  await expect(page.locator('html')).toContainText('280618-402H')
})
