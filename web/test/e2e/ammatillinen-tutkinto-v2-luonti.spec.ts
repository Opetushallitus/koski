import { Page } from '@playwright/test'
import { expect, test } from './base'
import {
  luoAmmatillinenTutkinto,
  STADIN_AMMATTIOPISTO,
  täytäAmmatillinenTutkinto
} from './fragments/ammatillinenTutkintoLuonti'
import { getVirkailijaSession, virkailija } from './setup/auth'

const OMNIA = { haku: 'Omnia', oid: '1.2.246.562.10.51720121923' }

const opiskeluoikeudenTila = (page: Page) => ({
  tila: page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila'),
  rahoitus: page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.rahoitus')
})

const suoritus = (page: Page, testId: string) =>
  page.getByTestId(`oo.0.suoritukset.0.${testId}`)

const lisätietoRivi = (page: Page, nimi: string) =>
  page.locator('.EditorContainer__lisatiedot .KeyValueRow', {
    has: page.getByText(nimi, { exact: true })
  })

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  const henkilönNimikentät = (page: Page) => ({
    etunimet: page.getByTestId('uusiOpiskeluoikeus.oppija.etunimet.input'),
    kutsumanimi: page.locator('.henkilo .kutsumanimi input'),
    sukunimi: page.getByTestId('uusiOpiskeluoikeus.oppija.sukunimi.input')
  })
  const lomakkeenHetu = (page: Page) => page.locator('.henkilo .hetu .value')

  const henkilöpalvelunHenkilöt = [
    {
      kuvaus: 'jolla on hetu ja useita etunimiä',
      hash: 'hetu=280608-6619',
      sukunimi: 'Tunkkila-Fagerlund',
      etunimet: 'Tero Petteri Gustaf',
      lomakkeenHetu: '280608-6619',
      otsikko: 'Tunkkila-Fagerlund, Tero Petteri Gustaf (280608-6619)'
    },
    {
      kuvaus: 'jolla on OID ja hetu',
      hash: 'oid=1.2.246.562.24.99999555555',
      sukunimi: 'EiKoskessa',
      etunimet: 'Eino',
      lomakkeenHetu: '',
      otsikko: 'EiKoskessa, Eino (270181-5263)'
    },
    {
      kuvaus: 'jolla on vanha hetu',
      hash: 'hetu=270181-517T',
      sukunimi: 'EiKoskessa',
      etunimet: 'Eino',
      lomakkeenHetu: '270181-517T',
      otsikko: 'EiKoskessa, Eino (270181-5263)'
    },
    {
      kuvaus: 'jolla on vain OID',
      hash: 'oid=1.2.246.562.24.99999555556',
      sukunimi: 'EiKoskessaHetuton',
      etunimet: 'Eino',
      lomakkeenHetu: '',
      otsikko: 'EiKoskessaHetuton, Eino'
    }
  ]

  for (const henkilö of henkilöpalvelunHenkilöt) {
    test(`Henkilöpalvelusta löytyvälle henkilölle, ${henkilö.kuvaus}`, async ({
      page,
      oppijaPage,
      uusiOppijaPage
    }) => {
      await page.goto(`/koski/uusioppija#${henkilö.hash}`)

      const nimet = henkilönNimikentät(page)
      await expect(nimet.etunimet).toHaveValue(henkilö.etunimet)
      await expect(nimet.etunimet).toBeDisabled()
      await expect(nimet.sukunimi).toHaveValue(henkilö.sukunimi)
      await expect(nimet.kutsumanimi).toHaveValue(
        henkilö.etunimet.split(' ')[0]
      )
      await expect(lomakkeenHetu(page)).toHaveText(henkilö.lomakkeenHetu)

      await täytäAmmatillinenTutkinto(page, uusiOppijaPage)
      await uusiOppijaPage.submitAndExpectSuccess()

      await expect(oppijaPage.oppijaHeading).toContainText(henkilö.otsikko)
      await expect(suoritus(page, 'koulutus')).toHaveText(
        'Autoalan perustutkinto'
      )
      await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
        'Stadin ammatti- ja aikuisopisto,'
      )
      await expect(suoritus(page, 'suorituskieli.value')).toHaveText('suomi')
    })
  }

  test('Uudelle henkilölle; samaan opiskeluoikeuteen ei voi lisätä toista tutkintoa', async ({
    page,
    oppijaPage,
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo('230872-7258')

    const nimet = henkilönNimikentät(page)
    await expect(nimet.etunimet).toHaveValue('')
    await expect(nimet.etunimet).toBeEnabled()
    await expect(nimet.sukunimi).toHaveValue('')

    await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
    await täytäAmmatillinenTutkinto(page, uusiOppijaPage, {
      suorituskieli: 'kieli_SV'
    })
    await uusiOppijaPage.submitAndExpectSuccess()

    await expect(oppijaPage.oppijaHeading).toContainText(
      'Tyhjä, Tero (230872-7258)'
    )
    const { tila, rahoitus } = opiskeluoikeudenTila(page)
    await expect(tila).toHaveText('Läsnä')
    await expect(rahoitus).toHaveText('Valtionosuusrahoitteinen koulutus')
    await expect(suoritus(page, 'koulutus')).toHaveText(
      'Autoalan perustutkinto'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Stadin ammatti- ja aikuisopisto,'
    )
    await expect(suoritus(page, 'suorituskieli.value')).toHaveText('ruotsi')

    // Vain näyttötutkintoon valmistavan koulutuksen ja tutkinnon pari on sallittu
    const lisäysLinkki = page.getByText('lisää ammatillisen tutkinnon suoritus')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
    await expect(lisäysLinkki).toHaveCount(0)
    await expect(
      page.getByText('lisää näyttötutkintoon valmistavan koulutuksen suoritus')
    ).toHaveCount(0)
    await expect(page.locator('.Tabs__item')).toHaveCount(1)
  })

  test('Näyttötutkintoon valmistava koulutus uutena opiskeluoikeutena; tutkinnon voi lisätä samaan opiskeluoikeuteen', async ({
    page,
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo('230872-7258')
    await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
    await täytäAmmatillinenTutkinto(page, uusiOppijaPage, {
      suoritustyyppi: 'nayttotutkintoonvalmistavakoulutus',
      suorituskieli: 'kieli_SV',
      tutkinto: { haku: 'Autoalan työnjoh', avain: '457305_40/011/2001' }
    })
    await uusiOppijaPage.submitAndExpectSuccess()

    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
      'Näyttötutkintoon valmistava koulutus'
    )
    await expect(suoritus(page, 'koulutus')).toHaveText(
      'Näyttötutkintoon valmistava koulutus'
    )
    await expect(suoritus(page, 'tutkinto')).toHaveText(
      'Autoalan työnjohdon erikoisammattitutkinto'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Stadin ammatti- ja aikuisopisto,'
    )
    await expect(suoritus(page, 'suorituskieli.value')).toHaveText('ruotsi')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(
      page.getByText('lisää ammatillisen tutkinnon suoritus')
    ).toBeVisible()
  })

  test('Opintojen rahoitus tallentuu tilan ensimmäiselle jaksolle', async ({
    page,
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo('230872-7258')
    await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
    await täytäAmmatillinenTutkinto(page, uusiOppijaPage, {
      rahoitus: 'Aikuisten osaamisperustan vahvistaminen'
    })
    await uusiOppijaPage.submitAndExpectSuccess()

    const { tila, rahoitus } = opiskeluoikeudenTila(page)
    await expect(tila).toHaveText('Läsnä')
    await expect(rahoitus).toHaveText('Aikuisten osaamisperustan vahvistaminen')
  })
})

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: maksuttomuus', () => {
  test.use({ storageState: virkailija('pää') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  const oppivelvollinen = '010104A6094'
  const eiOppivelvollinen = '311203A1454'

  test('Maksutonta koulutusta voi muokata ja oikeuden pidennyksen lisätä', async ({
    page,
    uusiOppijaPage,
    oppijaPageV2
  }) => {
    await luoAmmatillinenTutkinto(page, uusiOppijaPage, oppivelvollinen, {
      aloituspäivä: '1.1.2021',
      maksuton: 'maksuton'
    })
    const maksuttomuus = lisätietoRivi(page, 'Koulutuksen maksuttomuus')
    await expect(maksuttomuus).toHaveText(
      /Koulutuksen maksuttomuus\s*1\.1\.2021 —\s*Maksuton/
    )

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.maksuttomuus.alku.input')
      .fill('10.8.2021')
    await oppijaPageV2.tallenna()
    await expect(maksuttomuus).toHaveText(
      /Koulutuksen maksuttomuus\s*10\.8\.2021 —\s*Maksuton/
    )

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    const pidennys = lisätietoRivi(page, 'Oikeutta maksuttomuuteen pidennetty')
    await pidennys.getByRole('button', { name: 'Lisää' }).click()
    await pidennys
      .getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.aikajakso.alku.input')
      .fill('1.1.2025')
    await pidennys
      .getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.aikajakso.loppu.input')
      .fill('12.12.2025')
    await oppijaPageV2.tallenna()

    await expect(maksuttomuus).toHaveText(
      /Koulutuksen maksuttomuus\s*10\.8\.2021 —\s*Maksuton/
    )
    await expect(pidennys).toHaveText(
      /Oikeutta maksuttomuuteen pidennetty\s*1\.1\.2025 — 12\.12\.2025/
    )
  })

  test('Maksullinen koulutus näytetään lisätiedoissa', async ({
    page,
    uusiOppijaPage
  }) => {
    await luoAmmatillinenTutkinto(page, uusiOppijaPage, oppivelvollinen, {
      aloituspäivä: '1.8.2021',
      maksuton: 'maksullinen'
    })
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton')
    ).toBeVisible()
    await expect(lisätietoRivi(page, 'Koulutuksen maksuttomuus')).toHaveText(
      /Koulutuksen maksuttomuus\s*1\.8\.2021 —\s*Maksullinen/
    )
  })

  test('Ilman maksuttomuustietoa lisätietoja ei näytetä', async ({
    page,
    uusiOppijaPage
  }) => {
    await luoAmmatillinenTutkinto(page, uusiOppijaPage, eiOppivelvollinen, {
      aloituspäivä: '1.8.2021',
      maksuton: 'eiOvlPiirissä'
    })
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton')
    ).toHaveCount(0)
    await expect(page.locator('.EditorContainer__lisatiedot')).toHaveCount(0)
  })
})

test.describe('Ammatillisen tutkinnon uusi käyttöliittymä: sisältyvä opiskeluoikeus', () => {
  test.use({ storageState: virkailija('omnia-tallentaja') })

  const ammattilainen = '1.2.246.562.24.00000000020'
  const ammattilaisenHetu = '280618-402H'
  const luontoJaYmpäristöala = {
    haku: 'Luonto- ja ympäristöalan perust',
    avain: '361902_62/011/2014',
    suoritustapa: 'ops'
  }

  test('Sisältävä opiskeluoikeus haetaan oidilla ja sisältävä organisaatio näkee sisältyvän muttei voi muokata sitä', async ({
    browser,
    page,
    fixtures,
    uusiOppijaPage,
    oppijaPageV2
  }, testInfo) => {
    await fixtures.reset()

    // Tallentaja kuuluu sisältävän opiskeluoikeuden (Stadin) organisaatioon
    const tallentajaContext = await browser.newContext({
      storageState: await getVirkailijaSession(
        testInfo,
        'tallentaja',
        'tallentaja'
      )
    })
    const tallentajaPage = await tallentajaContext.newPage()
    const sisältäväOppija = await (
      await tallentajaPage.request.get(`/koski/api/oppija/${ammattilainen}`)
    ).json()
    const sisältävänOid: string = sisältäväOppija.opiskeluoikeudet[0].oid

    // Sisältyvän ja sisältävän on oltava samaa tutkintoa (TOR-2379)
    await uusiOppijaPage.goTo(ammattilaisenHetu)
    await täytäAmmatillinenTutkinto(page, uusiOppijaPage, {
      oppilaitos: OMNIA,
      tutkinto: luontoJaYmpäristöala
    })
    await uusiOppijaPage.submitAndExpectSuccess()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Omnia'
    )

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    const sisältyy = page.locator('.KeyValueRow', {
      has: page.getByText('Sisältyy opiskeluoikeuteen', { exact: true })
    })
    await sisältyy.getByRole('button', { name: 'Lisää' }).click()
    const oppilaitos = (id: string) =>
      page.getByTestId(`oo.0.opiskeluoikeus.oppilaitos.${id}`)

    // Myös oppisopimustoimipiste kelpaa sisältäväksi oppilaitokseksi
    await oppilaitos('input').fill('Stadin oppisopimuskeskus')
    await oppilaitos('options.1.2.246.562.10.88592529245.item').click()
    await expect(oppilaitos('input')).toHaveValue('Stadin oppisopimuskeskus')

    await oppilaitos('input').fill(STADIN_AMMATTIOPISTO.haku)
    await oppilaitos(`options.${STADIN_AMMATTIOPISTO.oid}.item`).click()
    await expect(oppilaitos('input')).toHaveValue(
      'Stadin ammatti- ja aikuisopisto'
    )

    const oid = sisältyy.locator('input.TextEdit__input')
    await oid.fill('929292929')
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('globalErrors')).toContainText(
      'Sisältävää opiskeluoikeutta ei löydy oid-arvolla 929292929'
    )

    await oid.fill(sisältävänOid)
    await oppijaPageV2.tallenna()
    await expect(sisältyy).toContainText(sisältävänOid)
    await expect(sisältyy).toContainText('Stadin ammatti- ja aikuisopisto')

    await tallentajaPage.goto(`/koski/oppija/${ammattilainen}`)
    const opiskeluoikeudet = tallentajaPage.locator(
      '.opiskeluoikeuksientiedot > li'
    )
    const opiskeluoikeus = (otsikko: string) =>
      opiskeluoikeudet.filter({
        has: tallentajaPage
          .getByTestId('oo.0.opiskeluoikeus.nimi')
          .filter({ hasText: otsikko })
      })
    await expect(opiskeluoikeudet).toHaveCount(2)
    await expect(
      opiskeluoikeus(
        'Stadin ammatti- ja aikuisopisto, luonto- ja ympäristöalan perustutkinto'
      )
    ).toHaveCount(1)
    await expect(
      opiskeluoikeus('Omnia, luonto- ja ympäristöalan perustutkinto')
    ).toHaveCount(1)

    await expect(
      opiskeluoikeus('Stadin').getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible()
    await expect(
      opiskeluoikeus('Omnia').getByTestId('oo.0.opiskeluoikeus.edit')
    ).toHaveCount(0)

    await tallentajaContext.close()
  })
})
