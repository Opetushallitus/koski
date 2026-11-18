import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Lukiokoulutus 2019: moduulin osaamisen tunnustaminen', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .locator(
        '[data-testid^="oo.0.opiskeluoikeus.tila.edit.items."][data-testid$=".remove"]'
      )
      .click()
  })

  test('Osaamisen tunnustaminen voidaan lisätä kurssille', async ({
    page
  }) => {
    const ai = page.locator('[data-koodiarvo="AI"]')
    const ai1 = ai.locator('li.kurssi').filter({
      has: page.locator('.tunniste', { hasText: /^ÄI1$/ })
    })

    await ai1.locator('.tunniste').click()
    const modaali = page.locator('[role="dialog"]')
    await modaali.locator('[data-testid$=".tunnustettu.edit.add"]').click()
    await modaali
      .locator('[data-testid$=".tunnustettu.edit.selite.input"]')
      .fill('Tunnustettu aiemmasta opinnosta')
    await modaali
      .locator('[data-testid$=".tunnustettu.edit.bool.label"]')
      .click()
    await modaali.getByRole('button', { name: 'Sulje' }).click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    const ai1Again = page
      .locator('[data-koodiarvo="AI"]')
      .locator('li.kurssi')
      .filter({ has: page.locator('.tunniste', { hasText: /^ÄI1$/ }) })
    await expect(
      ai1Again.locator('[data-testid$=".tunnustettu.selite"]')
    ).toHaveText('Tunnustettu aiemmasta opinnosta')
    await expect(
      ai1Again.locator('[data-testid$=".tunnustettu.rahoituksenPiirissä"]')
    ).toHaveText('Kyllä')
  })
})
