import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const hetuHakuPyyntö = (page: Page) =>
  page.waitForRequest(
    (r) => r.method() === 'POST' && r.url().endsWith('/koski/api/henkilo/hetu')
  )

test.describe('Dokumentaation API-testeri', () => {
  test.use({ storageState: virkailija('pää') })

  test('lähettää syötedatan editorista', async ({ page }) => {
    await page.goto('/koski/dokumentaatio/rajapinnat/opintohallintojarjestelmat')

    const operaatio = page.locator('.api-operation', {
      has: page.locator('h3', {
        hasText: /^POST\s*\/koski\/api\/henkilo\/hetu$/
      })
    })
    await operaatio.locator('h3').click()

    const syötedata = operaatio.locator('.postdata')
    await expect(syötedata).toContainText('"hetu"')

    const kokeile = operaatio.getByRole('button', { name: 'Kokeile' })
    const tulos = operaatio.locator('.result')

    let pyyntö = hetuHakuPyyntö(page)
    await kokeile.click()
    expect((await pyyntö).postDataJSON()).toEqual({ hetu: '010101-123N' })
    await expect(tulos).toContainText('200')
    await expect(tulos).toContainText('Esimerkki')

    await syötedata.getByText('"010101-123N"', { exact: true }).click()
    await page.keyboard.press('ControlOrMeta+A')
    await page.keyboard.insertText('{"hetu": "081165-793C"}')

    pyyntö = hetuHakuPyyntö(page)
    await kokeile.click()
    expect((await pyyntö).postDataJSON()).toEqual({ hetu: '081165-793C' })
    await expect(tulos).toContainText('Çelik-Eerola')
  })
})
