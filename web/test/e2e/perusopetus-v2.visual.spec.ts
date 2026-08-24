import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'

/**
 * Visuaaliset regressiotestit perusopetuksen uudelle (v2) käyttöliittymälle.
 *
 * Toiminnalliset testit: perusopetus-v2-*.spec.ts
 * Ks. documentation/visual-testing.md
 *
 * Kuvat otetaan laajennetuista osasuorituksista, koska oppiaineen kenttärivit
 * (nimi + arvo) näkyvät vain avatussa rivissä. Perusopetus on tässä herkin
 * koulutusmuoto: oppiainetaulukoita on kaksi rinnakkain, jolloin sarakkeet
 * ovat puolet kapeampia kuin koko leveyden taulukoissa.
 *
 * Kaisan päättötodistuksella (tab 0) on katselutilassa kaksi laajennettavaa
 * oppiainetta; 8. vuosiluokka (tab 2) näyttää muokkaustilassa kaikkien
 * oppiaineiden kentät, myös pisimmät nimet ("Yksilöllistetty oppimäärä").
 */

const kaisa = '1.2.246.562.24.00000000007'
const url = `${kaisa}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const päättötodistusTab = 'oo.0.suoritusTabs.0.tab'
const vuosiluokkaTab = 'oo.0.suoritusTabs.2.tab'

test.describe('Perusopetus v2 – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  // Muita visuaalitestejä leveämpi ikkuna on tässä olennainen: oletusleveydellä
  // (1280 px) yhteiset ja valinnaiset oppiaineet pinoutuvat allekkain koko
  // leveyteen, jolloin juuri se kapea kaksipalstainen asettelu jää testaamatta.
  test.use({
    storageState: virkailija('kalle'),
    viewport: { width: 1440, height: 900 }
  })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Päättötodistus, laajennettu katselunäkymä', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(url)
    await page.getByTestId(päättötodistusTab).click()
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toBeVisible()
    await oppijaPageV2.openExpandableOsasuoritukset(2)
    await takeFullPageScreenshot(
      page,
      'perusopetus-paattotodistus-katselu-avattu.png'
    )
  })

  test('8. vuosiluokka, laajennettu muokkaustila', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input'
      )
    ).toBeVisible()
    await oppijaPageV2.openExpandableOsasuoritukset(23)
    await takeFullPageScreenshot(
      page,
      'perusopetus-vuosiluokka-muokkaus-avattu.png'
    )
  })
})
