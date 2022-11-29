import { test, expect } from './base'

const ESH_OID = '1.2.246.562.24.00000000065'

test.describe('European School of Helsinki', () => {
  test.describe('Virkailijan näkymä', () => {
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset(false)
    })

    test.beforeEach(async ({ virkailijaLoginPage, oppijaPage }) => {
      await virkailijaLoginPage.apiLoginAsUser('kalle', 'kalle')
      await oppijaPage.goto(ESH_OID)
    })

    test('Näyttää oppijan tiedot oikein', async ({ oppijaPage }) => {
      await expect(oppijaPage.oppijaHeading).toContainText(
        'Eurooppalainen, Emilia (050707A130V)'
      )
      await expect(oppijaPage.hetu).toContainText('050707A130V')
      await expect(oppijaPage.koulutusmoduuli).toContainText(
        'European Baccalaureate2023'
      )
      await expect(oppijaPage.luokka).toContainText('S7 EN')
      await expect(oppijaPage.toimipiste).toContainText(
        'Helsingin eurooppalainen koulu'
      )
    })

    test(`Lisää S7-vuosiluokan osasuoritukseen uuden alaosasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('S1', 'first')
      await oppijaPage.clickSuoritusTabByLabel('S7', 'first')
      await oppijaPage.avaaMuokkausnäkymä()

      await customPage
        .getByRole('button', {
          name: 'Laajenna suoritus Information and Communication Technology',
          expanded: false
        })
        .click()

      await customPage
        .getByRole('combobox', { name: 'Lisää alaosasuoritus' })
        .click()
      await customPage.getByRole('listitem', { name: 'Year mark' }).click()
    })

    test.describe('Primary-vuosiluokan suoritukset', () => {
      const vuosiluokat = ['P1', 'P5']

      vuosiluokat.forEach((vuosiluokka) => {
        test(`Lisää ${vuosiluokka}-vuosiluokan suoritukseen uuden osasuorituksen, jolla ei ole prefillattuja alaosasuorituksia`, async ({
          customPage,
          oppijaPage
        }) => {
          await oppijaPage.clickSuoritusTabByLabel(vuosiluokka, 'first')
          await oppijaPage.avaaMuokkausnäkymä()

          // Lisää osasuoritus
          const osasuoritus = await oppijaPage.lisääESHOsasuoritus(
            'Advanced studies of the second language'
          )

          // Syötä osasuorituksen pakolliset tiedot
          await osasuoritus.valitseKieli('englanti')
          await osasuoritus.valitseSuorituskieli('englanti')
          await osasuoritus.syötäLaajuus('4')
          await osasuoritus.valitseArvosana('pass')

          // Syötä osasuorituksen valinnaiset tiedot
          await osasuoritus.laajennaBtn.click()
          await osasuoritus.syötäArviointipäivä('31.7.2007')
          await osasuoritus.lisääArvioija('Pekka Perhonen')
          await osasuoritus.lisääKuvaus('Good job!')

          // Lisää alaosasuorituksia
          const alaosasuoritus1 = await osasuoritus.lisääOsasuoritus(
            'Listening and understanding'
          )
          await alaosasuoritus1.valitseArvosana(/^\+\+\+$/)
          await alaosasuoritus1.laajennaBtn.click()
          await alaosasuoritus1.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus1.lisääArvioija('Paula Perhonen')

          const alaosasuoritus2 = await osasuoritus.lisääOsasuoritus(
            'Reading and understanding'
          )
          await alaosasuoritus2.valitseArvosana('++++')
          await alaosasuoritus2.laajennaBtn.click()
          await alaosasuoritus2.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus2.lisääArvioija('Paula Perhonen')

          // Tallenna opiskeluoikeus
          await oppijaPage.tallenna()

          await expect(customPage).toHaveURL(
            new RegExp(
              `koski\/oppija\/1\.2\..*\?1\.2\..*\.suoritus=${vuosiluokka}$`
            )
          )
          await expect(oppijaPage.muokkausNäkymäBtn).toBeVisible()
        })
      })
    })

    test.describe('Luodessa opiskeluoikeutta', async () => {
      test(`Autofillaa S1-vuosiluokan osasuoritukset oikein`, async ({
        uusiOppijaPage
      }) => {
        // Satunnaisesti generoitu hetu
        await uusiOppijaPage.lisaaOppija({
          hetu: '110363-155S',
          aloituspäivä: new Date(),
          curriculum: '2023',
          etunimet: '',
          luokkaAste: '',
          opiskeluoikeudenTila: '',
          opiskeluoikeus: 'European School of Helsinki',
          oppilaitos: '',
          sukunimi: '',
          suorituskieli: ''
        })
      })
    })
  })
  test.describe('Kansalaisen näkymä', () => {
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset(false)
    })

    test.beforeEach(async ({ kansalainenLoginPage }) => {
      await kansalainenLoginPage.loginWithHetu('050707A130V')
    })

    test('Sivulla ei saavutettavuusvirheitä', async ({
      kansalainenPage,
      makeAxeBuilder
    }) => {
      await kansalainenPage.goto()
      const accessibilityScanResults = await makeAxeBuilder().analyze()
      expect(accessibilityScanResults.violations).toEqual([])
    })

    test.afterEach(async ({ customPage }) => {
      // Tämä pitää muistaa kutsua jokaisen testin päätteeksi, jotta saman kontekstin jakava sivuobjekti sulkeutuu oikein. Muuten selainikkunat jäävät päälle.
      await customPage.close()
    })
  })
})
