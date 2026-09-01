import { expect, test } from './base'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'
import { kansalainen } from './setup/auth'

const opiskeluoikeudet = [
  'Perusopetus',
  'Lukion oppimäärä',
  'Autoalan perustutkinto'
]

test.describe('Omat tiedot – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: kansalainen('190751-739W') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test('Opiskeluoikeudet avattu', async ({ page, kansalainenPage }) => {
    await kansalainenPage.goto()

    for (const opiskeluoikeus of opiskeluoikeudet) {
      await kansalainenPage.openOpiskeluoikeus(opiskeluoikeus)
    }

    await expect(page.locator('.opiskeluoikeus-content')).toHaveCount(
      opiskeluoikeudet.length
    )
    await takeFullPageScreenshot(
      page,
      'omat-tiedot-opiskeluoikeudet-avattu.png'
    )
  })
})
