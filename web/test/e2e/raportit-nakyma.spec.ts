import { test, expect } from './base'
import { virkailija } from './setup/auth'
import { formatFinnishDate } from '../../app/date/date'

const kaikkiKategoriat = [
  'Esiopetus',
  'Perusopetus',
  'Perusopetukseen valmistava opetus',
  'TUVA-koulutus',
  'Aikuisten perusopetus',
  'Ammatillinen koulutus',
  'Lukiokoulutus',
  'Lukiokoulutus (LOPS2021)',
  'IB-koulutus',
  'VST',
  'MUKS',
  'Yleiset'
]

const esiopetuksenRaportit = [
  'Opiskeluoikeus- ja suoritustiedot',
  'Tunnusluvut: oppilasmäärät'
]

const lukionRaportit = [
  'Opiskeluoikeus- ja suoritustiedot',
  'Tunnusluvut: kurssikertymät',
  'Tunnusluvut: opiskelijamäärät',
  'Tunnusluvut: LUVA-opiskelijamäärät'
]

const kallenEsiopetuksenOrganisaatiot = [
  'Aapajoen koulu',
  'Jyväskylän normaalikoulu',
  'Kulosaaren ala-aste'
]

const kallenKaikkiOrganisaatiot = [
  'Aalto-yliopisto',
  'Aapajoen koulu',
  'Helsingin Saksalainen koulu',
  'Helsingin eurooppalainen koulu',
  'Helsingin medialukio',
  'Helsingin yliopisto',
  'International School of Helsinki',
  'Itä-Suomen yliopisto',
  'Jatkuva Koulutus Oy',
  'Jyväskylän normaalikoulu',
  'Kallaveden lukio',
  'Kulosaaren ala-aste',
  'Kuopion aikuislukio',
  'Lahden ammattikorkeakoulu',
  'Omnia',
  'Oulun aikuislukio',
  'PK Vironniemi',
  'Ressun lukio',
  'Sockenbacka lågstadieskola',
  'Stadin ammatti- ja aikuisopisto',
  'Varsinais-Suomen kansanopisto',
  'WinNova',
  'Ylioppilastutkintolautakunta',
  'Yrkeshögskolan Arcada',
  'Övernäs skola'
]

const helsinginEsiopetuksenOrganisaatiot = [
  'Helsingin kaupunki',
  'Kulosaaren ala-aste',
  'Ostopalvelu/palveluseteli'
]

const helsinginPerusopetuksenOrganisaatiot = [
  'Helsingin kaupunki',
  'Kulosaaren ala-aste',
  'Stadin ammatti- ja aikuisopisto'
]

const tänään = () => formatFinnishDate(new Date())!

test.describe('Raporttinäkymä ilman kirjautumista', () => {
  test('Näyttää kirjautumissivun', async ({ page, virkailijaLoginPage }) => {
    await page.goto('/koski/raportit')
    await expect(virkailijaLoginPage.loginButton).toBeVisible()
  })
})

test.describe('Raporttinäkymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen
    await fixtures.reset(true)
  })

  test.describe('Raporttikategoriat', () => {
    test('Näyttää käyttäjän kategoriat ja valitsee ensimmäisen', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await expect(raportitPage.kategoriat).toHaveText(kaikkiKategoriat)
      await expect(raportitPage.valittuKategoria).toHaveText('Esiopetus')
      await expect(raportitPage.otsikko).toHaveText('Esiopetuksen raportit')
    })

    test('Kategorian vaihtaminen vaihtaa otsikon', async ({ raportitPage }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Perusopetus')
      await expect(raportitPage.otsikko).toHaveText('Perusopetuksen raportit')
    })

    test('Yhden raportin kategoriassa ei näytetä raporttivalintaa', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('IB-koulutus')
      await expect(raportitPage.otsikko).toHaveText('IB-koulutuksen raportit')
      await expect(raportitPage.muodostaButton).toBeVisible()
      await expect(raportitPage.raporttivalinnat).toHaveCount(0)
    })

    test.describe('Pääkäyttäjä', () => {
      test.use({ storageState: virkailija('pää') })

      test('Näkee kaikki kategoriat ja organisaatiot hierarkiassa', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await expect(raportitPage.kategoriat).toHaveText(kaikkiKategoriat)
        await expect(raportitPage.valittuKategoria).toHaveText('Esiopetus')
        await expect(raportitPage.organisaatiot).toHaveText([
          'Helsingin kaupunki',
          'Kulosaaren ala-aste',
          'Jyväskylän yliopisto',
          'Jyväskylän normaalikoulu',
          'Pyhtään kunta',
          'Päiväkoti Majakka',
          'Päiväkoti Touhula',
          'Tornion kaupunki',
          'Aapajoen koulu'
        ])
        await expect(
          raportitPage.organisaatiovalitsin.locator(
            '.options > .option > .value'
          )
        ).toHaveText([
          'Helsingin kaupunki',
          'Jyväskylän yliopisto',
          'Pyhtään kunta',
          'Tornion kaupunki'
        ])
      })
    })

    test.describe('Esiopetuksen käyttäjä', () => {
      test.use({ storageState: virkailija('stadin-esiopetus') })

      test('Näkee vain kategoriat, joiden raportteihin hänellä on oikeus', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await expect(raportitPage.kategoriat).toHaveText([
          'Esiopetus',
          'Yleiset'
        ])
      })
    })
  })

  test.describe('Raporttivalinta', () => {
    test('Näyttää kategorian raportit ja valitsee ensimmäisen', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await expect(raportitPage.raporttivalinnat).toHaveText(
        esiopetuksenRaportit
      )
      await expect(raportitPage.valittuRaportti).toHaveText(
        esiopetuksenRaportit[0]
      )

      await raportitPage.valitseKategoria('Lukiokoulutus')
      await expect(raportitPage.raporttivalinnat).toHaveText(lukionRaportit)
      await expect(raportitPage.valittuRaportti).toHaveText(lukionRaportit[0])
    })

    test('Kategorian vaihtaminen valitsee kategorian ensimmäisen raportin', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Lukiokoulutus')
      await raportitPage.valitseRaportti('Tunnusluvut: kurssikertymät')

      await raportitPage.valitseKategoria('Esiopetus')
      await expect(raportitPage.valittuRaportti).toHaveText(
        esiopetuksenRaportit[0]
      )
    })

    test('Raportin vaihtaminen palauttaa lomakkeen oletusarvot', async ({
      raportitPage
    }) => {
      await raportitPage.goto(
        '/lukio/lukiodiaibinternationaleshopiskelijamaarat'
      )
      await raportitPage.päiväkenttä.fill('1.1.2016')
      await raportitPage.valitseRaportti('Tunnusluvut: LUVA-opiskelijamäärät')
      await expect(raportitPage.päiväkenttä).toHaveValue(tänään())

      await raportitPage.valitseRaportti('Tunnusluvut: kurssikertymät')
      await raportitPage.alkuKenttä.fill('1.1.2016')
      await raportitPage.loppuKenttä.fill('31.12.2016')
      await raportitPage.valitseRaportti('Opiskeluoikeus- ja suoritustiedot')
      await expect(raportitPage.alkuKenttä).toHaveValue('')
      await expect(raportitPage.loppuKenttä).toHaveValue('')
    })
  })

  test.describe('Osoite', () => {
    test('Avaa osoitteen mukaisen kategorian ja raportin', async ({
      raportitPage
    }) => {
      await raportitPage.goto('/lukio/lukiokurssikertyma')
      await expect(raportitPage.valittuKategoria).toHaveText('Lukiokoulutus')
      await expect(raportitPage.valittuRaportti).toHaveText(
        'Tunnusluvut: kurssikertymät'
      )

      await raportitPage.goto('/esiopetus/esiopetuksenoppijamäärienraportti')
      await expect(raportitPage.valittuKategoria).toHaveText('Esiopetus')
      await expect(raportitPage.valittuRaportti).toHaveText(
        'Tunnusluvut: oppilasmäärät'
      )
    })

    test('Tuntemattomasta raportista avataan kategorian ensimmäinen raportti', async ({
      page,
      raportitPage
    }) => {
      await raportitPage.goto('/lukio/tuntematon')
      await expect(raportitPage.valittuKategoria).toHaveText('Lukiokoulutus')
      await expect(raportitPage.valittuRaportti).toHaveText(lukionRaportit[0])
      await expect(page).toHaveURL(
        /\/koski\/raportit\/lukio\/lukionsuoritustietojentarkistus$/
      )
    })

    test('Puuttuvasta tai tuntemattomasta kategoriasta avataan ensimmäinen kategoria', async ({
      page,
      raportitPage
    }) => {
      for (const polku of ['', '/tuntematon']) {
        await raportitPage.goto(polku)
        await expect(raportitPage.valittuKategoria).toHaveText('Esiopetus')
        await expect(raportitPage.valittuRaportti).toHaveText(
          esiopetuksenRaportit[0]
        )
        await expect(page).toHaveURL(
          /\/koski\/raportit\/esiopetus\/esiopetuksenraportti$/
        )
      }
    })

    test('Osoite päivittyy valintojen mukaan ja säilyttää kyselyparametrit', async ({
      page,
      raportitPage
    }) => {
      await raportitPage.goto('?testi=1')
      await raportitPage.valitseKategoria('Lukiokoulutus')
      await expect(page).toHaveURL(
        /\/koski\/raportit\/lukio\/lukionsuoritustietojentarkistus\?testi=1$/
      )

      await raportitPage.valitseRaportti('Tunnusluvut: kurssikertymät')
      await expect(page).toHaveURL(
        /\/koski\/raportit\/lukio\/lukiokurssikertyma\?testi=1$/
      )
    })

    test.describe('Esiopetuksen käyttäjä', () => {
      test.use({ storageState: virkailija('stadin-esiopetus') })

      test('Piilotetusta kategoriasta avataan ensimmäisen näkyvän kategorian ensimmäinen raportti', async ({
        page,
        raportitPage
      }) => {
        await raportitPage.goto('/lukio/lukiokurssikertyma')
        await expect(raportitPage.valittuKategoria).toHaveText('Esiopetus')
        await expect(raportitPage.raporttivalinnat).toHaveText(
          esiopetuksenRaportit
        )
        await expect(raportitPage.valittuRaportti).toHaveText(
          esiopetuksenRaportit[0]
        )
        await expect(page).toHaveURL(
          /\/koski\/raportit\/esiopetus\/esiopetuksenraportti$/
        )
      })
    })
  })

  test.describe('Organisaatiovalinta', () => {
    test('Näyttää raportin organisaatiot ja valitsee ensimmäisen', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await expect(raportitPage.organisaatiot).toHaveText(
        kallenEsiopetuksenOrganisaatiot
      )
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Aapajoen koulu'
      )
    })

    test('Valinta säilyy raportin ja kategorian vaihtuessa', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseOrganisaatio('Jyväskylän normaalikoulu')

      await raportitPage.valitseRaportti('Tunnusluvut: oppilasmäärät')
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Jyväskylän normaalikoulu'
      )

      await raportitPage.valitseKategoria('Perusopetus')
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Jyväskylän normaalikoulu'
      )

      await raportitPage.valitseKategoria('Aikuisten perusopetus')
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Jyväskylän normaalikoulu'
      )
    })

    test('Hakusana rajaa organisaatiot', async ({ raportitPage }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Yleiset')
      await raportitPage.haeOrganisaatiota('helsin')
      await expect(raportitPage.organisaatiot).toHaveText([
        'Helsingin Saksalainen koulu',
        'Helsingin eurooppalainen koulu',
        'Helsingin medialukio',
        'Helsingin yliopisto',
        'International School of Helsinki'
      ])
    })

    test('Haku ilman osumia tyhjentää listan', async ({ raportitPage }) => {
      await raportitPage.goto()
      await raportitPage.haeOrganisaatiota('eiloydy')
      await expect(raportitPage.organisaatiot).toHaveCount(0)
    })

    test('Listan sulkeminen hylkää hakusanan', async ({ raportitPage }) => {
      await raportitPage.goto()
      await raportitPage.haeOrganisaatiota('kulo')
      await expect(raportitPage.organisaatiolistaAuki).toBeVisible()
      await expect(raportitPage.organisaatiot).toHaveText([
        'Kulosaaren ala-aste'
      ])

      await raportitPage.otsikko.click()
      await expect(raportitPage.organisaatiolistaAuki).toBeHidden()
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Aapajoen koulu'
      )

      await raportitPage.organisaatiokenttä.click()
      await expect(raportitPage.organisaatiot).toHaveText(
        kallenEsiopetuksenOrganisaatiot
      )
    })

    test('Valintaa voi vaihtaa nuolinäppäimillä ja Enter sulkee listan', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.organisaatiokenttä.click()
      await expect(raportitPage.organisaatiolistaAuki).toBeVisible()
      await expect(raportitPage.listastaValittuOrganisaatio).toHaveText(
        'Aapajoen koulu'
      )

      await raportitPage.organisaatiokenttä.press('ArrowDown')
      await expect(raportitPage.listastaValittuOrganisaatio).toHaveText(
        'Jyväskylän normaalikoulu'
      )
      await raportitPage.organisaatiokenttä.press('ArrowDown')
      await raportitPage.organisaatiokenttä.press('ArrowDown')
      await expect(raportitPage.listastaValittuOrganisaatio).toHaveText(
        'Kulosaaren ala-aste'
      )
      await raportitPage.organisaatiokenttä.press('ArrowUp')
      await expect(raportitPage.listastaValittuOrganisaatio).toHaveText(
        'Jyväskylän normaalikoulu'
      )

      await raportitPage.organisaatiokenttä.press('Enter')
      await expect(raportitPage.organisaatiolistaAuki).toBeHidden()
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Jyväskylän normaalikoulu'
      )
    })

    test.describe('Koulutustoimijan käyttäjä', () => {
      test.use({ storageState: virkailija('hki-tallentaja') })

      test('Voi valita ostopalvelun vain esiopetuksen raporteille', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await expect(raportitPage.organisaatiot).toHaveText(
          helsinginEsiopetuksenOrganisaatiot
        )

        await raportitPage.valitseKategoria('Perusopetus')
        await expect(raportitPage.organisaatiot).toHaveText(
          helsinginPerusopetuksenOrganisaatiot
        )
      })

      test('Valinta vaihtuu ensimmäiseen valittavaan, jos sitä ei ole uuden kategorian listassa', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await raportitPage.valitseOrganisaatio('Ostopalvelu/palveluseteli')

        await raportitPage.valitseKategoria('Perusopetus')
        await expect(raportitPage.organisaatiot).toHaveText(
          helsinginPerusopetuksenOrganisaatiot
        )
        const ensimmäinenValittava = await raportitPage.valittavatOrganisaatiot
          .first()
          .textContent()
        await expect(raportitPage.organisaatiokenttä).toHaveValue(
          ensimmäinenValittava!
        )
      })
    })

    test.describe('Pääkäyttäjä', () => {
      test.use({ storageState: virkailija('pää') })

      test('Haku juuriorganisaatiolla näyttää sen kaikki toimipisteet', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await raportitPage.valitseKategoria('Yleiset')
        await raportitPage.haeOrganisaatiota('itä-suomen yliopisto')
        await expect(raportitPage.organisaatiot).toHaveText([
          'Itä-Suomen yliopisto',
          'Itä-Suomen yliopisto',
          'Joensuun normaalikoulu',
          'Rantakylän normaalikoulu',
          'Savonlinnan normaalikoulu',
          'Tulliportin normaalikoulu'
        ])
      })

      test('Haku toimipisteellä näyttää sen yläorganisaatiot', async ({
        raportitPage
      }) => {
        await raportitPage.goto()
        await raportitPage.valitseKategoria('Yleiset')
        await raportitPage.haeOrganisaatiota('aikuisopisto')
        await expect(raportitPage.organisaatiot).toHaveText([
          'Espoon seudun koulutuskuntayhtymä Omnia',
          'Omnian aikuisopisto',
          'Helsingin kaupunki',
          'Stadin aikuisopisto',
          'Stadin ammatti- ja aikuisopisto'
        ])
      })
    })
  })

  test.describe('Päivämäärät', () => {
    test('Päivän raportissa on oletuksena kuluva päivä', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await expect(raportitPage.päivämääräkentät).toHaveCount(2)
      await expect(raportitPage.päiväkenttä).toHaveValue(tänään())
      await expect(raportitPage.kotikuntaPvmKenttä).toHaveValue(tänään())
      await expect(raportitPage.muodostaButton).toBeEnabled()
    })

    test('Virheellinen päivä estää raportin muodostamisen', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.päiväkenttä.fill('12.12')
      await expect(raportitPage.muodostaButton).toBeDisabled()

      await raportitPage.päiväkenttä.fill('12.12.2020')
      await expect(raportitPage.muodostaButton).toBeEnabled()

      await raportitPage.kotikuntaPvmKenttä.fill('12.12')
      await expect(raportitPage.muodostaButton).toBeDisabled()
    })

    test('Aikajakson raportin kentät ovat oletuksena tyhjiä', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Aikuisten perusopetus')
      await expect(raportitPage.päivämääräkentät).toHaveCount(2)
      await expect(raportitPage.alkuKenttä).toHaveValue('')
      await expect(raportitPage.loppuKenttä).toHaveValue('')
      await expect(raportitPage.muodostaButton).toBeDisabled()
    })

    test('Raportin voi muodostaa, kun aikajakson molemmat päivät on annettu', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Aikuisten perusopetus')
      await raportitPage.alkuKenttä.fill('1.1.2020')
      await expect(raportitPage.muodostaButton).toBeDisabled()

      await raportitPage.loppuKenttä.fill('31.1.2020')
      await expect(raportitPage.muodostaButton).toBeEnabled()
    })

    test('Aikajakso ei voi päättyä ennen alkuaan', async ({ raportitPage }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Aikuisten perusopetus')
      await raportitPage.alkuKenttä.fill('31.1.2020')
      await raportitPage.loppuKenttä.fill('30.1.2020')
      await expect(raportitPage.muodostaButton).toBeDisabled()

      await raportitPage.loppuKenttä.fill('31.1.2020')
      await expect(raportitPage.muodostaButton).toBeEnabled()
    })
  })

  test.describe('Päällekkäisten opiskeluoikeuksien raportti', () => {
    test('Näyttää kaikki organisaatiot, säilyttää valinnan ja näyttää raportointikannan päivitysajan', async ({
      raportitPage
    }) => {
      await raportitPage.goto()
      await raportitPage.valitseKategoria('Yleiset')
      await expect(raportitPage.organisaatiovalitsin).toBeVisible()
      await expect(raportitPage.organisaatiot).toHaveText(
        kallenKaikkiOrganisaatiot
      )
      await expect(raportitPage.organisaatiokenttä).toHaveValue(
        'Aapajoen koulu'
      )
      await expect(raportitPage.päivitysaika).toHaveText(
        /^\d{1,2}\.\d{1,2}\.\d{4} \d{1,2}:\d{2}$/
      )
    })
  })
})
