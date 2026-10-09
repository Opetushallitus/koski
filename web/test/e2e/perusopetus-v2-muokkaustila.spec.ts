import { expect, test } from './base'
import { virkailija } from './setup/auth'

// Monia Useitan perusopetuksen välilehdellä aineopiskelijan opiskeluoikeus
// näkyy vanhalla käyttöliittymällä ja kaksi muuta uudella (oo.0 ja oo.1).
const moniaOid = '1.2.246.562.24.00000000165'
const moniaUrl = `${moniaOid}?opiskeluoikeudenTyyppi=perusopetus`

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

  test('Vanhan käyttöliittymän navigointi kysyy ennen kuin uuden käyttöliittymän tallentamattomat muutokset hylätään', async ({
    page
  }) => {
    const vanha = page
      .locator('[data-testid="opiskeluoikeuksientiedot"] > li')
      .filter({ has: page.locator('.versiohistoria') })
    const vanhanLisätiedot = vanha.locator('a', { hasText: 'Lisätiedot' })
    const osoite = () => decodeURIComponent(page.url())
    const lisätieto = page.locator(
      '[data-testid^="oo.0.suoritukset."][data-testid$="todistuksellaNäkyvätLisätiedot.edit.input"]'
    )

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await lisätieto.fill('Tallentamaton muutos')

    const kysymykset: string[] = []
    page.once('dialog', (dialog) => {
      kysymykset.push(dialog.message())
      return dialog.dismiss()
    })
    await vanhanLisätiedot.click()
    await expect.poll(() => kysymykset).toHaveLength(1)
    expect(kysymykset[0]).toContain('Tallentamattomat muutokset menetetään')
    expect(osoite()).not.toContain('lisätiedot-expanded')
    await expect(lisätieto).toHaveValue('Tallentamaton muutos')

    page.once('dialog', (dialog) => dialog.accept())
    await vanhanLisätiedot.click()
    await expect.poll(osoite).toContain('lisätiedot-expanded=true')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
    await expect(lisätieto).toHaveCount(0)
  })
})
