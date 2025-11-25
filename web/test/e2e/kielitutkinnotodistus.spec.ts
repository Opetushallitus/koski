import { test, expect } from './base'
import { kansalainen } from './setup/auth'

const hetut = {
  kielitutkinnonSuorittaja: '010107A329V',
  kielitutkintoTodistusVirhe: '020107A540T'
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

  test.describe('Virheiden hallinta', () => {
    test.use({ storageState: kansalainen(hetut.kielitutkintoTodistusVirhe) })

    test('Käyttäjälle näytetään virheilmoitus jos todistuksen luonti epäonnistuu', async ({
      page,
      kansalainenPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('en')
      await kansalainenPage.generateKielitutkintoTodistus()

      // Tarkista että virheilmoitus näkyy
      expect(await kansalainenPage.getKielitutkintoTodistusError()).toEqual(
        'Tapahtui odottamaton tekninen ongelma. Jos ongelma jatkuu, ota yhteyttä KOSKI-tiimiin osoitteeseen koski@opintopolku.fi'
      )

      // Tarkista että latausnappi ei näy
      const openButton = kansalainenPage.$.kielitutkintoTodistus.open
      expect(await openButton.isVisible()).toBeFalsy()

      // Tarkista että samaa kieltä voi yrittää uudelleen
      const startButton = kansalainenPage.$.kielitutkintoTodistus.start
      expect(await startButton.isVisible()).toBeTruthy()
    })

    test('Todistus luodaan onnistuneesti mutta lataus epäonnistuu ruotsiksi', async ({
      page,
      kansalainenPage,
      context
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kansalainenPage.setKielitutkintoTodistusLanguage('sv')
      await kansalainenPage.generateKielitutkintoTodistus()

      // Tarkista että todistus on luotu onnistuneesti
      const openButton = kansalainenPage.$.kielitutkintoTodistus.open
      await openButton.waitFor()
      expect(await openButton.isVisible()).toBeTruthy()

      // Tarkista että linkki osoittaa oikeaan paikkaan
      const link = await page.getByTestId('kielitutkintoTodistus.open')
      const href = await link.getAttribute('href')
      expect(href).toContain('/koski/todistus/download/')

      // Avaa linkki uuteen välilehteen ja tarkista virhesivu
      const pagePromise = context.waitForEvent('page')
      await link.click()
      const errorPage = await pagePromise
      await errorPage.waitForLoadState()

      // Tarkista että virhesivu näyttää HTML-muotoisen virheilmoituksen
      const errorContent = errorPage.locator('.error.content-area')
      await errorContent.waitFor()

      const statusCode = await errorContent.locator('h1.http-status').textContent()
      expect(statusCode).toBe('500')

      const errorMessage = await errorContent.locator('.error-message').textContent()
      expect(errorMessage).toContain('Todistuksen lataus epäonnistui testitarkoitukseen.')
      expect(errorMessage).toContain('Yritä uudestaan')

      await errorPage.close()
    })
  })
})
