import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Koodisto fetch error', () => {
  test.use({ storageState: virkailija('pää') })

  test.setTimeout(60000)

  test('error banneri näkyy uusi oppija dialogissa', async ({
    page
  }) => {
    //asetetaan koodiston haku failaamaan että banneria voidaan testata
    await page.route('**/koski/api/types/koodisto/**', (route) => {
      route.abort()
    })

    await page.goto('/koski/uusioppija#hetu=270181-517T')

    const oppilaitosInput = page.getByTestId(
      'uusiOpiskeluoikeus.modal.oppilaitos.input'
    )
    await oppilaitosInput.click()
    await oppilaitosInput.fill('Stadin')

    const stadinOption = page.getByTestId(
      'uusiOpiskeluoikeus.modal.oppilaitos.options.1.2.246.562.10.52251087186.item'
    )
    await stadinOption.waitFor({ state: 'visible', timeout: 10000 })
    await stadinOption.click()

    await page.getByTestId('uusiOpiskeluoikeus.modal.opiskeluoikeus.input').click()
    const ooOption = page.locator(
      '[data-testid*="uusiOpiskeluoikeus.modal.opiskeluoikeus.options"][data-testid$=".item"]'
    ).first()
    await ooOption.waitFor({ state: 'visible', timeout: 10000 })
    await ooOption.click()

    await expect(page.getByTestId('globalErrors')).toBeVisible({ timeout: 30000 })
    await expect(page.getByTestId('globalErrors')).toContainText(
      'Tietojen hakeminen epäonnistui'
    )
  })
})
