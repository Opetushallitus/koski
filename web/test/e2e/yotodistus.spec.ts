import { test, expect } from './base'
import { kansalainen } from './setup/auth'

const hetut = {
  ylioppilas: '080698-967F',
  vanhaTutkinto: '190580-678T',
  maksamatonTutkintomaksu: '101000A3582',
  ongelmiaJärjestelmissä: '280100A855E',
  luontiTimeouttaa: '270900A2635',
  huoltaja: '030300-5215'
}

test.describe.skip('Digitaalinen yo-todistus', () => {
  test.beforeEach(async ({ fixtures, virkailijaLoginPage }) => {
    await virkailijaLoginPage.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await virkailijaLoginPage.apiLogout()
  })

  test.describe('Todistuksen lataaminen onnistuneesti', () => {
    test.use({ storageState: kansalainen(hetut.ylioppilas) })

    test('Oman todistuksen lataaminen onnistuu', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.ylioppilas)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.setYoTodistusLanguage('en')
      await kansalainenPage.generateYoTodistus()
      await kansalainenPage.getYoTodistusFile()
    })

    test('Vaikka todistus on luotu yhdelle kielelle, sitä ei ole vielä muille', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.ylioppilas)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.setYoTodistusLanguage('en')
      await kansalainenPage.generateYoTodistus()
      await kansalainenPage.setYoTodistusLanguage('fi')
      const button = kansalainenPage.$.yoTodistus.start
      await button.waitFor()
      expect(await button.isVisible()).toBeTruthy()
    })
  })

  test.describe('Huoltajana käyttäminen', () => {
    test.use({ storageState: kansalainen(hetut.huoltaja) })

    test('Huollettavan todistuksen lataaminen onnistuu', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.huoltaja)
      await kansalainenPage.openHuollettava('Ynjevi Ylioppilaslukiolainen')
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.generateYoTodistus()
      await kansalainenPage.getYoTodistusFile()
    })
  })

  test.describe('Virheiden hallinta', () => {
    test.use({ storageState: kansalainen(hetut.ylioppilas) })

    test('Lataaminen estetty, jos vanha tutkinto', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.vanhaTutkinto)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      expect(await kansalainenPage.getYoTodistusError()).toEqual(
        'Todistus ei ole ladattavissa itsepalvelun kautta. Voit tilata maksullisen todistuksen Ylioppilastutkintolautakunnan verkkopalvelusta https://kokelas.ylioppilastutkinto.fi Tarvittaessa saat lisätietoa Ylioppilastutkintolautakunnalta osoitteesta lautakunta@ylioppilastutkinto.fi'
      )
    })

    test('Lataaminen estetty, jos maksamaton tutkintomaksu', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.maksamatonTutkintomaksu)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      expect(await kansalainenPage.getYoTodistusError()).toEqual(
        'Todistuksen lataaminen on estetty. Syynä voi olla esimerkiksi maksamaton tutkintomaksu. Lisätietoa saa tarvittaessa Ylioppilastutkintolautakunnalta osoitteesta lautakunta@ylioppilastutkinto.fi'
      )
    })

    test('Käyttäjälle näytetään virheilmoitus jos YTR:n päässä tapahtui virhe', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.ongelmiaJärjestelmissä)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.generateYoTodistus()
      expect(await kansalainenPage.getYoTodistusError()).toEqual(
        'Todistuksen luonti epäonnistui teknisen ongelman takia. Odota muutama minuutti ja lataa todistus uudelleen. Jos ongelma jatkuu muutaman yrityksen jälkeen, ota yhteyttä Ylioppilastutkintolautakuntaan osoitteeseen lautakunta@ylioppilastutkinto.fi'
      )
    })

    test('Käyttäjälle näytetään virheilmoitus jos Kosken päässä tapahtui virhe', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.ongelmiaJärjestelmissä)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.setYoTodistusLanguage('en') // MockYtrClient simuloi Kosken backendissa tapahtuneen virheen, jos kieli on muu kuin suomi
      await kansalainenPage.generateYoTodistus()
      expect(await kansalainenPage.getYoTodistusError()).toEqual(
        'Tapahtui odottamaton tekninen ongelma. Jos ongelma jatkuu, ota yhteyttä KOSKI-tiimiin osoitteeseen koski@opintopolku.fi'
      )
    })

    test('Käyttäjälle näytetään virheilmoitus jos todistuksen luonti kestää odottamattoman kauan', async ({
      kansalainenLoginPage,
      kansalainenPage
    }) => {
      await kansalainenLoginPage.loginWithHetu(hetut.luontiTimeouttaa)
      await kansalainenPage.openOpiskeluoikeus('Ylioppilastutkinto')
      await kansalainenPage.generateYoTodistus()
      expect(await kansalainenPage.getYoTodistusError()).toContain(
        'aloitettu todistuksen luonti epäonnistui palvelun ruuhkautumisen takia.'
      )
    })
  })
})
