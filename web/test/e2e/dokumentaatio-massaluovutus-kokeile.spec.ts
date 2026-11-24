import { Locator, Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const massaluovutusApi = '/koski/api/massaluovutus'

const clickAndGetQueryRequestBody = async (page: Page, button: Locator) => {
  const request = page.waitForRequest(
    (r) => r.method() === 'POST' && r.url().endsWith(massaluovutusApi)
  )
  await button.click()
  return (await request).postDataJSON()
}

test.describe('Massaluovutus-dokumentaation Kokeile-lomake', () => {
  test.use({ storageState: virkailija('pää') })

  test('lähettää esimerkkikyselyn ja pollaa tulosta valmiiksi', async ({
    page
  }) => {
    // Ankkuri avaa osion avattavat lohkot, myös Kokeile-lomakkeen
    await page.goto(
      '/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat#aiemmatOpiskeluoikeudet'
    )

    const section = page.locator('section.query-doc', {
      has: page.locator('#aiemmatOpiskeluoikeudet')
    })
    const kokeile = section.locator('details', {
      has: page.locator('summary', { hasText: 'Kokeile' })
    })
    const queryJson = kokeile.getByRole('textbox')
    await expect(queryJson).toBeVisible()

    const example = JSON.parse(await queryJson.inputValue())
    expect(example.type).toEqual('aiemmatOpiskeluoikeudet')

    const sendButton = kokeile.getByRole('button', { name: 'Lähetä kysely' })
    const result = kokeile.locator('.result')

    expect(await clickAndGetQueryRequestBody(page, sendButton)).toEqual(example)
    await expect(result).toContainText('200')
    await expect(result).toContainText('kyselyn tila: complete', {
      timeout: 20000
    })
    await expect(result.locator('.files a').first()).toBeVisible()
    await expect(result.locator('pre')).toContainText('"status": "complete"')
  })

  test('näyttää virheet ja palauttaa esimerkin', async ({ page }) => {
    await page.goto(
      '/koski/dokumentaatio/rajapinnat/massaluovutus/koulutuksenjarjestajat#aiemmatOpiskeluoikeudet'
    )

    const section = page.locator('section.query-doc', {
      has: page.locator('#aiemmatOpiskeluoikeudet')
    })
    const kokeile = section.locator('details', {
      has: page.locator('summary', { hasText: 'Kokeile' })
    })
    const queryJson = kokeile.getByRole('textbox')
    const sendButton = kokeile.getByRole('button', { name: 'Lähetä kysely' })
    const result = kokeile.locator('.result')

    const exampleJson = await queryJson.inputValue()
    const example = JSON.parse(exampleJson)

    // Rikkinäinen JSON pysäytetään selaimessa ilman pyyntöä palvelimelle
    await queryJson.fill('{ "type": ')
    await sendButton.click()
    await expect(result).toContainText('Virheellinen JSON')

    // Palvelimen hylkäämä kysely näytetään virhekoodilla
    const withoutOppijat = { ...example }
    delete withoutOppijat.hetut
    delete withoutOppijat.oppijaOidit
    await queryJson.fill(JSON.stringify(withoutOppijat))
    expect(await clickAndGetQueryRequestBody(page, sendButton)).toEqual(
      withoutOppijat
    )
    await expect(result).toContainText('400')

    await kokeile.getByRole('button', { name: 'Palauta esimerkki' }).click()
    await expect(queryJson).toHaveValue(exampleJson)
    await expect(result).toHaveCount(0)
  })
})
