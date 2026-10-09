import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

// Monia Useitan perusopetuksen välilehdellä aineopiskelijan opiskeluoikeus
// näkyy vanhalla käyttöliittymällä ja kaksi muuta uudella (oo.0 ja oo.1).
const moniaOid = '1.2.246.562.24.00000000165'
const moniaUrl = `${moniaOid}?opiskeluoikeudenTyyppi=perusopetus`

const vanhaOpiskeluoikeus = (page: Page) =>
  page
    .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
    .filter({ has: page.locator('.versiohistoria') })
const lisätieto = (page: Page) =>
  page.locator(
    '[data-testid^="oo.0.suoritukset."][data-testid$="todistuksellaNäkyvätLisätiedot.edit.input"]'
  )
const osoite = (page: Page) => decodeURIComponent(page.url())

test.describe('Vanhan ja uuden käyttöliittymän muokkaustila samalla sivulla', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures, oppijaPage }) => {
    await fixtures.reset()
    await oppijaPage.goto(moniaUrl)
  })

  test('Vain yksi opiskeluoikeus on kerrallaan muokkaustilassa', async ({
    page
  }) => {
    const vanhanMuokkaa = page.locator('.toggle-edit')
    const lisääOpiskeluoikeus = page.locator('.add-opiskeluoikeus')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toHaveCount(0)
    await expect(vanhanMuokkaa).toHaveCount(0)
    await expect(lisääOpiskeluoikeus).toHaveClass(/disabled/)

    await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toBeVisible()
    await expect(vanhanMuokkaa).toBeVisible()
    await expect(lisääOpiskeluoikeus).not.toHaveClass(/disabled/)

    await vanhanMuokkaa.click()
    await expect(page).toHaveURL(/edit=/)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
    await expect(page.getByTestId('oo.1.opiskeluoikeus.edit')).toHaveCount(0)
  })

  test('Uuden käyttöliittymän tallentamattomat muutokset säilyvät vanhan käyttöliittymän navigoinnissa saman välilehden sisällä', async ({
    page
  }) => {
    const kysymykset: string[] = []
    page.on('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')
    await vanhaOpiskeluoikeus(page).locator('a', { hasText: 'Lisätiedot' }).click()

    await expect.poll(() => osoite(page)).toContain('lisätiedot-expanded=true')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')
    expect(kysymykset).toHaveLength(0)
  })

  test('Välilehden vaihto kysyy ennen kuin uuden käyttöliittymän tallentamattomat muutokset hylätään', async ({
    page
  }) => {
    const välilehti = page
      .getByTestId('opiskeluoikeustyyppi-esiopetus')
      .locator('a')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')

    const kysymykset: string[] = []
    page.once('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await välilehti.click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(kysymykset[0]).toContain('Tallentamattomat muutokset menetetään')
    expect(osoite(page)).toContain('opiskeluoikeudenTyyppi=perusopetus')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')

    page.once('dialog', (dialog) => dialog.accept())
    await välilehti.click()
    await expect.poll(() => osoite(page)).toContain('opiskeluoikeudenTyyppi=esiopetus')
    await expect(lisätieto(page)).toHaveCount(0)
  })

  test('Vanhan käyttöliittymän version sulkeminen kysyy ennen kuin uuden käyttöliittymän tallentamattomat muutokset hylätään', async ({
    page
  }) => {
    const vanha = vanhaOpiskeluoikeus(page)
    await vanha.locator('.versiohistoria > a').click()
    await vanha.locator('.versiohistoria .modal a').filter({ hasText: 'v1' }).click()
    await expect.poll(() => osoite(page)).toContain('versionumero=1')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto(page).fill('Tallentamaton muutos')

    const kysymykset: string[] = []
    page.once('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await vanha.locator('.versiohistoria > a').click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(osoite(page)).toContain('versionumero=1')
    await expect(lisätieto(page)).toHaveValue('Tallentamaton muutos')
  })
})
