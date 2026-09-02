import { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Ahvenanmaan perusopetuksen opiskeluoikeuden luonti uusi oppija -dialogissa.
 *
 * Ahvenanmaalla nuorten ja muiden kuin oppivelvollisten perusopetusta ei eroteta
 * opiskeluoikeuden tyypillä kuten manner-Suomessa (perusopetus /
 * aikuistenperusopetus), vaan päätason suorituksen tyypillä saman
 * ahvenanmaanperusopetus-opiskeluoikeuden sisällä. Kumpikin oppimäärä syntyy
 * siis pelkästään dialogin oppimäärävalinnasta, eikä sen takana ole
 * opiskeluoikeuden tyypin tasoista tarkistusta — siksi molemmat testataan
 * erikseen luontiin asti.
 *
 * Övernäs skola (Maarianhamina, kunta_478) on mockdatan ainoa ahvenanmaalainen
 * oppilaitos, ja OppilaitosServlet tarjoaa sille Ahvenanmaan perusopetuksen
 * manner-Suomen perusopetuksen tyyppien sijaan.
 */

const oppilaitos = 'Övernäs skola'
const opiskeluoikeus = 'Ahvenanmaan perusopetus'

const oppimääränKey = 'suorituksentyyppi_ahvenanmaanperusopetuksenoppimaara'
const aikuistenOppimääränKey =
  'suorituksentyyppi_ahvenanmaanperusopetuksenoppimaaraaikuiset'

// Oppivelvollinen oppilas ja muu kuin oppivelvollinen opiskelija.
const oppivelvollisenHetu = '210310A247H'
const aikuisenHetu = '090684-5181'

const aloituspäivä = new Date(2018, 7, 1)

/**
 * Molempien oppimäärien päätason suoritus näkyy editorissa samalla nimellä
 * ("Perusopetus"), joten käyttöliittymästä ei voi päätellä kumpi tyyppi
 * tallentui. Tarkistetaan se tallennetusta opiskeluoikeudesta.
 */
const haeLuotuOpiskeluoikeus = async (page: Page) => {
  const oid = page.url().match(/oppija\/(1\.2\.[\d.]+)/)?.[1]
  expect(oid).toBeDefined()
  const response = await page.request.get(`/koski/api/oppija/${oid}`)
  expect(response.ok()).toBeTruthy()
  const oppija = await response.json()
  expect(oppija.opiskeluoikeudet).toHaveLength(1)
  return oppija.opiskeluoikeudet[0]
}

test.describe('Ahvenanmaan perusopetuksen opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Oppimäärävalinnassa on vain oppimäärän suoritukset, ei vuosiluokkaa', async ({
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo(oppivelvollisenHetu)
    await uusiOppijaPage.fill({
      etunimet: 'Anton',
      sukunimi: 'Ålänning',
      oppilaitos,
      opiskeluoikeus
    })

    // Vuosiluokan suoritukset lisätään editorissa, ei tässä dialogissa.
    const oppimäärät = await uusiOppijaPage.controls.oppimäärä.options()
    expect(oppimäärät).toHaveLength(2)
    expect(oppimäärät).toContain('Ahvenanmaan perusopetuksen oppimäärä')
    expect(oppimäärät).toContain(
      'Ahvenanmaan perusopetuksen oppimäärä, muut kuin oppivelvolliset'
    )
  })

  test('Suorituskielen oletus on ruotsi', async ({ uusiOppijaPage }) => {
    await uusiOppijaPage.goTo(oppivelvollisenHetu)
    await uusiOppijaPage.fill({
      etunimet: 'Anton',
      sukunimi: 'Ålänning',
      oppilaitos,
      opiskeluoikeus
    })

    expect(await uusiOppijaPage.controls.suorituskieli.value()).toEqual(
      'ruotsi'
    )
  })

  test('Oppimäärän suorituksen luonti onnistuu', async ({
    uusiOppijaPage,
    page
  }) => {
    await uusiOppijaPage.goTo(oppivelvollisenHetu)
    await uusiOppijaPage.fill({
      etunimet: 'Anton',
      sukunimi: 'Ålänning',
      oppilaitos,
      opiskeluoikeus,
      aloituspäivä,
      opiskeluoikeudenTila: 'Läsnä'
    })
    await uusiOppijaPage.controls.oppimäärä.set(oppimääränKey)

    await uusiOppijaPage.submitAndExpectSuccess()

    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toHaveText(
      'Perusopetus'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
    ).toContainText('ruotsi')

    const opiskeluoikeus0 = await haeLuotuOpiskeluoikeus(page)
    expect(opiskeluoikeus0.tyyppi.koodiarvo).toEqual('ahvenanmaanperusopetus')
    expect(opiskeluoikeus0.suoritukset).toHaveLength(1)

    const suoritus = opiskeluoikeus0.suoritukset[0]
    expect(suoritus.tyyppi.koodiarvo).toEqual(
      'ahvenanmaanperusopetuksenoppimaara'
    )
    // Oppivelvollisen oppimäärän alkamispäivä tulee vuosiluokan suorituksilta,
    // joita dialogi ei luo (ks. KoskiValidator.validateAlkamispäivä).
    expect(suoritus.alkamispäivä).toBeUndefined()
    // Pakolliset oppiaineet esitäytetään avgångsbetygin kaavakkeelta.
    expect(suoritus.osasuoritukset).toHaveLength(17)
  })

  test('Muiden kuin oppivelvollisten oppimäärän suorituksen luonti onnistuu', async ({
    uusiOppijaPage,
    page
  }) => {
    await uusiOppijaPage.goTo(aikuisenHetu)
    await uusiOppijaPage.fill({
      etunimet: 'Bertil',
      sukunimi: 'Ålänning',
      oppilaitos,
      opiskeluoikeus,
      aloituspäivä,
      opiskeluoikeudenTila: 'Läsnä'
    })
    await uusiOppijaPage.controls.oppimäärä.set(aikuistenOppimääränKey)

    await uusiOppijaPage.submitAndExpectSuccess()

    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toHaveText(
      'Perusopetus'
    )

    const opiskeluoikeus0 = await haeLuotuOpiskeluoikeus(page)
    expect(opiskeluoikeus0.tyyppi.koodiarvo).toEqual('ahvenanmaanperusopetus')

    const suoritus = opiskeluoikeus0.suoritukset[0]
    expect(suoritus.tyyppi.koodiarvo).toEqual(
      'ahvenanmaanperusopetuksenoppimaaraaikuiset'
    )
    // Muiden kuin oppivelvollisten opiskeluoikeudella ei ole vuosiluokan
    // suorituksia, joten alkamispäivä kirjataan oppimäärän suoritukselle.
    expect(suoritus.alkamispäivä).toEqual('2018-08-01')
    expect(suoritus.osasuoritukset).toHaveLength(17)
  })
})
