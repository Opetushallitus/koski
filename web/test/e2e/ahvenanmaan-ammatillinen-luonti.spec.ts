import { Page } from '@playwright/test'
import { expect, test } from './base'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija } from './setup/auth'

/**
 * Ahvenanmaan ammatillisen koulutuksen opiskeluoikeuden luonti uusi oppija
 * -dialogissa.
 *
 * Ålands yrkesgymnasium (Maarianhamina, kunta_478) on mockdatan
 * ahvenanmaalainen ammatillinen oppilaitos, ja OppilaitosServlet tarjoaa sille
 * Ahvenanmaan ammatillisen koulutuksen manner-Suomen ammatillisen sijaan.
 * Tutkinnot ovat manner-Suomen perusteita. Suoritustapa on aina reformi, eikä
 * opiskeluoikeusjaksoilla ole rahoitusta tai lisätiedoissa maksuttomuutta,
 * joten niitä ei kysytä.
 */

const ålandsYrkesgymnasium = '1.2.246.562.10.62744318455'
const autoalanPerustutkinto = { haku: 'auto', avain: '351301_39/011/2014' }

const tutkinnonHetu = '170305A214K'
const osittaisenHetu = '080604A338X'

const kenttä = (page: Page, id: string) =>
  page.getByTestId(`uusiOpiskeluoikeus.modal.${id}`)

const täytäLomake = async (
  page: Page,
  uusiOppijaPage: KoskiUusiOppijaPage,
  hetu: string,
  suoritustyyppi?: string
) => {
  await uusiOppijaPage.goTo(hetu)
  await uusiOppijaPage.fill({ etunimet: 'Elin', sukunimi: 'Ålänning' })
  await kenttä(page, 'oppilaitos.input').fill('Ålands')
  await kenttä(page, `oppilaitos.options.${ålandsYrkesgymnasium}.item`).click()
  await uusiOppijaPage.controls.opiskeluoikeus.set(
    'opiskeluoikeudentyyppi_ahvenanmaanammatillinenkoulutus'
  )
  if (suoritustyyppi) {
    await uusiOppijaPage.controls.suoritustyyppi.set(
      `suorituksentyyppi_${suoritustyyppi}`
    )
  }
  await kenttä(page, 'tutkinto.input').fill(autoalanPerustutkinto.haku)
  await kenttä(page, `tutkinto.options.${autoalanPerustutkinto.avain}.item`).click()
}

const haeLuotuOpiskeluoikeus = async (page: Page) => {
  const oid = page.url().match(/oppija\/(1\.2\.[\d.]+)/)?.[1]
  expect(oid).toBeDefined()
  const response = await page.request.get(`/koski/api/oppija/${oid}`)
  expect(response.ok()).toBeTruthy()
  const oppija = await response.json()
  expect(oppija.opiskeluoikeudet).toHaveLength(1)
  return oppija.opiskeluoikeudet[0]
}

test.describe('Ahvenanmaan ammatillisen koulutuksen opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lomakkeella kysytään vain Ahvenanmaan suoritukselle kuuluvat tiedot', async ({
    page,
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo(tutkinnonHetu)
    await kenttä(page, 'oppilaitos.input').fill('Ålands')
    await kenttä(page, `oppilaitos.options.${ålandsYrkesgymnasium}.item`).click()

    const opiskeluoikeudet =
      await uusiOppijaPage.controls.opiskeluoikeus.options()
    expect(opiskeluoikeudet).toContain('Ahvenanmaan ammatillinen koulutus')
    expect(opiskeluoikeudet).not.toContain('Ammatillinen koulutus')

    await uusiOppijaPage.controls.opiskeluoikeus.set(
      'opiskeluoikeudentyyppi_ahvenanmaanammatillinenkoulutus'
    )
    expect(await uusiOppijaPage.controls.suoritustyyppi.options()).toHaveLength(
      2
    )
    await expect(uusiOppijaPage.controls.suorituskieli.input).toHaveValue(
      'ruotsi'
    )
    await expect(kenttä(page, 'tutkinto.input')).toBeVisible()
    await expect(kenttä(page, 'suoritustapa.input')).toHaveCount(0)
    await expect(kenttä(page, 'opintojenRahoitus.input')).toHaveCount(0)
    await expect(
      page.locator('[data-testid^="uusiOpiskeluoikeus.modal.maksuton"]')
    ).toHaveCount(0)
  })

  test('Tutkinnon suorituksen luonti onnistuu', async ({
    page,
    uusiOppijaPage
  }) => {
    await täytäLomake(page, uusiOppijaPage, tutkinnonHetu)
    await uusiOppijaPage.submitAndExpectSuccess()

    // Perusteen nimi täydennetään ePerusteista; koulutuskoodin nimi on
    // koodistossa "Ajoneuvoalan perustutkinto".
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toHaveText(
      'Autoalan perustutkinto'
    )

    const opiskeluoikeus = await haeLuotuOpiskeluoikeus(page)
    expect(opiskeluoikeus.tyyppi.koodiarvo).toEqual(
      'ahvenanmaanammatillinenkoulutus'
    )
    expect(
      opiskeluoikeus.tila.opiskeluoikeusjaksot[0].opintojenRahoitus
    ).toBeUndefined()
    const suoritus = opiskeluoikeus.suoritukset[0]
    expect(suoritus.tyyppi.koodiarvo).toEqual('ahvenanmaanammatillinentutkinto')
    expect(suoritus.suoritustapa.koodiarvo).toEqual('reformi')
    expect(suoritus.suorituskieli.koodiarvo).toEqual('SV')
    expect(suoritus.koulutusmoduuli.perusteenDiaarinumero).toEqual(
      '39/011/2014'
    )
    expect(suoritus.koulutusmoduuli.perusteenNimi.fi).toEqual(
      'Autoalan perustutkinto'
    )
  })

  test('Tutkinnon osan/osien suorituksen luonti onnistuu, ja sille voi lisätä tutkinnon osan', async ({
    page,
    uusiOppijaPage,
    oppijaPageV2
  }) => {
    await täytäLomake(
      page,
      uusiOppijaPage,
      osittaisenHetu,
      'ahvenanmaanammatillinentutkintoosittainen'
    )
    await uusiOppijaPage.submitAndExpectSuccess()

    const opiskeluoikeus = await haeLuotuOpiskeluoikeus(page)
    const suoritus = opiskeluoikeus.suoritukset[0]
    expect(suoritus.tyyppi.koodiarvo).toEqual(
      'ahvenanmaanammatillinentutkintoosittainen'
    )
    expect(suoritus.suoritustapa.koodiarvo).toEqual('reformi')
    expect(suoritus.toinenTutkintonimike).toEqual(false)
    expect(suoritus.toinenOsaamisala).toEqual(false)

    // Osa/osia-suorituksella on omat kenttänsä ja osasuoritusluokkansa.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(
      page.locator('.KeyValueRow', {
        has: page.getByText('Toinen tutkintonimike', { exact: true })
      })
    ).toBeVisible()
    await page
      .getByTestId('oo.0.suoritukset.0.uusi-muu-tutkinnonosa.input')
      .first()
      .click()
    await page
      .getByTestId('oo.0.suoritukset.0.uusi-muu-tutkinnonosa.options.100016.item')
      .first()
      .click()
    await oppijaPageV2.tallenna()

    await expect(
      page
        .locator('.OsasuoritusRow__cellContent')
        .getByText('Huolto- ja korjaustyöt', { exact: true })
    ).toBeVisible()
  })
})
