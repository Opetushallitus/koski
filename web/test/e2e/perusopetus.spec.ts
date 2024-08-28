import { test, expect } from './base'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija, virkailijaPath } from './setup/auth'

test.describe('Perusopetus', () => {
  // Palautetaan virkailijan istunto
  test.use({ storageState: virkailija('kalle') })
  test.describe('Perusopetuksen lukuvuositodistukset ja päättötodistus', async () => {
    test.beforeEach(
      async ({ virkailijaPage, oppijaHaku, perusopetusOppijaPage }) => {
        await virkailijaPage.goto()
        const result = await oppijaHaku.search('220109-784L')
        await result.clickOnFirst()
        await perusopetusOppijaPage.selectOpiskeluoikeus('perusopetus')
      }
    )
    test('Näyttää opiskeluoikeuden tiedot', async ({
      perusopetusOppijaPage
    }) => {
      await expect(perusopetusOppijaPage.opiskeluoikeudenTiedot).toContainText(
        'Opiskeluoikeuden voimassaoloaika: 15.8.2008 — 4.6.2016 Tila4.6.2016Valmistunut15.8.2008Läsnä'
      )
      await expect(
        perusopetusOppijaPage.opiskeluoikeudetNavValittuVälilehti
      ).toContainText(['Perusopetuksen oppimäärä 2008—2016, Valmistunut'])
    })

    test.describe('Perusopetuksen oppimäärä', async () => {
      test.describe('Kaikki tiedot näkyvissä', async () => {
        test('Näyttää suorituksen tiedot', async ({
          perusopetusOppijaPage
        }) => {
          await expect(
            perusopetusOppijaPage.page.locator('.suoritus > .properties')
          ).toContainText(
            'KoulutusPerusopetus201101104/011/2014Oppilaitos / toimipisteJyväskylän normaalikouluSuoritustapaKoulutusSuorituskielisuomiKoulusivistyskielisuomi'
          )
          await expect(
            perusopetusOppijaPage.page.locator('.suoritus > .tila-vahvistus')
          ).toContainText(
            'Suoritus valmisVahvistus: 4.6.2016 Jyväskylä Reijo Reksi, rehtori'
          )
        })
        test('Näyttää oppiaineiden arvosanat', async ({
          perusopetusOppijaPage
        }) => {
          await expect(
            perusopetusOppijaPage.page.getByTestId(
              'perusopetuksen-arvosteluasteikko'
            )
          ).toContainText('Arvostelu 4-10, S (suoritettu) tai H (hylätty)')
          await expect(
            perusopetusOppijaPage.page
              .getByTestId('oppiaine-taulukko')
              .getByTestId('oppiaine-row')
              .getByTestId('oppiaine')
          ).toContainText([
            'Äidinkieli ja kirjallisuus, Suomen kieli ja kirjallisuus',
            'B1-kieli, ruotsi',
            'A1-kieli, englanti',
            'Uskonto/Elämänkatsomustieto',
            'Historia',
            'Yhteiskuntaoppi',
            'Matematiikka',
            'Kemia',
            'Fysiikka',
            'Biologia',
            'Maantieto',
            'Musiikki',
            'Kuvataide',
            'Kotitalous',
            'Terveystieto',
            'Käsityö',
            'Liikunta',
            'B1-kieli, ruotsi',
            'Kotitalous',
            'Liikunta',
            'B2-kieli',
            'Tietokoneen hyötykäyttö'
          ])
          await expect(
            perusopetusOppijaPage.page
              .getByTestId('oppiaine-taulukko')
              .getByTestId('oppiaine-row')
              .getByTestId('oppiaine-row-arvosana')
          ).toContainText([
            '9',
            '8',
            '8',
            '10',
            '8',
            '10',
            '9',
            '7',
            '9',
            '9',
            '9',
            '7',
            '8',
            '8',
            '8',
            '9',
            '9',
            'S',
            'S',
            'S',
            '9',
            '9'
          ])
        })
      })
    })
  })
  test.describe('Opiskeluoikeuden lisääminen', async () => {
    test.beforeEach(async ({ browser, virkailijaPage, oppijaHaku }) => {
      const ctx = await browser.newContext({
        storageState: virkailijaPath('kalle')
      })
      const page = await ctx.newPage()
      const fixtures = new KoskiFixtures(page)
      await fixtures.reset()
      const uusiOppijaPage = new KoskiUusiOppijaPage(page)
      await uusiOppijaPage.goTo('230872-7258')
      await uusiOppijaPage.fill({
        etunimet: 'Tero',
        sukunimi: 'Tyhjä',
        oppilaitos: 'Jyväskylän normaalikoulu',
        opiskeluoikeus: 'Aikuisten perusopetus',
        suorituskieli: 'suomi',
        oppimäärä: 'Aikuisten perusopetuksen oppimäärän alkuvaihe',
        peruste:
          'OPH-1280-2017 Aikuisten perusopetuksen opetussuunnitelman perusteet',
        aloituspäivä: new Date(2018, 1, 1),
        rahoitus: 'Valtionosuusrahoitteinen koulutus',
        opintojenMaksuttomuus: 'eiOvlPiirissä'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
      await page.close()
      await virkailijaPage.goto()
      const search = await oppijaHaku.search('230872-7258')
      await search.clickOnFirst()
    })
    test.describe('Aikuisten perusopetuksen alkuvaihe', async () => {
      test('Näytetään aikuisten perusopetuksen oppimäärän alkuvaihe opiskeluoikeuden otsikossa', async ({
        page
      }) => {
        await expect(page.locator('.opiskeluoikeus h3 .koulutus')).toHaveText(
          'Aikuisten perusopetuksen oppimäärän alkuvaihe'
        )
      })
    })
    test.describe('Päättövaiheen opinnot', async () => {
      test.beforeEach(async ({ perusopetusOppijaPage, page }) => {
        await perusopetusOppijaPage.avaaMuokkausnäkymä()
        await page.waitForLoadState('networkidle')
        await page
          .getByRole('button', {
            name: 'lisää opintojen päättövaiheen suoritus'
          })
          .click()
        await page.waitForLoadState('networkidle', { timeout: 20000 })
        await expect(
          page.getByRole('button', {
            name: 'lisää opintojen päättövaiheen suoritus'
          })
        ).not.toBeVisible({ timeout: 90000 })
      })
      test.describe('Lisäyksen jälkeen', async () => {
        test('Piilotetaan lisäyslinkki ja esitäytetään pakolliset oppiaineet', async ({
          page
        }) => {
          // Ollaan jo muokkausnäkymässä
          await expect(
            page.locator('.oppiaineet .oppiaine .nimi')
          ).toContainText([
            'Äidinkieli ja kirjallisuus,',
            'A1-kieli,',
            'B1-kieli,',
            'Matematiikka',
            'Biologia',
            'Maantieto',
            'Fysiikka',
            'Kemia',
            'Terveystieto',
            'Uskonto/Elämänkatsomustieto',
            'Historia',
            'Yhteiskuntaoppi',
            'Musiikki',
            'Kuvataide',
            'Käsityö',
            'Liikunta',
            'Kotitalous',
            'Opinto-ohjaus'
          ])

          await expect(
            page.getByTestId('oppiaine-row').first().getByRole('combobox', {
              name: 'Suomen kieli ja kirjallisuus'
            })
          ).toBeVisible()
        })
      })

      test.describe('Tallennuksen jälkeen', function () {
        test('Näytetään aikuisten perusopetuksen oppimäärä opiskeluoikeuden otsikossa, kun päättövaiheen suoritus on lisätty', async ({
          page,
          perusopetusOppijaPage
        }) => {
          // Ollaan jo muokkausnäkymässä
          await perusopetusOppijaPage.tallenna()
          await expect(perusopetusOppijaPage.suoritusTabs).toContainText([
            ' Aikuisten perusopetuksen oppimäärän alkuvaihe'
          ])
          await expect(page.locator('.opiskeluoikeus h3 .koulutus')).toHaveText(
            'Aikuisten perusopetuksen oppimäärä'
          )
        })
      })
    })
  })
})
