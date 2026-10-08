import { expect, test } from './base'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'
import { kansalainen } from './setup/auth'

const vanhanKälinOpiskeluoikeudet = [
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

    const perusopetus = page
      .getByTestId(/^oo\.\d+\.opiskeluoikeus\.expand$/)
      .filter({ hasText: 'Perusopetus' })
    await perusopetus.click()
    const päättötodistus = page
      .getByTestId(/^oo\.\d+\.suoritusTabs\.\d+\.tab$/)
      .filter({ hasText: 'Päättötodistus' })
    await päättötodistus.click()
    await expect(page.locator('.Tabs__item-active')).toHaveText(
      'Päättötodistus'
    )

    for (const opiskeluoikeus of vanhanKälinOpiskeluoikeudet) {
      await kansalainenPage.openOpiskeluoikeus(opiskeluoikeus)
    }

    await expect(page.locator('.opiskeluoikeus-content')).toHaveCount(
      vanhanKälinOpiskeluoikeudet.length
    )
    await takeFullPageScreenshot(
      page,
      'omat-tiedot-opiskeluoikeudet-avattu.png'
    )
  })
})
