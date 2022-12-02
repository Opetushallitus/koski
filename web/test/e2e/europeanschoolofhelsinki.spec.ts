import { test, expect } from './base'

const ESH_OID = '1.2.246.562.24.00000000065'

test.describe('European School of Helsinki', () => {
  test.describe('Virkailijan näkymä', () => {
    test.beforeAll(async ({ fixtures }) => {
      await fixtures.reset(false)
    })

    test.beforeEach(async ({ virkailijaLoginPage, eshOppijaPage }) => {
      await virkailijaLoginPage.apiLoginAsUser('kalle', 'kalle')
      await eshOppijaPage.goto(ESH_OID)
    })

    test('Näyttää oppijan tiedot oikein', async ({ eshOppijaPage }) => {
      await expect(eshOppijaPage.oppijaHeading).toContainText(
        'Eurooppalainen, Emilia (050707A130V)'
      )
      await expect(eshOppijaPage.hetu).toContainText('050707A130V')
      await expect(eshOppijaPage.koulutusmoduuli).toContainText(
        'European Baccalaureate2023'
      )
      await expect(eshOppijaPage.luokka).toContainText('S7 EN')
      await expect(eshOppijaPage.toimipiste).toContainText(
        'Helsingin eurooppalainen koulu'
      )
    })

    test(`Lisää S7-vuosiluokan osasuoritukseen uuden alaosasuorituksen`, async ({
      customPage,
      eshOppijaPage
    }) => {
      await eshOppijaPage.clickSuoritusTabByLabel('S1', 'first')
      await eshOppijaPage.clickSuoritusTabByLabel('S7', 'first')
      await eshOppijaPage.avaaMuokkausnäkymä()

      const osasuoritus = eshOppijaPage.getOsasuoritus('Third language')
      await osasuoritus.laajennaBtn.click()

      const alaosasuoritus = await osasuoritus.lisääOsasuoritus('Year mark')

      await alaosasuoritus.valitseArvosana('7.5')

      await eshOppijaPage.tallenna()
      await customPage.waitForURL(
        new RegExp(`koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=S7$`)
      )
      await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
    })

    test.describe('Primary-vuosiluokan suoritukset', () => {
      const vuosiluokat = ['P1', 'P5']

      vuosiluokat.forEach((vuosiluokka) => {
        test(`Lisää ${vuosiluokka}-vuosiluokan suoritukseen uuden osasuorituksen, jolla ei ole prefillattuja alaosasuorituksia, ja sallii tallentamisen vain kun pakollisissa kentissä ond tiedot syötettynä`, async ({
          customPage,
          eshOppijaPage
        }) => {
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

          expect(await eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await osasuoritus.valitseArvosana('pass')

          expect(await eshOppijaPage.tallennusBtn).toBeEnabled()

          // Syötä osasuorituksen valinnaiset tiedot
          await osasuoritus.laajennaBtn.click()
          await osasuoritus.syötäArviointipäivä('31.7.2007')
          await osasuoritus.lisääArvioija('Pekka Perhonen')
          await osasuoritus.lisääKuvaus('Good job!')

          // Lisää alaosasuorituksia
          const alaosasuoritus1 = await osasuoritus.lisääOsasuoritus(
            'Listening and understanding'
          )

          expect(await eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await alaosasuoritus1.valitseArvosana(/^\+\+\+$/)

          expect(await eshOppijaPage.tallennusBtn).toBeEnabled()

          await alaosasuoritus1.laajennaBtn.click()
          await alaosasuoritus1.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus1.lisääArvioija('Paula Perhonen')

          const alaosasuoritus2 = await osasuoritus.lisääOsasuoritus(
            'Reading and understanding'
          )

          expect(await eshOppijaPage.tallennusBtn).not.toBeEnabled()

          await alaosasuoritus2.valitseArvosana('++++')

          expect(await eshOppijaPage.tallennusBtn).toBeEnabled()

          await alaosasuoritus2.laajennaBtn.click()
          await alaosasuoritus2.syötäArviointipäivä('31.7.2007')
          await alaosasuoritus2.lisääArvioija('Paula Perhonen')

          // Tallenna opiskeluoikeus
          await eshOppijaPage.tallenna()

          await customPage.waitForURL(
            new RegExp(
              `koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=${vuosiluokka}$`
            )
          )
          await expect(eshOppijaPage.muokkausNäkymäBtn).toBeVisible()
        })
      })

      test(`Poista P3-vuosiluokalta kaikki osasuoritukset ja lisää prefillattu lapsioppimisalue ja prefillattu oppiaine`, async ({
        customPage,
        eshOppijaPage
      }) => {
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

        await expect(customPage).toHaveURL(
          new RegExp(
            `koski\\/oppija\\/1\\.2\\..*\\?1\\.2\\..*\\.suoritus=${vuosiluokka}$`
          ),
          { timeout: 20000 }
        )
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
        ).toHaveText([
          'Eronnut',
          'Läsnä',
          'Valmistunut',
          'Väliaikaisesti keskeytynyt'
        ])
      })

      test('Opiskeluoikeuden luonti onnistuu ja luo uuden opiskeluoikeuden tyhjällä EB-tutkinnon suorituksella', async ({
        uusiOppijaPage,
        eshOppijaPage
      }) => {
        await uusiOppijaPage.fill(oppija)

        await uusiOppijaPage.submitAndExpectSuccess()

        await expect(eshOppijaPage.oppijaHeading).toContainText(
          `${oppija.sukunimi}, ${oppija.etunimet} (${hetu})`
        )
        await expect(eshOppijaPage.hetu).toContainText(`${hetu}`)
        await expect(eshOppijaPage.koulutusmoduuli).toContainText(
          'European Baccalaureate2023'
        )
        await expect(eshOppijaPage.toimipiste).toContainText(
          `${oppija.oppilaitos}`
        )
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
