import { test, expect } from './base'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { getVirkailijaSession, kansalainen, virkailija } from './setup/auth'

const ESH_OID = '1.2.246.562.24.00000000065'

test.describe('European School of Helsinki', () => {
  test.describe('Virkailijan näkymä', () => {
    test.use({ storageState: virkailija('kalle') })

    test.beforeEach(async ({ fixtures, eshOppijaPage }) => {
      await fixtures.reset(false)
      await eshOppijaPage.goto(ESH_OID)
      await eshOppijaPage.selectOpiskeluoikeus('europeanschoolofhelsinki')
    })

    test('Näyttää oppijan tiedot oikein', async ({ eshOppijaPage }) => {
      await expect(eshOppijaPage.oppijaHeading).toContainText(
        'Eurooppalainen, Emilia (050707A130V)'
      )
      await expect(eshOppijaPage.hetu).toContainText('050707A130V')
      await expect(eshOppijaPage.koulutusmoduuli).toContainText('S72023')
      await expect(eshOppijaPage.luokka).toContainText('S7A')
      await expect(eshOppijaPage.toimipiste).toContainText(
        'Helsingin eurooppalainen koulu'
      )
    })

    test.describe('Vuosiluokan poisto ja uudelleenluonti', () => {
      test.afterEach(async ({ fixtures }) => {
        await fixtures.reset()
      })

      test('S3-vuosiluokka', async ({ page, eshOppijaPage }) => {
        test.slow()
        const vuosiluokka = 'S3'

        await eshOppijaPage.poistaSuoritus(vuosiluokka)

        await eshOppijaPage.poistaViimeisinTila()

        await eshOppijaPage.lisääVuosiluokanSuoritus(vuosiluokka, '1.8.2014')

        const luokkaInput = page
          .getByTestId('luokka-value')
          .locator('input[type="text"]')
        await luokkaInput.click()
        await luokkaInput.fill(`${vuosiluokka}A`)

        await eshOppijaPage.avaaKaikkiOsasuoritukset()

        const firstLanguageOsasuoritus =
          eshOppijaPage.getOsasuoritus('First language')
        await firstLanguageOsasuoritus.valitseKieli('englanti')
        await firstLanguageOsasuoritus.valitseSuorituskieli('englanti')
        await firstLanguageOsasuoritus.syötäLaajuus('3')
        await firstLanguageOsasuoritus.valitseArvosana('B')

        const mathematicsOsasuoritus =
          eshOppijaPage.getOsasuoritus('Mathematics')
        await mathematicsOsasuoritus.valitseSuorituskieli('aimara')
        await mathematicsOsasuoritus.syötäLaajuus('6')
        await mathematicsOsasuoritus.valitseArvosana('A')

        const poistettavatOsasuoritukset = [
          'Second language',
          'Third language',
          'Other National Language',
          'Host Country Language',
          'Physical Education',
          'Religious Education',
          'Human Sciences',
          'Integrated Science',
          'Art',
          'Music',
          'Information and Communication Technology',
          'Health Education'
        ]
        for (const os of poistettavatOsasuoritukset) {
          const osasuoritus = eshOppijaPage.getOsasuoritus(os)
          await osasuoritus.poista()
        }

        await eshOppijaPage.vahvistaSuoritus('1.1.2015')

        await eshOppijaPage.opiskeluoikeudenTila.lisääTila(
          '31.5.2024',
          'Valmistunut'
        )

        await eshOppijaPage.tallenna()
        await eshOppijaPage.expectSuoritusUrl(vuosiluokka, '')
        await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
      })

      test('S6-vuosiluokka', async ({ page, eshOppijaPage }) => {
        test.slow()
        const vuosiluokka = 'S6'

        await eshOppijaPage.poistaSuoritus(vuosiluokka)

        await eshOppijaPage.poistaViimeisinTila()

        await eshOppijaPage.lisääVuosiluokanSuoritus(vuosiluokka, '1.8.2017')

        const luokkaInput = page
          .getByTestId('luokka-value')
          .locator('input[type="text"]')
        await luokkaInput.click()
        await luokkaInput.fill(`${vuosiluokka}A`)

        await eshOppijaPage.avaaKaikkiOsasuoritukset()

        const firstLanguageOsasuoritus =
          eshOppijaPage.getOsasuoritus('First language')
        await firstLanguageOsasuoritus.valitseKieli('englanti')
        await firstLanguageOsasuoritus.valitseSuorituskieli('englanti')
        await firstLanguageOsasuoritus.syötäLaajuus('3')
        await firstLanguageOsasuoritus.valitseArvosana('6.5')

        const mathematicsOsasuoritus =
          eshOppijaPage.getOsasuoritus('Mathematics')
        await mathematicsOsasuoritus.valitseSuorituskieli('aimara')
        await mathematicsOsasuoritus.syötäLaajuus('6')
        await mathematicsOsasuoritus.valitseArvosana('9.0')

        const poistettavatOsasuoritukset = [
          'Second language',
          'Religious Education',
          'Physical Education',
          'Biology',
          'History',
          'Philosophy',
          'Latin',
          'Ancient Greek',
          'Geography',
          'Third language',
          'Fourth language',
          'Physics',
          'Chemistry',
          'Art',
          'Music',
          'Advanced studies of the first language',
          'Advanced studies of the second language',
          'Advanced Mathematics',
          'Practical Physics',
          'Practical Chemistry',
          'Practical Biology',
          'Information and Communication Technology',
          'Sociology',
          'Economics',
          'Political Science'
        ]

        for (const os of poistettavatOsasuoritukset) {
          const osasuoritus = eshOppijaPage.getOsasuoritus(os)
          await osasuoritus.poista()
        }

        await eshOppijaPage.vahvistaSuoritus('1.1.2018')

        await eshOppijaPage.opiskeluoikeudenTila.lisääTila(
          '31.5.2024',
          'Valmistunut'
        )

        await eshOppijaPage.tallenna()
        await eshOppijaPage.expectSuoritusUrl(vuosiluokka, '')
        await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
      })

      test('S7-vuosiluokka', async ({ page, eshOppijaPage }) => {
        test.slow()
        const vuosiluokka = 'S7'

        await eshOppijaPage.poistaSuoritus(vuosiluokka, 'S6')

        await eshOppijaPage.poistaViimeisinTila()

        await eshOppijaPage.lisääVuosiluokanSuoritus(vuosiluokka, '1.8.2018')

        const luokkaInput = page
          .getByTestId('luokka-value')
          .locator('input[type="text"]')
        await luokkaInput.click()
        await luokkaInput.fill(`${vuosiluokka}A`)

        await eshOppijaPage.avaaKaikkiOsasuoritukset()

        const firstLanguageOsasuoritus =
          eshOppijaPage.getOsasuoritus('First language')
        await firstLanguageOsasuoritus.valitseKieli('englanti')
        await firstLanguageOsasuoritus.valitseSuorituskieli('englanti')
        await firstLanguageOsasuoritus.syötäLaajuus('3')
        await firstLanguageOsasuoritus
          .getOsasuoritus('A')
          .valitseArvosana('0.2')
        await firstLanguageOsasuoritus
          .getOsasuoritus('B')
          .valitseArvosana('0.4')

        const mathematicsOsasuoritus =
          eshOppijaPage.getOsasuoritus('Mathematics')
        await mathematicsOsasuoritus.valitseSuorituskieli('aimara')
        await mathematicsOsasuoritus.syötäLaajuus('6')
        await mathematicsOsasuoritus.getOsasuoritus('A').poista()
        await mathematicsOsasuoritus.getOsasuoritus('B').poista()
        await (
          await mathematicsOsasuoritus.lisääOsasuoritus('Year mark')
        ).valitseArvosana('6.4')
        await (
          await mathematicsOsasuoritus.lisääOsasuoritus('Preliminary')
        ).valitseArvosana('6.8')

        const poistettavatOsasuoritukset = [
          'Second language',
          'Religious Education',
          'Physical Education',
          'Biology',
          'History',
          'Philosophy',
          'Latin',
          'Ancient Greek',
          'Geography',
          'Third language',
          'Fourth language',
          'Physics',
          'Chemistry',
          'Art',
          'Music',
          'Advanced studies of the first language',
          'Advanced studies of the second language',
          'Advanced Mathematics',
          'Practical Physics',
          'Practical Chemistry',
          'Practical Biology',
          'Information and Communication Technology',
          'Sociology',
          'Economics',
          'Political Science'
        ]
        for (const os of poistettavatOsasuoritukset) {
          const osasuoritus = eshOppijaPage.getOsasuoritus(os)
          await osasuoritus.poista()
        }

        await eshOppijaPage.vahvistaSuoritus('1.1.2019')

        await eshOppijaPage.opiskeluoikeudenTila.lisääTila(
          '31.5.2024',
          'Valmistunut'
        )

        await eshOppijaPage.tallenna()
        await eshOppijaPage.expectSuoritusUrl(vuosiluokka, '')
        await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
      })

      // Vanhan EB-tutkinnon editointi ei enää toimi, koska tietomalli korvataan uudella erillisellä. Tätä ei kuitenkaan
      // ole tarkoituksenmukaista nyt korjata, koska tarkoitus on poistaa vanha tietomallihaara kokonaan.
      test.skip('EB-tutkinto', async ({ page, eshOppijaPage }) => {
        test.slow()
        await eshOppijaPage.poistaSuoritus('European Baccalaureate', 'S7')

        await eshOppijaPage.poistaViimeisinTila()

        await eshOppijaPage.lisääEBTutkinnonSuoritus()

        const yleisarvosanaInput = page
          .getByTestId('yleisarvosana-value')
          .locator('input[type="text"]')
        await yleisarvosanaInput.click()
        await yleisarvosanaInput.fill('7,77')

        const firstLanguageOsasuoritus = await eshOppijaPage.lisääOsasuoritus(
          'First language'
        )
        await firstLanguageOsasuoritus.valitseKieli('englanti')
        await firstLanguageOsasuoritus.valitseSuorituskieli('englanti')
        await firstLanguageOsasuoritus.syötäLaajuus('3')

        const mathematicsOsasuoritus = await eshOppijaPage.lisääOsasuoritus(
          'Mathematics'
        )

        await mathematicsOsasuoritus.valitseSuorituskieli('aimara')
        await mathematicsOsasuoritus.syötäLaajuus('6')

        await eshOppijaPage.avaaKaikkiOsasuoritukset()

        await (
          await firstLanguageOsasuoritus.lisääOsasuoritus('Preliminary')
        ).valitseArvosana('6.4')
        await (
          await firstLanguageOsasuoritus.lisääOsasuoritus('Written')
        ).valitseArvosana('6.98')
        await (
          await firstLanguageOsasuoritus.lisääOsasuoritus('Oral')
        ).valitseArvosana('8.91')
        await (
          await firstLanguageOsasuoritus.lisääOsasuoritus('Final')
        ).valitseArvosana('7.44')
        await (
          await mathematicsOsasuoritus.lisääOsasuoritus('Final')
        ).valitseArvosana('7.44')

        await eshOppijaPage.vahvistaSuoritus('1.1.2020')

        await eshOppijaPage.opiskeluoikeudenTila.lisääTila(
          '31.5.2024',
          'Valmistunut'
        )

        await eshOppijaPage.tallenna()
        await eshOppijaPage.expectSuoritusUrl(
          'EB-tutkinto\\%20\\(European\\%20Baccalaureate\\)',
          ''
        )
        await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
      })
    })

    test.describe('Primary-vuosiluokan suoritukset', () => {
      test.setTimeout(120000)
      const vuosiluokat = ['P1', 'P5']
      test.beforeEach(async ({ eshOppijaPage }) => {
        await eshOppijaPage.goto(ESH_OID)
        await eshOppijaPage.selectOpiskeluoikeus('europeanschoolofhelsinki')
      })
      vuosiluokat.forEach((vuosiluokka) => {
        test(`Lisää ${vuosiluokka}-vuosiluokan suoritukseen uuden osasuorituksen, jolla ei ole prefillattuja alaosasuorituksia, ja sallii tallentamisen vain kun pakollisissa kentissä ond tiedot syötettynä`, async ({
          eshOppijaPage
        }) => {
          test.slow()
          await eshOppijaPage.clickSuoritusTabByLabel(vuosiluokka, 'first')
          await eshOppijaPage.avaaMuokkausnäkymä()

          // Lisää osasuoritus
          const osasuoritus = await eshOppijaPage.lisääOsasuoritus(
            'Advanced studies of the second language'
          )

          // Syötä osasuorituksen pakolliset tiedot
          await osasuoritus.valitseKieli('englanti')
          await osasuoritus.valitseSuorituskieli('englanti')
          await osasuoritus.syötäLaajuus('4')

          await expect(eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await osasuoritus.valitseArvosana('pass')

          await expect(eshOppijaPage.tallennusBtn).toBeEnabled()

          // Syötä osasuorituksen valinnaiset tiedot
          await osasuoritus.laajennaBtn.click()
          await osasuoritus.syötäArviointipäivä('31.7.2007')
          await osasuoritus.lisääArvioija('Pekka Perhonen')
          await osasuoritus.lisääKuvaus('Good job!')

          // Lisää alaosasuorituksia
          const alaosasuoritus1 = await osasuoritus.lisääOsasuoritus(
            'Listening and understanding'
          )

          await expect(eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await alaosasuoritus1.valitseArvosana('+++')

          await expect(eshOppijaPage.tallennusBtn).toBeEnabled()

          await alaosasuoritus1.laajennaBtn.click()
          await alaosasuoritus1.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus1.lisääArvioija('Paula Perhonen')

          const alaosasuoritus2 = await osasuoritus.lisääOsasuoritus(
            'Reading and understanding'
          )

          await expect(eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await alaosasuoritus2.valitseArvosana('++++')

          await expect(eshOppijaPage.tallennusBtn).toBeEnabled()

          await alaosasuoritus2.laajennaBtn.click()
          await alaosasuoritus2.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus2.lisääArvioija('Paula Perhonen')

          // Tallenna opiskeluoikeus
          await eshOppijaPage.tallenna()

          await eshOppijaPage.expectSuoritusUrl(vuosiluokka, '')

          await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
        })
      })

      test(`Poista P3-vuosiluokalta kaikki osasuoritukset ja lisää prefillattu lapsioppimisalue ja prefillattu oppiaine`, async ({
        eshOppijaPage
      }) => {
        test.slow()
        const vuosiluokka = 'P3'

        await eshOppijaPage.clickSuoritusTabByLabel(vuosiluokka, 'first')
        await eshOppijaPage.avaaMuokkausnäkymä()

        // Poista kaikki olemassaolevat osasuoritukset
        await eshOppijaPage.poistaKaikkiOsasuoritukset()

        // Lisää lapsi-oppimisalueen osasuoritus
        {
          const expectedAlaosasuoritukset = [
            'Engages in learning',
            'Listens attentively',
            'Develops working habits',
            'Works independently',
            'Perseveres with difficult tasks',
            'Uses ICT',
            'Presents work carefully',
            'Produces quality homework'
          ]

          const osasuoritus = await eshOppijaPage.lisääOsasuoritus(
            'The Child As a Learner'
          )
          await osasuoritus.valitseArvosana('pass')
          await osasuoritus.laajennaBtn.click()

          expect(
            await osasuoritus.countOsasuoritukset(
              'primarylapsioppimisalueenalaosasuoritus'
            )
          ).toBe(expectedAlaosasuoritukset.length)

          for (const os of expectedAlaosasuoritukset) {
            const alaosasuoritus = osasuoritus.getOsasuoritus(os)
            await alaosasuoritus.valitseArvosana('++++')
          }
        }

        // Lisää oppiaineen osasuoritus
        {
          const expectedAlaosasuoritukset = [
            'Plastic and static visual arts',
            'The arts and entertainment'
          ]

          const osasuoritus = await eshOppijaPage.lisääOsasuoritus('Art')
          await osasuoritus.valitseSuorituskieli('englanti')
          await osasuoritus.syötäLaajuus('3')
          await osasuoritus.valitseArvosana('pass')
          await osasuoritus.laajennaBtn.click()

          expect(
            await osasuoritus.countOsasuoritukset(
              'primaryoppimisalueenalaosasuoritus'
            )
          ).toBe(expectedAlaosasuoritukset.length)

          for (const os of expectedAlaosasuoritukset) {
            const alaosasuoritus = osasuoritus.getOsasuoritus(os)
            await alaosasuoritus.valitseArvosana('++++')
          }
        }

        // Tallenna opiskeluoikeus
        await eshOppijaPage.tallenna()

        await eshOppijaPage.expectSuoritusUrl(vuosiluokka, '')

        await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
      })
    })

    test.describe('Uuden opiskeluoikeuden luonti', () => {
      const hetu = '110363-155S'
      const oppija = {
        oppilaitos: 'Helsingin eurooppalainen koulu',
        etunimet: 'Ella',
        sukunimi: 'European'
      }

      test.beforeEach(
        async ({ fixtures, virkailijaLoginPage, uusiOppijaPage }) => {
          await fixtures.reset()
          await virkailijaLoginPage.apiLoginAsUser('kalle', 'kalle')
          await uusiOppijaPage.goTo(hetu)
        }
      )

      test('Lisäys ei onnistu ilman valittua opiskeluoikeutta', async ({
        uusiOppijaPage
      }) => {
        await expect(uusiOppijaPage.submitBtn).toBeDisabled()
      })

      test('Vain oikeat opiskeluoikeuden tilat valittavissa', async ({
        uusiOppijaPage
      }) => {
        await uusiOppijaPage.fill({
          oppilaitos: 'Helsingin eurooppalainen koulu'
        })

        await expect(
          await uusiOppijaPage.opiskeluoikeudenTila.getOptions()
        ).toHaveText(
          ['Eronnut', 'Läsnä', 'Valmistunut', 'Väliaikaisesti keskeytynyt'],
          { timeout: 5000 }
        )
      })

      test('Opiskeluoikeuden luonti onnistuu ja luo uuden opiskeluoikeuden tyhjällä N1-suorituksella', async ({
        uusiOppijaPage,
        eshOppijaPage
      }) => {
        await uusiOppijaPage.fill(oppija)

        await uusiOppijaPage.submitAndExpectSuccess()

        await expect(eshOppijaPage.oppijaHeading).toContainText(
          `${oppija.sukunimi}, ${oppija.etunimet} (${hetu})`
        )
        await expect(eshOppijaPage.hetu).toContainText(`${hetu}`)
        await expect(eshOppijaPage.koulutusmoduuli).toContainText('N1')
        await expect(eshOppijaPage.toimipiste).toContainText(
          `${oppija.oppilaitos}`
        )
      })
    })
  })

  test.describe('Kansalaisen näkymä', () => {
    test.beforeAll(async ({ browser }, testInfo) => {
      // Pakotetaan fixture-reset käyttämään virkailijan istuntoa
      const virkailijaSessionPath = await getVirkailijaSession(
        testInfo,
        'kalle',
        'kalle'
      )
      const ctx = await browser.newContext({
        storageState: virkailijaSessionPath
      })
      const page = await ctx.newPage()
      await new KoskiFixtures(page).reset()
    })

    test.describe('Saavutettavuus', () => {
      test.use({ storageState: kansalainen('050707A130V') })
      test('Sivulla ei saavutettavuusvirheitä', async ({
        kansalainenPage,
        makeAxeBuilder
      }) => {
        await kansalainenPage.goto()
        const accessibilityScanResults = await makeAxeBuilder().analyze()
        expect(accessibilityScanResults.violations).toEqual([])
      })
    })
  })
})
