import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit perusopetuksen opiskeluoikeuden mitätöinnille.
 *
 * Kalle-virkailijalla on oikeudet mitätöidä opiskeluoikeuksia. Testi tarkistaa
 * mitätöinti-painikkeen näkymisen ja peruutuksen, muttei suorita varsinaista
 * mitätöintiä (joka rikkoisi muiden testien fixturen).
 *
 * Oppilaitoksen pääkäyttäjä (stadin-pää) voi mitätöidä ilman muokkausoikeutta,
 * joten painike näkyy hänelle katselutilassa.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus`

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

test.describe('Perusopetuksen uusi käyttöliittymä: mitätöinti oppilaitoksen pääkäyttäjänä', () => {
  test.use({ storageState: virkailija('stadin-pää') })

  test('Mitätöi-painike näkyy katselutilassa ilman muokkausoikeutta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    // Miia Monikoululainen: stadin-pää näkee vain Kulosaaren ala-asteen
    // (Helsingin kaupunki) opiskeluoikeuden.
    await oppijaPage.goto(
      '1.2.246.562.24.00000000012?opiskeluoikeudenTyyppi=perusopetus'
    )

    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Kulosaaren ala-aste'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)

    const mitätöiBtn = page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')
    await expect(mitätöiBtn).toBeVisible()
    await mitätöiBtn.click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.invalidate.confirm')
    ).toBeVisible()
    await page.getByTestId('oo.0.opiskeluoikeus.invalidate.cancel').click()
    await expect(mitätöiBtn).toBeVisible()
  })
})
