import { expect, test } from './base'
import { kansalainen } from './setup/auth'

test.describe('Omat tiedot', () => {
  test.use({ storageState: kansalainen('220109-784L') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test('Perusopetukseen valmistava opetus', async ({ kansalainenPage }) => {
    await kansalainenPage.goto()
    await kansalainenPage.openOpiskeluoikeus('Perusopetukseen valmistava opetus')

    await expect(kansalainenPage.arviointiasteikkoOtsikko).toHaveText(
      'Arviointiasteikko'
    )
    await expect(kansalainenPage.arviointiasteikkoTeksti).toHaveText(
      'Arvostelu 4-10, S (suoritettu), H (hylätty) tai O (osallistunut)'
    )
  })
})
