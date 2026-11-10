import { Page } from '@playwright/test'
import { expect, test } from './base'
import { formatFinnishDate } from '../../app/date/date'
import { luoAmmatillinenTutkinto } from './fragments/ammatillinenTutkintoLuonti'
import { virkailija } from './setup/auth'

// Henkilöpalvelusta löytyvä henkilö, jolla ei ole opiskeluoikeuksia
const eiKoskessa = '280608-6619'
// Autoalan perustutkinto ilman osasuorituksia; luottamuksellisia lisätietoja
const esimerkki = '1.2.246.562.24.00000000001'
// Ammatillinen opiskeluoikeus lähdejärjestelmästä, kesken
const tiedonsiirto = '1.2.246.562.24.00000000058'
const tiedonsiirtoAmmatillinen = `${tiedonsiirto}?opiskeluoikeudenTyyppi=ammatillinenkoulutus`
// Peruste puuttuu ePerusteista
const eiPerusteissa = '1.2.246.562.24.00000000025'
// Luonto- ja ympäristöalan perustutkinto, ops, kesken
const amis = '1.2.246.562.24.00000000026'
// Luonto- ja ympäristöalan perustutkinto, ops, valmis
const ammattilainen = '1.2.246.562.24.00000000020'
// Autoalan perustutkinto, näyttö, kesken; vanhan perusteen suoritus tunnustettu
const tunnustettu = '1.2.246.562.24.00000000057'
// Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu
const historoitsija = '1.2.246.562.24.00000000073'

const suoritus = (page: Page, testId: string) =>
  page.getByTestId(`oo.0.suoritukset.0.${testId}`)

const osa = (page: Page, testId: string) =>
  suoritus(page, `osasuoritukset.0.${testId}`)

const osanOminaisuus = (page: Page, testId: string) =>
  osa(page, `properties.${testId}`)

const muokkaa = async (page: Page) => {
  await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
  await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
}

// Rivit voivat olla sisäkkäin, joten viimeinen osuma on otsikon oma rivi
const rivi = (page: Page, otsikko: string) =>
  page
    .locator('.KeyValueRow', {
      has: page.getByText(otsikko, { exact: true })
    })
    .last()

const lisätietoRivi = (page: Page, otsikko: string) =>
  page.locator('.EditorContainer__lisatiedot > .KeyValueTable > .KeyValueRow', {
    has: page.getByText(otsikko, { exact: true })
  })

const ominaisuus = (page: Page, otsikko: string) =>
  page.locator('.OsasuoritusProperty', {
    has: page.getByText(otsikko, { exact: true })
  })

// Huolto- ja korjaustyöt
const lisääAmmatillinenOsa = async (page: Page) => {
  await suoritus(page, 'uusi-muu-tutkinnonosa.input').first().click()
  await suoritus(page, 'uusi-muu-tutkinnonosa.options.100016.item')
    .first()
    .click()
}

const osaRivi = (page: Page, nimi: string) =>
  page.locator('.OsasuoritusRow', {
    has: page
      .locator('.OsasuoritusRow__cellContent')
      .getByText(nimi, { exact: true })
  })

const T1K3 =
  'Arviointiasteikko ammatillinen T1-K3.arviointiasteikkoammatillinent1k3'
const ASTEIKKO_1_5 =
  'Arviointiasteikko ammatillinen 1-5.arviointiasteikkoammatillinen15'

const valitseArvosana = async (
  page: Page,
  kenttä: string,
  asteikko: string,
  arvosana: string
) => {
  await suoritus(page, `${kenttä}.input`).click()
  await suoritus(page, `${kenttä}.options.${asteikko}_${arvosana}.item`).click()
}

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: mitätöinti', () => {
  const mitätöintilinkki = (page: Page) =>
    page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')

  const oppijaTaulukossa = async (page: Page, näkyy: boolean) => {
    await page.goto('/koski/virkailija?nimihaku=Esimerkki')
    const rivit = page.locator('.oppijataulukko tbody tr td.nimi')
    await expect(page.getByTestId('oppijataulukko')).toBeVisible()
    await expect(page.locator('.oppijataulukko tbody.loading')).toHaveCount(0)
    if (näkyy) {
      await expect(rivit).toHaveText(['Esimerkki, Eero'])
    } else {
      await expect(rivit).toHaveCount(0)
    }
  }

  const mitätöiJaTarkistaKatoaminen = async (page: Page) => {
    await mitätöintilinkki(page).click()
    await page.getByTestId('oo.0.opiskeluoikeus.invalidate.confirm').click()
    await expect(page).toHaveURL(/\/koski\/virkailija/)
    await expect(page.getByTestId('oppijataulukko')).toBeVisible()

    const sync = await page.request.post('/koski/fixtures/sync-perustiedot')
    expect(sync.ok()).toBeTruthy()
    await oppijaTaulukossa(page, false)
  }

  test.describe('Tallentajana', () => {
    test.use({ storageState: virkailija('kalle') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('Mitätöintilinkki näytetään muokkaustilassa, vahvistuksen jälkeen opiskeluoikeus katoaa', async ({
      page,
      oppijaPage
    }) => {
      await oppijaTaulukossa(page, true)

      await oppijaPage.goto(esimerkki)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
      await expect(mitätöintilinkki(page)).toHaveCount(0)
      await muokkaa(page)
      await expect(mitätöintilinkki(page)).toBeVisible()

      await mitätöintilinkki(page).click()
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.invalidate.confirm')
      ).toBeVisible()
      await page.getByTestId('oo.0.opiskeluoikeus.invalidate.cancel').click()
      await mitätöiJaTarkistaKatoaminen(page)
    })

    test('Lähdejärjestelmästä tulleelle keskeneräiselle opiskeluoikeudelle ei näytetä mitätöintilinkkiä', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(tiedonsiirtoAmmatillinen)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
      await expect(mitätöintilinkki(page)).toHaveCount(0)
    })
  })

  test.describe('Oppilaitoksen pääkäyttäjänä', () => {
    test.use({ storageState: virkailija('stadin-pää') })

    test.beforeEach(async ({ fixtures }) => {
      await fixtures.reset()
    })

    test('Mitätöintilinkki näytetään katselutilassa, vahvistuksen jälkeen opiskeluoikeus katoaa', async ({
      page,
      oppijaPage
    }) => {
      await oppijaTaulukossa(page, true)

      await oppijaPage.goto(esimerkki)
      await expect(mitätöintilinkki(page)).toBeVisible()
      await mitätöiJaTarkistaKatoaminen(page)
    })

    test('Lähdejärjestelmästä tulleelle keskeneräiselle opiskeluoikeudelle näytetään mitätöintilinkki', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(tiedonsiirtoAmmatillinen)
      await expect(mitätöintilinkki(page)).toBeVisible()
    })
  })
})

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: tietojen muokkaus', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.describe('Opiskeluoikeuden tiedot', () => {
    test('Järjestämismuoto validilla y-tunnuksella', async ({
      page,
      uusiOppijaPage,
      oppijaPageV2
    }) => {
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa)
      await muokkaa(page)

      const järjestämismuodot = rivi(page, 'Järjestämismuodot')
      await järjestämismuodot.getByRole('button', { name: 'Lisää' }).click()
      await suoritus(page, '0.alku.input').fill('22.8.2017')
      await suoritus(page, '0.järjestämismuoto.input').click()
      await suoritus(page, '0.järjestämismuoto.options.20.item').click()
      await rivi(page, 'Yritys').locator('input').fill('Autohuolto oy')
      await rivi(page, 'Y-tunnus').locator('input').fill('1629284-5')
      await oppijaPageV2.tallenna()

      await expect(järjestämismuodot).toContainText('22.8.2017')
      await expect(suoritus(page, '0.järjestämismuoto')).toHaveText(
        'Koulutuksen järjestäminen oppisopimuskoulutuksena'
      )
      await expect(rivi(page, 'Yritys')).toContainText('Autohuolto oy')
      await expect(rivi(page, 'Y-tunnus')).toContainText('1629284-5')
    })

    test('Ostettu-tiedon muuttaminen', async ({
      page,
      uusiOppijaPage,
      oppijaPageV2
    }) => {
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa)
      const ostettu = rivi(page, 'Ostettu')
      await expect(ostettu).toHaveCount(0)

      await muokkaa(page)
      await expect(ostettu).toBeVisible()
      await page.getByTestId('oo.0.opiskeluoikeus.bool.label').click()
      await oppijaPageV2.tallenna()

      await expect(ostettu).toHaveText(/Ostettu\s*Kyllä/)
    })

    test('Lisätietojen tallentaminen', async ({
      page,
      uusiOppijaPage,
      oppijaPageV2
    }) => {
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa)
      await muokkaa(page)
      const lisääRivi = (otsikko: string) =>
        lisätietoRivi(page, otsikko)
          .getByRole('button', { name: 'Lisää' })
          .click()
      const kenttä = (otsikko: string, testId: string) =>
        lisätietoRivi(page, otsikko).getByTestId(
          `oo.0.opiskeluoikeus.lisätiedot.0.${testId}`
        )

      await lisääRivi('Ulkomaanjaksot')
      await kenttä('Ulkomaanjaksot', 'ulkomaanjakso.alku.input').fill(
        '22.6.2017'
      )
      await kenttä('Ulkomaanjaksot', 'ulkomaanjakso.maa.input').fill('Algeria')
      await kenttä(
        'Ulkomaanjaksot',
        'ulkomaanjakso.maa.options.012.item'
      ).click()
      await kenttä('Ulkomaanjaksot', 'ulkomaanjakso.kuvaus.input').fill(
        'Testing'
      )

      await lisääRivi('Majoitus')
      await kenttä('Majoitus', 'aikajakso.alku.input').fill('22.6.2017')
      await kenttä('Majoitus', 'aikajakso.loppu.input').fill('1.1.2099')

      await lisääRivi('Osa-aikaisuusjaksot')
      await kenttä('Osa-aikaisuusjaksot', 'alku.input').fill('22.6.2017')
      await lisätietoRivi(page, 'Osa-aikaisuusjaksot')
        .locator('input[type="number"]')
        .fill('80')

      await lisääRivi('Opiskeluvalmiuksia tukevat opinnot')
      await kenttä('Opiskeluvalmiuksia tukevat opinnot', 'alku.input').fill(
        '22.6.2017'
      )
      await kenttä('Opiskeluvalmiuksia tukevat opinnot', 'loppu.input').fill(
        '28.6.2017'
      )
      await kenttä('Opiskeluvalmiuksia tukevat opinnot', 'kuvaus.input').fill(
        'Testing'
      )

      await oppijaPageV2.tallenna()

      await expect(lisätietoRivi(page, 'Majoitus')).toHaveText(
        /Majoitus\s*22\.6\.2017 — 1\.1\.2099/
      )
      await expect(lisätietoRivi(page, 'Ulkomaanjaksot')).toHaveText(
        /Ulkomaanjaksot\s*22\.6\.2017 —\s*Maa: Algeria\s*Kuvaus: Testing/
      )
      await expect(lisätietoRivi(page, 'Osa-aikaisuusjaksot')).toHaveText(
        /Osa-aikaisuusjaksot\s*22\.6\.2017 —\s*Osa-aikaisuus: 80\s%/
      )
      await expect(
        lisätietoRivi(page, 'Opiskeluvalmiuksia tukevat opinnot')
      ).toHaveText(
        /Opiskeluvalmiuksia tukevat opinnot\s*22\.6\.2017 — 28\.6\.2017\s*Kuvaus: Testing/
      )
    })

    test('Osaamisala tallentuu ensin ilman päivämääriä ja sitten alkupäivällä', async ({
      page,
      uusiOppijaPage,
      oppijaPageV2
    }) => {
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa)
      await muokkaa(page)
      await rivi(page, 'Osaamisala')
        .getByRole('button', { name: 'Lisää' })
        .click()
      await suoritus(page, '0.osaamisala.input').click()
      await suoritus(page, '0.osaamisala.options.1527.item').click()
      await oppijaPageV2.tallenna()
      await expect(suoritus(page, '0.osaamisala').locator('..')).toHaveText(
        'Automyynnin osaamisala'
      )

      await muokkaa(page)
      await suoritus(page, '0.alku.input').fill('1.1.2017')
      await oppijaPageV2.tallenna()
      await expect(suoritus(page, '0.osaamisala').locator('..')).toHaveText(
        /^Automyynnin osaamisala\s*1\.1\.2017 —\s*$/
      )
    })

    test('Osaamisalaksi tarjotaan koko koodisto, kun perustetta ei löydy ePerusteista', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(eiPerusteissa)
      await muokkaa(page)
      // Haun valmistumista on odotettava: kesken haun tarjotaan koko koodisto
      // joka tapauksessa
      const osaamisalaHaku = page.waitForResponse((vastaus) =>
        vastaus.url().includes('/editor/koodit/osaamisalat/osaamisala/')
      )
      await rivi(page, 'Osaamisala')
        .getByRole('button', { name: 'Lisää' })
        .click()
      const perusteenOsaamisalat = await (await osaamisalaHaku).json()
      expect(perusteenOsaamisalat.length).toBeGreaterThan(800)
      await suoritus(page, '0.osaamisala.input').click()

      const vaihtoehdot = suoritus(page, '0.osaamisala.options').locator(
        '.Select__optionLabel'
      )
      await expect(
        vaihtoehdot.getByText('Agroautomaation hyödyntämisen osaamisala', {
          exact: true
        })
      ).toHaveCount(1)
      await expect(
        vaihtoehdot.getByText('Aikuisten perusopetus', { exact: true })
      ).toHaveCount(1)
      // Perustekohtaisia osaamisaloja on enintään kymmeniä, koko koodistossa satoja
      await expect.poll(() => vaihtoehdot.count()).toBeGreaterThan(800)
    })

    test('Toimipisteen voi vaihtaa monta kertaa', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(amis)
      await muokkaa(page)
      const toimipiste = (id: string) =>
        suoritus(page, `organisaatio.edit.${id}`)
      const valitse = async (haku: string, oid: string, nimi: string) => {
        await toimipiste('input').fill(haku)
        await toimipiste(`options.${oid}.item`).click()
        await expect(toimipiste('input')).toHaveValue(nimi)
      }
      await valitse('Aalto', '1.2.246.562.10.56753942459', 'Aalto-yliopisto')
      await valitse(
        'Helsingin yliopisto',
        '1.2.246.562.10.39218317368',
        'Helsingin yliopisto'
      )
    })

    test('Keskiarvoa ei voi tallentaa arvoasteikon ulkopuolelta', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(ammattilainen)
      await muokkaa(page)
      const tallenna = page.getByTestId('oo.0.opiskeluoikeus.save')
      await suoritus(page, 'painotettu-keskiarvo.edit.input').fill('3.5')
      await expect(tallenna).toBeEnabled()

      await suoritus(page, 'painotettu-keskiarvo.edit.input').fill('7')
      await tallenna.click()
      await expect(page.getByTestId('globalErrors')).toContainText(
        'greaterThanMaximumValue'
      )

      await oppijaPage.goto(ammattilainen)
      await expect(suoritus(page, 'painotettu-keskiarvo.value')).toHaveText(
        '4,00'
      )
    })
  })

  test.describe('Tutkinnon osat', () => {
    test.beforeEach(async ({ page, uusiOppijaPage }) => {
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa)
      await muokkaa(page)
    })

    test('Vaihtoehdot tulevat ePerusteista', async ({ page }) => {
      await suoritus(page, 'uusi-muu-tutkinnonosa.input').first().click()
      await expect(
        suoritus(page, 'uusi-muu-tutkinnonosa.options')
          .first()
          .locator('.Select__optionLabel')
      ).toHaveCount(47)
      await page.keyboard.press('Escape')

      await suoritus(page, 'uusi-yhteinen-tutkinnonosa.input').click()
      await expect(
        suoritus(page, 'uusi-yhteinen-tutkinnonosa.options').locator(
          '.Select__optionLabel'
        )
      ).toHaveText([
        '101053 Viestintä- ja vuorovaikutusosaaminen',
        '101054 Matemaattis-luonnontieteellinen osaaminen',
        '101055 Yhteiskunnassa ja työelämässä tarvittava osaaminen',
        '101056 Sosiaalinen ja kulttuurinen osaaminen'
      ])
    })

    test('Arvioimaton osa näytetään keskeneräisenä ja arvioitu valmiina', async ({
      page,
      oppijaPageV2
    }) => {
      await lisääAmmatillinenOsa(page)
      await oppijaPageV2.tallenna()

      const osanRivi = osaRivi(page, 'Huolto- ja korjaustyöt')
      await expect(osanRivi.getByLabel('Suoritus kesken')).toBeVisible()
      await expect(osanRivi.getByLabel('Suoritus valmis')).toHaveCount(0)
      await expect(osa(page, 'arvosana.value')).toHaveText('–')

      await muokkaa(page)
      await valitseArvosana(page, 'osasuoritukset.0.arvosana', T1K3, '3')
      await oppijaPageV2.tallenna()

      await expect(osanRivi.getByLabel('Suoritus valmis')).toBeVisible()
      await expect(osanRivi.getByLabel('Suoritus kesken')).toHaveCount(0)
      await expect(osa(page, 'arvosana.value')).toHaveText('3')
    })

    test('Valtakunnallinen vapaasti valittava tutkinnon osa', async ({
      page,
      oppijaPageV2
    }) => {
      // Ryhmät ovat järjestyksessä: ammatilliset, vapaasti valittavat, ...
      await suoritus(page, 'uusi-muu-tutkinnonosa.input').nth(1).click()
      await suoritus(page, 'uusi-muu-tutkinnonosa.options')
        .getByText('Huippuosaajana toimiminen')
        .click()
      await expect(osaRivi(page, 'Huippuosaajana toimiminen')).toBeVisible()
      await osa(page, 'laajuus.edit.input').fill('10')
      await valitseArvosana(page, 'osasuoritukset.0.arvosana', T1K3, '3')

      await oppijaPageV2.tallenna()
      await expect(osaRivi(page, 'Huippuosaajana toimiminen')).toBeVisible()
      // Osa lasketaan vapaasti valittavien ryhmään, ei ammatillisiin osiin
      await expect(suoritus(page, 'yhteensa.3')).toHaveText('10 / 10 osp')
      await expect(suoritus(page, 'yhteensa.1')).toHaveCount(0)
    })

    test('Osaamisen tunnustamisen muokkaus ja poisto', async ({
      page,
      oppijaPageV2
    }) => {
      await lisääAmmatillinenOsa(page)
      await osanOminaisuus(page, 'tunnustettu.edit.add').click()
      await osanOminaisuus(page, 'tunnustettu.edit.selite.input').fill(
        'Tunnustamisen esimerkkiselite'
      )
      await oppijaPageV2.tallenna()
      await expect(osanOminaisuus(page, 'tunnustettu.selite')).toHaveText(
        'Tunnustamisen esimerkkiselite'
      )
      await expect(
        osanOminaisuus(page, 'tunnustettu.rahoituksenPiirissä')
      ).toHaveText('Ei')

      await muokkaa(page)
      await osanOminaisuus(page, 'tunnustettu.edit.selite.input').fill(
        'Tunnustamisen muokattu esimerkkiselite'
      )
      await osanOminaisuus(page, 'tunnustettu.edit.bool.label').click()
      await oppijaPageV2.tallenna()
      await expect(osanOminaisuus(page, 'tunnustettu.selite')).toHaveText(
        'Tunnustamisen muokattu esimerkkiselite'
      )
      await expect(
        osanOminaisuus(page, 'tunnustettu.rahoituksenPiirissä')
      ).toHaveText('Kyllä')

      await muokkaa(page)
      await osanOminaisuus(page, 'tunnustettu.edit.delete').click()
      await oppijaPageV2.tallenna()
      await expect(osanOminaisuus(page, 'tunnustettu.selite')).toHaveCount(0)

      await muokkaa(page)
      await expect(osanOminaisuus(page, 'tunnustettu.edit.add')).toBeVisible()
    })

    test('Tutkinnon osan lisätiedon lisäys, muokkaus ja poisto', async ({
      page,
      oppijaPageV2
    }) => {
      const lisätiedot = ominaisuus(page, 'Lisätiedot')
      const valitseLaji = async (laji: string) => {
        await osanOminaisuus(page, '0.tunniste.input').click()
        await osanOminaisuus(page, '0.tunniste.options').getByText(laji).click()
      }

      await lisääAmmatillinenOsa(page)
      await lisätiedot.getByRole('button', { name: 'Lisää' }).click()
      await valitseLaji('Muu lisätieto')
      await lisätiedot.locator('textarea').fill('Muita tietoja')
      await oppijaPageV2.tallenna()
      await expect(lisätiedot).toHaveText(
        /Lisätiedot\s*Muu lisätieto\s*Muita tietoja/
      )

      await muokkaa(page)
      await valitseLaji('Osaamisen arvioinnin mukauttaminen')
      await lisätiedot.locator('textarea').fill('Arviointia on mukautettu')
      await oppijaPageV2.tallenna()
      await expect(lisätiedot).toHaveText(
        /Lisätiedot\s*Osaamisen arvioinnin mukauttaminen\s*Arviointia on mukautettu/
      )

      await muokkaa(page)
      await osanOminaisuus(page, '0.delete').click()
      await oppijaPageV2.tallenna()
      await expect(lisätiedot).toHaveCount(0)

      await muokkaa(page)
      await expect(
        lisätiedot.getByRole('button', { name: 'Lisää' })
      ).toBeVisible()
      await expect(lisätiedot.locator('textarea')).toHaveCount(0)
    })

    test('Näytön lisäys, muokkaus ja poisto', async ({
      page,
      oppijaPageV2
    }) => {
      const näyttö = ominaisuus(page, 'Näyttö')
      const näytönArvo = (otsikko: string) =>
        näyttö.locator('.KeyValueRow', {
          has: page.getByText(otsikko, { exact: true })
        })

      await lisääAmmatillinenOsa(page)
      await page
        .getByRole('button', { name: 'Lisää ammattiosaamisen näyttö' })
        .click()
      await osanOminaisuus(page, 'nayton-kuvaus.input').fill(
        'Näytön esimerkkikuvaus'
      )
      await osanOminaisuus(page, 'suorituspaikka.input').click()
      await osanOminaisuus(page, 'suorituspaikka.options.1.item').click()
      await näyttö
        .getByPlaceholder('Kuvaus')
        .fill('Esimerkkityöpaikka, Esimerkkisijainti')
      await valitseArvosana(
        page,
        'osasuoritukset.0.properties.arvosana',
        ASTEIKKO_1_5,
        '3'
      )
      await osanOminaisuus(page, 'input').fill('1.2.2017')
      await osanOminaisuus(
        page,
        'ammatillisennaytonarvioinnistapaattaneet-uusi.input'
      ).click()
      await osanOminaisuus(
        page,
        'ammatillisennaytonarvioinnistapaattaneet-uusi.options.1.item'
      ).click()
      await oppijaPageV2.tallenna()

      await expect(näytönArvo('Kuvaus')).toContainText('Näytön esimerkkikuvaus')
      await expect(näytönArvo('Suorituspaikka')).toContainText(
        'työpaikka: Esimerkkityöpaikka, Esimerkkisijainti'
      )
      await expect(näytönArvo('Arvosana')).toContainText('3')
      await expect(näytönArvo('Arviointipäivä')).toContainText('1.2.2017')
      await expect(näytönArvo('Arvioinnista päättäneet')).toContainText(
        'Opettaja'
      )

      await muokkaa(page)
      await expect(osanOminaisuus(page, 'nayton-kuvaus.input')).toHaveValue(
        'Näytön esimerkkikuvaus'
      )
      await expect(osanOminaisuus(page, 'input')).toHaveValue('1.2.2017')
      await osanOminaisuus(page, 'nayton-kuvaus.input').fill(
        'Näytön muokattu esimerkkikuvaus'
      )
      await valitseArvosana(
        page,
        'osasuoritukset.0.properties.arvosana',
        ASTEIKKO_1_5,
        '2'
      )
      await oppijaPageV2.tallenna()
      await expect(näytönArvo('Kuvaus')).toContainText(
        'Näytön muokattu esimerkkikuvaus'
      )
      await expect(näytönArvo('Arvosana')).toContainText('2')

      await muokkaa(page)
      // Näytön oma poistopainike on sisällön jälkeen, sisällössä on muitakin
      await osanOminaisuus(page, 'delete').last().click()
      const lisääNäyttö = page.getByRole('button', {
        name: 'Lisää ammattiosaamisen näyttö'
      })
      await expect(lisääNäyttö).toBeVisible()
      await oppijaPageV2.tallenna()
      await expect(näyttö).toHaveCount(0)

      await muokkaa(page)
      await expect(lisääNäyttö).toBeVisible()
    })

    test('Useamman arvioinnin lisäys ja poisto', async ({
      page,
      oppijaPageV2
    }) => {
      const arviointi = (id: string) => osanOminaisuus(page, `arviointi.${id}`)
      const tänään = formatFinnishDate(new Date())!

      await lisääAmmatillinenOsa(page)
      await arviointi('lisää-arviointi').click()
      await arviointi('lisää-arviointi').click()
      await valitseArvosana(
        page,
        'osasuoritukset.0.properties.arviointi.0.arvosana',
        ASTEIKKO_1_5,
        '3'
      )
      await valitseArvosana(
        page,
        'osasuoritukset.0.properties.arviointi.1.arvosana',
        ASTEIKKO_1_5,
        '4'
      )
      await oppijaPageV2.tallenna()

      await expect(arviointi('0.arvosana')).toHaveText('3')
      await expect(arviointi('1.arvosana')).toHaveText('4')
      await expect(ominaisuus(page, 'Arviointi')).toHaveText(
        new RegExp(
          `Arvosana\\s*3\\s*Arviointipäivä\\s*${tänään}\\s*Arvioijat\\s*Arvosana\\s*4\\s*Arviointipäivä\\s*${tänään}`
        )
      )

      await muokkaa(page)
      await arviointi('1.delete').click()
      await arviointi('0.delete').click()
      await oppijaPageV2.tallenna()
      await expect(arviointi('0.arvosana')).toHaveCount(0)
      await expect(osa(page, 'arvosana.value')).toHaveText('–')

      await muokkaa(page)
      await expect(arviointi('lisää-arviointi')).toBeVisible()
      await expect(arviointi('0.arvosana')).toHaveCount(0)
    })
  })

  test.describe('Jatko-opintovalmiuksia tukevat opinnot', () => {
    const JATKO_OPINNOT =
      'Yhteisten tutkinnon osien osa-alueita, lukio-opintoja tai muita jatko-opintovalmiuksia tukevia opintoja'
    const REFORMIN_PERUSTUTKINTO = {
      haku: 'Autoalan perustutkinto, Koulutusvientikokeilu',
      avain: '351301_OPH-4792-2017'
    }
    const opinto = (indeksi: number, testId: string) =>
      `osasuoritukset.0.properties.osasuoritukset.${indeksi}.${testId}`

    test('Osa-alueen ja muun opinnon lisääminen; valmiiksi merkitseminen arvioinnin jälkeen', async ({
      page,
      uusiOppijaPage,
      oppijaPageV2
    }) => {
      test.slow()
      await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiKoskessa, {
        tutkinto: REFORMIN_PERUSTUTKINTO
      })
      await muokkaa(page)
      await suoritus(page, 'uusi-jatko-opintovalmiuksia').click()

      await osanOminaisuus(page, 'uusi-yhteinen-osan-osa-alue.input').click()
      const osaAlueet = osanOminaisuus(
        page,
        'uusi-yhteinen-osan-osa-alue.options'
      ).locator('.Select__optionLabel')
      for (const osaAlue of [
        'TET Työelämätaidot',
        'MA Matematiikka',
        'AI Äidinkieli',
        '003 Viestintä- ja vuorovaikutusosaaminen'
      ]) {
        await expect(
          osaAlueet.getByText(osaAlue, { exact: true })
        ).toBeVisible()
      }
      await osaAlueet.getByText('TET Työelämätaidot', { exact: true }).click()
      await oppijaPageV2.tallenna()
      await expect(osaRivi(page, JATKO_OPINNOT)).toBeVisible()
      await expect(osaRivi(page, 'Työelämätaidot')).toBeVisible()

      await muokkaa(page)
      await page
        .getByRole('button', {
          name: 'Lisää muu opintovalmiuksia tukeva opinto'
        })
        .click()
      await page.locator('.Modal input').fill('Tutortoiminta')
      await page
        .locator('.Modal')
        .getByRole('button', { name: 'Lisää' })
        .click()
      await oppijaPageV2.tallenna()
      await expect(osaRivi(page, 'Tutortoiminta')).toBeVisible()

      await muokkaa(page)
      await valitseArvosana(page, opinto(0, 'arvosana'), T1K3, '3')
      await valitseArvosana(page, opinto(1, 'arvosana'), T1K3, '3')
      await expect(
        suoritus(page, 'suorituksenVahvistus.edit.merkitseValmiiksi')
      ).toBeEnabled()
      await oppijaPageV2.tallenna()
      await expect(suoritus(page, opinto(0, 'arvosana.value'))).toHaveText('3')
      await expect(suoritus(page, opinto(1, 'arvosana.value'))).toHaveText('3')
    })
  })
})

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: tietojen katselu', () => {
  test.describe('Oppilaitoksen tallentajana', () => {
    test.use({ storageState: virkailija('kalle') })

    test('Luottamukselliset lisätiedot näytetään', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(esimerkki)
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton')
      ).toBeVisible()
      await expect(
        lisätietoRivi(page, 'Vaativan erityisen tuen erityinen tehtävä')
      ).toHaveText(/Vaativan erityisen tuen erityinen tehtävä\s*30\.5\.2019 —/)
      await expect(lisätietoRivi(page, 'Vankilaopetuksessa')).toHaveText(
        /Vankilaopetuksessa\s*30\.5\.2019 —/
      )
    })

    test('Tunnustettu suoritus', async ({
      page,
      fixtures,
      oppijaPage,
      oppijaPageV2
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(tunnustettu)

      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
      ).toHaveText(
        'Opiskeluoikeuden voimassaoloaika: 1.9.2016 — 1.5.2020 (Arvioitu päättymispäivä)'
      )
      const tila = (id: string) =>
        page.getByTestId(`oo.0.opiskeluoikeus.tila.value.items.0.${id}`)
      await expect(tila('date')).toHaveText('1.9.2016')
      await expect(tila('tila')).toHaveText('Läsnä')
      await expect(tila('rahoitus')).toHaveText(
        'Valtionosuusrahoitteinen koulutus'
      )

      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Autoalan perustutkinto'
      )
      await expect(page.locator('.AmmatillinenTutkinto')).toContainText(
        '351301'
      )
      await expect(suoritus(page, 'peruste.value')).toHaveText('39/011/2014')
      await expect(suoritus(page, 'suoritustapa')).toHaveText('Näyttötutkinto')
      await expect(suoritus(page, 'organisaatio.value')).toHaveText(
        'Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka'
      )
      await expect(suoritus(page, 'alkamispäivä.value')).toHaveText('1.9.2016')
      await expect(suoritus(page, 'suorituskieli.value')).toHaveText('suomi')
      await expect(
        suoritus(page, 'suorituksenVahvistus.value.status')
      ).toHaveText('Suoritus kesken')

      await oppijaPageV2.openAllOsasuoritukset()
      await expect(
        osaRivi(page, 'Moottorin ja voimansiirron huolto ja korjaus')
      ).toBeVisible()
      await expect(osa(page, 'laajuus.value')).toHaveText('15 osp')
      await expect(osa(page, 'arvosana.value')).toHaveText('Hyväksytty')
      await expect(ominaisuus(page, 'Pakollinen')).toHaveText(/Pakollinen\s*Ei/)
      await expect(osanOminaisuus(page, 'toimipiste')).toHaveText(
        'Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka'
      )
      await expect(osanOminaisuus(page, 'vahvistus')).toHaveText(
        '31.5.2013 Reijo Reksi, rehtori'
      )
      await expect(osanOminaisuus(page, 'tunnustettu.selite')).toHaveText(
        'Tutkinnon osa on tunnustettu aiemmin suoritetusta autoalan perustutkinnon osasta (1.8.2000 nro 11/011/2000)'
      )
      await expect(
        osanOminaisuus(page, 'tunnustettu.rahoituksenPiirissä')
      ).toHaveText('Ei')
      await expect(osanOminaisuus(page, 'arviointi.0.arvosana')).toHaveText(
        'Hyväksytty'
      )
      await expect(ominaisuus(page, 'Arviointi')).toContainText('20.3.2013')
      for (const [indeksi, nimi] of [
        'Jaana Arstila',
        'Pekka Saurmann',
        'Juhani Mykkänen'
      ].entries()) {
        await expect(
          osanOminaisuus(page, `arviointi.0.arvioijat.nimi.${indeksi}`)
        ).toHaveText(nimi)
      }
      await expect(suoritus(page, 'yhteensa')).toHaveText('15 osp')
    })

    test('Opiskeluoikeuden lisätiedot', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(amis)

      for (const [otsikko, arvo] of [
        ['Majoitus', /1\.9\.2012 — 1\.9\.2013/],
        ['Sisäoppilaitosmainen majoitus', /1\.9\.2012 — 1\.9\.2013/],
        [
          'Vaativan erityisen tuen yhteydessä järjestettävä majoitus',
          /1\.9\.2012 — 1\.9\.2013/
        ],
        [
          'Ulkomaanjaksot',
          /1\.9\.2012 — 1\.9\.2013\s*Maa: Ruotsi\s*Kuvaus: Harjoittelua ulkomailla/
        ],
        ['Hojks', /Yleinen opetusryhmä/],
        ['Vaikeasti vammaisille järjestetty opetus', /1\.9\.2012 — 1\.9\.2013/],
        ['Vammainen ja avustaja', /1\.9\.2012 — 1\.9\.2013/],
        [
          'Osa-aikaisuusjaksot',
          /1\.9\.2012 —\s*Osa-aikaisuus: 80\s%\s*8\.5\.2019 —\s*Osa-aikaisuus: 60\s%/
        ],
        [
          'Opiskeluvalmiuksia tukevat opinnot',
          /1\.10\.2013 — 31\.10\.2013\s*Kuvaus: Opiskeluvalmiuksia tukevia opintoja/
        ],
        ['Henkilöstökoulutus', /Kyllä/],
        ['Vankilaopetuksessa', /2\.9\.2013 —/]
      ] as const) {
        await expect(lisätietoRivi(page, otsikko)).toHaveText(arvo)
      }
    })

    test('Tutkinnon nimi tulee ePerusteista', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(historoitsija)
      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu'
      )
      await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
        'Tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu'
      )
      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
        'tieto- ja viestintätekniikan perustutkinto, koulutusvientikokeilu'
      )
      await expect(page.locator('.AmmatillinenTutkinto')).toContainText(
        '341101'
      )
      await expect(suoritus(page, 'peruste.value')).toHaveText('OPH-1117-2019')
    })
  })

  test.describe('Vastuukäyttäjänä ilman luottamuksellinen-roolia', () => {
    test.use({ storageState: virkailija('stadin-vastuu') })

    test('Luottamuksellisia lisätietoja ei näytetä', async ({
      page,
      fixtures,
      oppijaPage
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(esimerkki)
      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Autoalan perustutkinto'
      )
      await expect(
        page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton')
      ).toHaveCount(0)
      await expect(page.locator('.EditorContainer__lisatiedot')).toHaveCount(0)
    })
  })
})
