import { expect, test } from './base'
import { type Page } from '@playwright/test'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1122-1225 "Liitetiedot" v2-editorille.
 * Vuosiluokan suorituksen liitetiedot (todistuksen liitetieto + kuvaus)
 * voidaan lisätä, muokata ja poistaa muokkaustilassa.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`
const liitetietoKuvausTestId = 'oo.0.suoritukset.3.liitetiedot.0.kuvaus.input'

const fillLiitetietoKuvaus = async (page: Page, value: string) => {
  const kuvausInput = page.getByTestId(liitetietoKuvausTestId)
  await expect(kuvausInput).toBeVisible()
  await expect(async () => {
    await kuvausInput.fill(value)
    await expect(kuvausInput).toHaveValue(value, { timeout: 1000 })
  }).toPass({ timeout: 10000 })
}

test.describe('Perusopetuksen uusi käyttöliittymä: liitetiedot', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Liitetiedon lisäys, kuvauksen täyttö ja tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // 7. vuosiluokka on tab 3
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Lisää liitetieto
    const lisaaBtn = page.getByTestId('oo.0.suoritukset.3.liitetiedot.lisaa')
    await expect(lisaaBtn).toBeVisible()
    await lisaaBtn.click()

    // Syötä kuvaus
    await fillLiitetietoKuvaus(page, 'TestiTesti')

    // Tallenna
    const saveButton = page.getByTestId('oo.0.opiskeluoikeus.save')
    await expect(saveButton).toBeEnabled()
    await saveButton.click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Liitetieto näkyy view-modessa
    await expect(
      page.getByTestId('oo.0.suoritukset.3.liitetiedot.0.kuvaus')
    ).toContainText('TestiTesti')
  })

  test('Liitetiedon poisto tyhjentää listan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(90000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Lisää ja tallenna
    await page.getByTestId('oo.0.suoritukset.3.liitetiedot.lisaa').click()
    await fillLiitetietoKuvaus(page, 'Poistettava')
    const saveButton = page.getByTestId('oo.0.opiskeluoikeus.save')
    await expect(saveButton).toBeEnabled()
    await saveButton.click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Poista (Removable-wrapperin roskakori-ikoni: testId=".delete")
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritukset.3.liitetiedot.0.delete').click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Liitetiedot-rivi ei enää näy (lista tyhjentyi)
    await expect(
      page.getByTestId('oo.0.suoritukset.3.liitetiedot.0.kuvaus')
    ).not.toBeVisible()
  })
})
