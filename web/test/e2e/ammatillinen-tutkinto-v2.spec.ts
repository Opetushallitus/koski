import { expect, test } from './base'
import { getVirkailijaSession, kansalainen, virkailija } from './setup/auth'
import { KoskiFixtures } from './fixtures/KoskiFixtures'
import { Page } from '@playwright/test'

// Ammatillinen tutkinto (koko tutkinto) renderöidään uudella käyttöliittymällä
// toistaiseksi vain feature flagin kanssa, ks. useUiAdapter.
const FEATURE_FLAG = 'ammatillinen-tutkinto-v2'
const flag = { [FEATURE_FLAG]: 'true' }

// Luonto- ja ympäristöalan perustutkinto, ops, valmis
const ammattilainen = '1.2.246.562.24.00000000020'
// Luonto- ja ympäristöalan perustutkinto, ops, kesken
const amis = '1.2.246.562.24.00000000026'
// Näyttötutkintoon valmistava koulutus + Autoalan työnjohdon erikoisammattitutkinto
const erikoinen = '1.2.246.562.24.00000000053'
// Automekaanikon erikoisammattitutkinto, reformi, kesken (peruste ilman ryhmiä)
const reformi = '1.2.246.562.24.00000000054'
// Autoalan perustutkinto, näyttö, kesken
const tunnustettu = '1.2.246.562.24.00000000057'
// Ajoneuvoalan perustutkinto, reformi, kesken
const autonen = '1.2.246.562.24.00000000137'
// Kaksi opiskeluoikeutta, joista jälkimmäisessä pelkkä näyttötutkintoon valmistava koulutus
const paallekkaisia = '1.2.246.562.24.00000000104'

const suoritus = (page: Page, testId: string) =>
  page.getByTestId(`oo.0.suoritukset.0.${testId}`)

const osanNimi = (page: Page, nimi: string) =>
  page.locator('.OsasuoritusRow__cellContent').getByText(nimi, { exact: true })

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Ilman feature flagia käytetään vanhaa käyttöliittymää', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(ammattilainen)
    await expect(page.locator('.opiskeluoikeuden-tiedot')).toBeVisible()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toHaveCount(0)
  })

  test.describe('Katselu', () => {
    test('Näyttää perustutkinnon tiedot, tutkinnon osat ja ryhmien laajuudet', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(ammattilainen, flag)

      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
        'Stadin ammatti- ja aikuisopisto, luonto- ja ympäristöalan perustutkinto'
      )
      await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
        'Luonto- ja ympäristöalan perustutkinto'
      )
      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Luonto- ja ympäristöalan perustutkinto'
      )
      await expect(suoritus(page, 'peruste.value')).toHaveText('62/011/2014')
      await expect(suoritus(page, 'suoritustapa')).toHaveText(
        'Ammatillinen perustutkinto'
      )
      await expect(suoritus(page, '0.osaamisala')).toHaveText(
        'Ympäristöalan osaamisala'
      )
      await expect(suoritus(page, 'suorituskieli.value')).toHaveText('suomi')
      await expect(suoritus(page, 'painotettu-keskiarvo.value')).toHaveText(
        '4,00'
      )
      await expect(
        suoritus(page, 'suorituksenVahvistus.value.details')
      ).toContainText('Vahvistus: 31.5.2016 Helsinki')
      // Vain tutkinnon osan/osien suorituksella olevia kenttiä ei näytetä
      await expect(page.getByText('Toinen tutkintonimike')).toHaveCount(0)

      // Laajuus ryhmittäin: hyväksytysti arvioidut / perusteen vaatimus
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('135 / 135 osp')
      await expect(suoritus(page, 'yhteensa.2')).toHaveText('38 / 35 osp')
      await expect(suoritus(page, 'yhteensa.3')).toHaveText('5 / 10 osp')
      await expect(suoritus(page, 'yhteensa.4')).toHaveText('5 osp')

      await oppijaPageV2.openAllOsasuoritukset()
      await expect(
        suoritus(page, 'osasuoritukset.0.properties.toimipiste')
      ).toHaveText(
        'Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka'
      )
      await expect(
        suoritus(page, 'osasuoritukset.0.properties.vahvistus')
      ).toHaveText('31.5.2016 Reijo Reksi, rehtori')
      await expect(
        suoritus(page, 'osasuoritukset.0.properties.arviointi.0.arvosana')
      ).toHaveText('3')
    })

    test('Jakson päivämäärät näytetään vain annetuilta osin', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)

      // Hojks ja osaamisala ilman päivämääriä: ei irrallisia viivoja
      const hojks = page.getByTestId(
        'oo.0.opiskeluoikeus.lisätiedot.opetusryhmä'
      )
      await expect(hojks.locator('..')).toHaveText('Yleinen opetusryhmä')
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.alku')
      ).toHaveCount(0)
      await expect(suoritus(page, '0.osaamisala').locator('..')).toHaveText(
        'Ympäristöalan osaamisala'
      )

      // Avoin jakso näytetään muodossa "alku —"
      await expect(
        page.locator('.KeyValueRow', { hasText: 'Vankilaopetuksessa' })
      ).toHaveText(/Vankilaopetuksessa\s*2\.9\.2013 —\s*$/)
    })

    test('Näyttötutkinnolla ei ole keskiarvoa eikä perusteen ryhmittelyä', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.gotoWithQueryParams(tunnustettu, flag)

      await expect(suoritus(page, 'suoritustapa')).toHaveText('Näyttötutkinto')
      await expect(suoritus(page, 'yhteensa')).toHaveText('15 osp')
      await expect(page.getByText('Painotettu keskiarvo')).toHaveCount(0)

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await expect(
        suoritus(page, 'painotettu-keskiarvo.edit.input')
      ).toHaveCount(0)
    })

    test('Näyttää näyttötutkintoon valmistavan koulutuksen ja tutkinnon omilla välilehdillään', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(erikoinen, flag)

      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
        'Stadin ammatti- ja aikuisopisto, näyttötutkintoon valmistava koulutus'
      )
      await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
        'Näyttötutkintoon valmistava koulutus'
      )
      await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toHaveText(
        'Autoalan työnjohdon erikoisammattitutkinto'
      )

      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Näyttötutkintoon valmistava koulutus'
      )
      await expect(suoritus(page, 'tutkinto')).toHaveText(
        'Autoalan työnjohdon erikoisammattitutkinto'
      )
      await expect(suoritus(page, 'peruste.value')).toHaveText('40/011/2001')
      await expect(suoritus(page, 'alkamispäivä.value')).toHaveText('1.9.2012')
      await expect(suoritus(page, 'päättymispäivä.value')).toHaveText(
        '31.5.2015'
      )
      await expect(
        suoritus(page, 'suorituksenVahvistus.value.details')
      ).toContainText('Vahvistus: 31.5.2015 Helsinki')

      await oppijaPageV2.openAllOsasuoritukset()
      await expect(
        suoritus(page, 'osasuoritukset.1.properties.kuvaus.value')
      ).toHaveText('valojärjestelmät')

      await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
      await expect(
        page.getByTestId('oo.0.suoritukset.1.suoritustapa')
      ).toHaveText('Näyttötutkinto')
      await expect(page.getByTestId('oo.0.suoritukset.1.yhteensa')).toHaveText(
        '0 osp'
      )
    })
  })

  test.describe('Muokkaus', () => {
    test('Laajuuteen lasketaan vain hyväksytysti arvioidut tutkinnon osat', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('90 / 135 osp')

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      // Perusteen mukaan tämän tutkinnon osan laajuus on vähintään 15 osp
      await suoritus(page, 'osasuoritukset.3.laajuus.edit.input').fill('15')
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('90 / 135 osp')

      await suoritus(page, 'osasuoritukset.3.arvosana.input').click()
      await suoritus(
        page,
        'osasuoritukset.3.arvosana.options.Arviointiasteikko ammatillinen T1-K3.arviointiasteikkoammatillinent1k3_2.item'
      ).click()
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('105 / 135 osp')

      await oppijaPageV2.tallenna()
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('105 / 135 osp')
      await expect(suoritus(page, 'osasuoritukset.3.laajuus.value')).toHaveText(
        '15 osp'
      )
    })

    test('Yhteisen tutkinnon osan ja sen osa-alueen lisääminen', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      await suoritus(page, 'uusi-yhteinen-tutkinnonosa.input').click()
      await suoritus(
        page,
        'uusi-yhteinen-tutkinnonosa.options.101053.item'
      ).click()
      await suoritus(
        page,
        'osasuoritukset.6.properties.uusi-yhteinen-osan-osa-alue.input'
      ).click()
      await suoritus(
        page,
        'osasuoritukset.6.properties.uusi-yhteinen-osan-osa-alue.options.AI.item'
      ).click()
      await suoritus(
        page,
        'osasuoritukset.6.properties.osasuoritukset.0.properties.arviointi.lisää-arviointi'
      ).click()

      await oppijaPageV2.tallenna()
      await expect(
        osanNimi(page, 'Viestintä- ja vuorovaikutusosaaminen')
      ).toBeVisible()
      await expect(suoritus(page, 'yhteensa.2')).toHaveText('0 / 35 osp')
    })

    test('Vapaasti valittavan paikallisen tutkinnon osan lisääminen', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Painikkeet ovat ryhmien järjestyksessä: ammatilliset, vapaasti valittavat, ...
      await page
        .getByRole('button', { name: 'Lisää paikallinen tutkinnon osa' })
        .nth(1)
        .click()
      await page.locator('.Modal input').fill('Hassut temput')
      await page
        .locator('.Modal')
        .getByRole('button', { name: 'Lisää tutkinnon osa' })
        .click()

      await oppijaPageV2.tallenna()
      await expect(osanNimi(page, 'Hassut temput')).toBeVisible()
    })

    test('Tutkinnon osan lisääminen toisesta tutkinnosta', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      await page
        .getByRole('button', {
          name: 'Lisää tutkinnon osa toisesta tutkinnosta'
        })
        .nth(1)
        .click()
      await suoritus(page, 'modal.tutkinto.input').fill('auto')
      await suoritus(
        page,
        'modal.tutkinto.options.351301_39/011/2014.item'
      ).click()
      await suoritus(
        page,
        'modal.uusi-muu-tutkinnonosa-toisesta-tutkinnosta.input'
      ).click()
      await suoritus(
        page,
        'modal.uusi-muu-tutkinnonosa-toisesta-tutkinnosta.options.100021.item'
      ).click()
      await suoritus(page, 'modal.confirm').click()

      await oppijaPageV2.tallenna()
      await expect(osanNimi(page, 'Ruiskumaalaustyöt')).toBeVisible()
      // Lisätty rivi on edelleen auki
      await expect(
        suoritus(page, 'osasuoritukset.6.properties.tutkintoNimi')
      ).toHaveText('Autoalan perustutkinto')
    })

    test('Osaamisen tunnustamisen lisääminen tutkinnon osalle', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await suoritus(page, 'osasuoritukset.0.expand').click()
      await suoritus(
        page,
        'osasuoritukset.0.properties.tunnustettu.edit.add'
      ).click()
      await suoritus(
        page,
        'osasuoritukset.0.properties.tunnustettu.edit.selite.input'
      ).fill('Tunnustettu aiemmasta tutkinnosta')

      await oppijaPageV2.tallenna()
      await expect(
        suoritus(page, 'osasuoritukset.0.properties.tunnustettu.selite')
      ).toHaveText('Tunnustettu aiemmasta tutkinnosta')
    })

    test('Keskiarvo tallennetaan ja näytetään kahden desimaalin tarkkuudella', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      // Keskiarvon voi antaa vain valmiille suoritukselle
      await oppijaPage.gotoWithQueryParams(ammattilainen, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await suoritus(page, 'painotettu-keskiarvo.edit.input').fill('3.5')

      await oppijaPageV2.tallenna()
      await expect(suoritus(page, 'painotettu-keskiarvo.value')).toHaveText(
        '3,50'
      )
    })

    test('Osaamisalaksi tarjotaan perusteen osaamisalat', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.gotoWithQueryParams(amis, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await suoritus(page, '0.osaamisala.input').click()

      const vaihtoehdot = suoritus(page, '0.osaamisala.options').locator(
        '[data-testid$=".item"]'
      )
      await expect(vaihtoehdot).toHaveCount(3)
      expect((await vaihtoehdot.allTextContents()).sort()).toEqual([
        'Luontoalan osaamisala',
        'Porotalouden osaamisala',
        'Ympäristöalan osaamisala'
      ])
    })

    test('Ryhmittelemättömän perusteen tutkinnon osa tallentuu ilman ryhmää', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(reformi, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      // Korkeakouluopintoja ja jatko-opintovalmiuksia tukevia opintoja voi
      // lisätä vain ammatillisiin tutkinnon osiin, joita ei tässä perusteessa ole
      await expect(suoritus(page, 'uusi-korkeakouluopinto')).toHaveCount(0)

      await suoritus(page, 'uusi-muu-tutkinnonosa.input').click()
      await suoritus(page, 'uusi-muu-tutkinnonosa.options.300050.item').click()

      // Backend hylkää ryhmän muulle kuin ammatillisen perustutkinnon osalle
      await oppijaPageV2.tallenna()
      await expect(osanNimi(page, 'Ajoneuvoalan kehitystehtävä')).toBeVisible()
    })

    test('Korkeakouluopintokokonaisuuden laajuus lasketaan ammatillisiin tutkinnon osiin', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(autonen, flag)
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('11 / 145 osp')
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      await suoritus(page, 'osasuoritukset.3.expand').click()
      await page
        .getByRole('button', { name: 'Lisää korkeakouluopintokokonaisuus' })
        .click()
      await page.locator('.Modal input').fill('Johdatus ohjelmointiin')
      await page
        .locator('.Modal')
        .getByRole('button', { name: 'Lisää' })
        .click()

      const kokonaisuus = 'osasuoritukset.3.properties.osasuoritukset.1'
      await suoritus(page, `${kokonaisuus}.laajuus.edit.input`).fill('5')
      await suoritus(page, `${kokonaisuus}.arvosana.input`).click()
      await page
        .locator(
          `[data-testid^="oo.0.suoritukset.0.${kokonaisuus}.arvosana.options."][data-testid$=".arviointiasteikkoammatillinen15_4.item"]`
        )
        .click()
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('16 / 145 osp')

      await oppijaPageV2.tallenna()
      await expect(suoritus(page, 'yhteensa.1')).toHaveText('16 / 145 osp')
    })

    test('Näyttötutkintoon valmistavan koulutuksen osien lisääminen', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(erikoinen, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      await suoritus(page, 'uusi-muu-tutkinnonosa.input').click()
      await suoritus(page, 'uusi-muu-tutkinnonosa.options.104053.item').click()

      await suoritus(page, 'lisaa-paikallinen-osa').click()
      await page.locator('.Modal input').fill('Hassut temput')
      await page
        .locator('.Modal')
        .getByRole('button', { name: 'Lisää tutkinnon osa' })
        .click()

      await suoritus(page, 'lisaa-osa-toisesta-tutkinnosta').click()
      await suoritus(page, 'modal.tutkinto.input').fill('auto')
      await suoritus(
        page,
        'modal.tutkinto.options.351301_39/011/2014.item'
      ).click()
      await suoritus(
        page,
        'modal.uusi-muu-tutkinnonosa-toisesta-tutkinnosta.input'
      ).click()
      await suoritus(
        page,
        'modal.uusi-muu-tutkinnonosa-toisesta-tutkinnosta.options.100021.item'
      ).click()
      await suoritus(page, 'modal.confirm').click()

      await oppijaPageV2.tallenna()
      for (const nimi of [
        'Asiakaspalvelu ja korjaamopalvelujen markkinointi',
        'Hassut temput',
        'Ruiskumaalaustyöt'
      ]) {
        await expect(osanNimi(page, nimi)).toBeVisible()
      }
    })

    test('Näyttötutkintoon valmistavan koulutuksen suorituksen lisääminen ja poistaminen', async ({
      page,
      oppijaPage,
      oppijaPageV2
    }) => {
      await oppijaPage.gotoWithQueryParams(tunnustettu, flag)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      await page
        .getByText('lisää näyttötutkintoon valmistavan koulutuksen suoritus')
        .click()
      await expect(page.getByTestId('oo.0.suoritukset.1.tutkinto')).toHaveText(
        'Autoalan perustutkinto'
      )

      await oppijaPageV2.tallenna()
      await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toHaveText(
        'Näyttötutkintoon valmistava koulutus'
      )

      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
      await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
      await page.getByTestId('oo.0.suoritukset.1.button').click()
      await page.getByTestId('oo.0.suoritukset.1.confirm').click()

      // Poisto tallentuu heti, joten suoritus on poissa myös uudelleenlatauksen jälkeen
      await expect(
        page.getByRole('button', {
          name: 'Näyttötutkintoon valmistava koulutus'
        })
      ).toHaveCount(0)
      await oppijaPage.gotoWithQueryParams(tunnustettu, flag)
      await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
        'Autoalan perustutkinto'
      )
      await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toHaveCount(0)
    })

    test('Ammatillisen tutkinnon suorituksen lisääminen näyttötutkintoon valmistavalle koulutukselle', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.gotoWithQueryParams(paallekkaisia, flag)
      // Oppijalla on kaksi opiskeluoikeutta, joista jälkimmäinen on pelkkä valmistava koulutus
      await page.getByTestId('oo.0.opiskeluoikeus.edit').nth(1).click()
      await page.getByText('lisää ammatillisen tutkinnon suoritus').click()

      await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toHaveText(
        'Autoalan työnjohdon erikoisammattitutkinto'
      )
      await expect(
        page.getByTestId('oo.0.suoritukset.1.suoritustapa')
      ).toHaveText('Näyttötutkinto')
    })
  })
})

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä kansalaiselle', () => {
  test.beforeAll(async ({ browser }, testInfo) => {
    const virkailijaSessionPath = await getVirkailijaSession(
      testInfo,
      'kalle',
      'kalle'
    )
    const ctx = await browser.newContext({
      storageState: virkailijaSessionPath
    })
    await new KoskiFixtures(await ctx.newPage()).reset()
  })
  test.use({ storageState: kansalainen('280618-402H') })

  test.beforeEach(async ({ page }) => {
    await page.addInitScript((flagName) => {
      window.localStorage.setItem(flagName, 'true')
    }, FEATURE_FLAG)
  })

  test('Näyttää tutkinnon tiedot ja ryhmien laajuudet', async ({
    page,
    kansalainenPage
  }) => {
    await kansalainenPage.goto()
    await page.locator('.OpiskeluoikeusTitle__expand').first().click()

    await expect(suoritus(page, 'koulutus')).toHaveText(
      'Luonto- ja ympäristöalan perustutkinto'
    )
    await expect(suoritus(page, 'yhteensa.1')).toHaveText('135 / 135 osp')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
  })

  test('Sivulla ei saavutettavuusvirheitä', async ({
    page,
    kansalainenPage,
    makeAxeBuilder
  }) => {
    await kansalainenPage.goto()
    await page.locator('.OpiskeluoikeusTitle__expand').first().click()
    await expect(suoritus(page, 'koulutus')).toBeVisible()
    const accessibilityScanResults = await makeAxeBuilder().analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
