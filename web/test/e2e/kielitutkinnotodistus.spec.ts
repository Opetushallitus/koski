import { test, expect } from './base'
import { kansalainen, virkailija } from './setup/auth'

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

  test.describe('Printattavan todistuksen lataaminen', () => {
    test.use({ storageState: virkailija('pää') })

    test('pääkäyttäjänä onnistuu', async ({ page }) => {
      await page.goto(
        '/koski/oppija/1.2.246.562.24.00000000177?pdf-todistus=true'
      )

      await page
        .getByTestId(
          'oo.0.suoritukset.0.kielitutkintoTodistus.pdfTemplate.input'
        )
        .click()
      await page
        .getByTestId(
          'oo.0.suoritukset.0.kielitutkintoTodistus.pdfTemplate.options.tulostettava_uusi.item'
        )
        .click()
      await page
        .getByTestId('oo.0.suoritukset.0.kielitutkintoTodistus.start')
        .click()

      const downloadPromise = page.waitForEvent('download')

      await page
        .getByTestId('oo.0.suoritukset.0.kielitutkintoTodistus.open')
        .click()

      const download = await downloadPromise
      // Tarkista että lataus onnistui
      await download.path()
    })
  })

  test.describe('Todistuksen lataaminen onnistuneesti', () => {
    test.use({ storageState: kansalainen(hetut.kielitutkinnonSuorittaja) })

    test('Oman todistuksen lataaminen onnistuu', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('fi')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()
      await kielitutkintoOppijaPage.getKielitutkintoTodistusFile()
    })

    test('Todistuksen voi ladata englanniksi', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('en')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()
      await kielitutkintoOppijaPage.getKielitutkintoTodistusFile()
    })

    test('Todistuksen voi ladata ruotsiksi', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('sv')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()
      await kielitutkintoOppijaPage.getKielitutkintoTodistusFile()
    })

    test('Vaikka todistus on luotu yhdelle kielelle, sitä ei ole vielä muille', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('en')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('fi')
      const button =
        kielitutkintoOppijaPage.$.suoritukset(0).kielitutkintoTodistus.start
      await button.waitFor()
      expect(await button.isVisible()).toBeTruthy()
    })
  })

  test.describe('Virheiden hallinta', () => {
    test.use({ storageState: kansalainen(hetut.kielitutkintoTodistusVirhe) })

    test('Käyttäjälle näytetään virheilmoitus jos todistuksen luonti epäonnistuu', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('en')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()

      // Tarkista että virheilmoitus näkyy
      expect(
        await kielitutkintoOppijaPage.getKielitutkintoTodistusError()
      ).toEqual(
        'Tapahtui odottamaton tekninen ongelma. Jos ongelma jatkuu, ota yhteyttä KOSKI-tiimiin osoitteeseen koski@opintopolku.fi'
      )

      // Tarkista että latausnappi ei näy
      const openButton =
        kielitutkintoOppijaPage.$.suoritukset(0).kielitutkintoTodistus.open
      expect(await openButton.isVisible()).toBeFalsy()

      // Tarkista että samaa kieltä voi yrittää uudelleen
      const startButton =
        kielitutkintoOppijaPage.$.suoritukset(0).kielitutkintoTodistus.start
      expect(await startButton.isVisible()).toBeTruthy()
    })

    test('Todistus luodaan onnistuneesti mutta lataus epäonnistuu ruotsiksi', async ({
      page,
      kansalainenPage,
      kielitutkintoOppijaPage,
      context
    }) => {
      await page.goto('/koski/omattiedot?pdf-todistus=true')
      await kansalainenPage.openOpiskeluoikeusByIndex(0)
      await kielitutkintoOppijaPage.setKielitutkintoTodistusLanguage('sv')
      await kielitutkintoOppijaPage.generateKielitutkintoTodistus()

      // Tarkista että todistus on luotu onnistuneesti
      const openButton =
        kielitutkintoOppijaPage.$.suoritukset(0).kielitutkintoTodistus.open
      await openButton.waitFor()
      expect(await openButton.isVisible()).toBeTruthy()

      // Tarkista että linkki osoittaa oikeaan paikkaan
      const link = await page.getByTestId(
        'oo.0.suoritukset.0.kielitutkintoTodistus.open'
      )
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

      const statusCode = await errorContent
        .locator('h1.http-status')
        .textContent()
      expect(statusCode).toBe('500')

      const errorMessage = await errorContent
        .locator('.error-message')
        .textContent()
      expect(errorMessage).toContain(
        'Todistuksen lataus epäonnistui testitarkoitukseen.'
      )
      expect(errorMessage).toContain('Yritä uudestaan')

      await errorPage.close()
    })
  })
})
