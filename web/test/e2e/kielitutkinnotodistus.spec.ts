import { test, expect } from './base'
import { kansalainen } from './setup/auth'

const hetut = {
  kielitutkinnonSuorittaja: '010107A329V'
}

test.describe('Digitaalinen kielitutkintotodistus', () => {
  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test.describe('Todistuksen lataaminen onnistuneesti', () => {
    test.use({ storageState: kansalainen(hetut.kielitutkinnonSuorittaja) })

    test('Oman todistuksen lataaminen onnistuu', async ({
      page,
      kansalainenPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('fi')
      await kansalainenPage.generateKielitutkintoTodistus()
      await kansalainenPage.getKielitutkintoTodistusFile()
    })

    test('Todistuksen voi ladata englanniksi', async ({
      page,
      kansalainenPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('en')
      await kansalainenPage.generateKielitutkintoTodistus()
      await kansalainenPage.getKielitutkintoTodistusFile()
    })

    test('Todistuksen voi ladata ruotsiksi', async ({
      page,
      kansalainenPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('sv')
      await kansalainenPage.generateKielitutkintoTodistus()
      await kansalainenPage.getKielitutkintoTodistusFile()
    })

    test('Vaikka todistus on luotu yhdelle kielelle, sitä ei ole vielä muille', async ({
      page,
      kansalainenPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('en')
      await kansalainenPage.generateKielitutkintoTodistus()
      await kansalainenPage.setKielitutkintoTodistusLanguage('fi')
      const button = kansalainenPage.$.kielitutkintoTodistus.start
      await button.waitFor()
      expect(await button.isVisible()).toBeTruthy()
    })
  })
})
