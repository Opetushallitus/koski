import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Lukiokoulutus 2019: muu suoritus (Lukiodiplomit/Muut suoritukset/Teemaopinnot)', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
  })

  test('Kurssin poistaminen muu-suoritus-rivin alta päivittää koulutusmoduulin laajuuden', async ({
    page
  }) => {
    const msRow = page
      .locator('table.LukionOppiaineet__taulukko tbody tr')
      .filter({ hasText: 'Muut suoritukset' })
    await expect(msRow).toHaveCount(1)
    await expect(msRow.locator('[data-testid$="laajuus.arvioitu"]')).toHaveText('7')

    const hai765 = msRow.locator('li.kurssi').filter({ hasText: 'HAI765' })
    await expect(hai765).toHaveCount(1)
    await hai765.locator('[data-testid$="poisto"]').click()
    await expect(hai765).toHaveCount(0)

    await expect(msRow.locator('[data-testid$="laajuus.arvioitu"]')).toHaveText('6')
    await expect(
      msRow.locator('[data-testid$="laajuus.hyvaksytystiArvioitu"]')
    ).toHaveText('6')

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    const msRowAfter = page
      .locator('table.LukionOppiaineet__taulukko tbody tr')
      .filter({ hasText: 'Muut suoritukset' })
    await expect(msRowAfter.locator('[data-testid$="laajuus.arvioitu"]')).toHaveText(
      '6'
    )
  })

  test('Lisää muu suoritus -valikko tarjoaa vain skeeman tukemat koodit (LD/TO/MS)', async ({
    page
  }) => {
    const toRow = page
      .locator('table.LukionOppiaineet__taulukko tbody tr')
      .filter({ hasText: 'Teemaopinnot' })
    await toRow.locator('> td.LukionOppiaineet__poisto [data-testid$="poisto"]').click()

    const select = page.locator('[data-testid$="uusi-muu-suoritus.toggle"]')
    await select.click()
    const allLabels = await page.locator('.Select__optionLabel').allTextContents()
    expect(allLabels).toEqual(['Teemaopinnot'])
  })
})
