import { expect, test } from './base'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'
import { virkailija } from './setup/auth'

test.describe('Korkeakoulutus – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test.describe('Virkailija', () => {
    test.use({ storageState: virkailija('pää') })

    test('Lisätiedot ja opintojaksot avattu', async ({
      page,
      virkailijaPage,
      oppijaHaku,
      oppijaPage
    }) => {
      await virkailijaPage.goto()
      await (await oppijaHaku.search('250668-293Y')).clickOnFirst()
      await oppijaPage.selectOpiskeluoikeus('korkeakoulutus')

      const tutkinto = oppijaPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., kemian tekniikka'
      )
      await tutkinto.avaaLisätiedot()

      const maksuton = oppijaPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2009—2009/
      )
      await maksuton.avaaLisätiedot()

      const maksullinen = oppijaPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2015—2016/
      )
      await maksullinen.avaaLisätiedot()
      await maksullinen.avaaKaikki()
      await expect(
        maksullinen.container.locator('.tutkinnon-osa .details').first()
      ).toBeVisible()

      await takeFullPageScreenshot(
        page,
        'korkeakoulu-virkailija-katselu-avattu.png'
      )
    })
  })

  test.describe('Kansalainen', () => {
    test('Lisätiedot ja ensimmäinen opintojakso avattu', async ({
      page,
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      // Kirjaudu resetin jälkeen: Virta-oppija luodaan henkilörekisteriin kirjautuessa.
      await kansalainenLoginPage.loginWithHetu('250668-293Y')
      await kansalainenPage.openOpiskeluoikeus('Dipl.ins., kemian tekniikka')
      const tutkinto = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        'Dipl.ins., kemian tekniikka'
      )
      await tutkinto.avaaLisätiedot()

      await kansalainenPage.openOpiskeluoikeus(/Avoimen opinnot.*2009—2009/)
      const maksuton = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2009—2009/
      )
      await maksuton.avaaLisätiedot()

      await kansalainenPage.openOpiskeluoikeus(/Avoimen opinnot.*2015—2016/)
      const maksullinen = kansalainenPage.getKorkeakouluOpiskeluoikeus(
        /Avoimen opinnot.*2015—2016/
      )
      await maksullinen.avaaLisätiedot()
      await maksullinen.avaaEnsimmäinenOpintojakso()
      await expect(maksullinen.ensimmäisenOpintojaksonLisätiedot).toBeVisible()

      await takeFullPageScreenshot(
        page,
        'korkeakoulu-kansalainen-katselu-avattu.png'
      )
    })
  })
})
