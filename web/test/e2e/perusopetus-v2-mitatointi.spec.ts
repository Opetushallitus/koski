import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit perusopetuksen opiskeluoikeuden mitätöinnille.
 *
 * Kalle-virkailijalla on oikeudet mitätöidä opiskeluoikeuksia. Testi tarkistaa
 * mitätöinti-painikkeen näkymisen ja peruutuksen, muttei suorita varsinaista
 * mitätöintiä (joka rikkoisi muiden testien fixturen).
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: opiskeluoikeuden mitätöinti', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Mitätöi-painike avaa vahvistusnäkymän, Peruuta palauttaa alkutilaan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Mitätöi-painike näkyy vasta muokkaustilassa, koska Kallella ei ole
    // hasAnyInvalidateAccess-oikeutta (vaan vain tallennusoikeus).
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const mitätöiBtn = page.getByTestId(
      'oo.0.opiskeluoikeus.invalidate.button'
    )
    await expect(mitätöiBtn).toBeVisible()
    await expect(mitätöiBtn).toContainText('Mitätöi')

    // Klikkaa Mitätöi → vahvistus- ja peruuta-painikkeet näkyvät
    await mitätöiBtn.click()

    const confirmBtn = page.getByTestId(
      'oo.0.opiskeluoikeus.invalidate.confirm'
    )
    const cancelBtn = page.getByTestId(
      'oo.0.opiskeluoikeus.invalidate.cancel'
    )
    await expect(confirmBtn).toBeVisible()
    await expect(confirmBtn).toContainText('Vahvista mitätöinti')
    await expect(cancelBtn).toBeVisible()

    // Klikkaa Peruuta → palaa alkutilaan
    await cancelBtn.click()
    await expect(mitätöiBtn).toBeVisible()
    await expect(confirmBtn).not.toBeVisible()
  })
})
