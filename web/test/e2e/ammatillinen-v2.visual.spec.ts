import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'

/**
 * Visuaaliset regressiotestit ammatillisen tutkinnon uudelle (v2)
 * käyttöliittymälle. Ottaa koko sivun kuvakaappaukset laajennetusta
 * katselunäkymästä ja muokkaustilasta.
 */

// Osittainen ammatillinen tutkinto. Vain osittainen renderöityy v2-editorilla;
// koko tutkinto ja muu ammatillinen käyttävät yhä vanhaa (ks. useUiAdapter).
const osittainenTutkinto = '1.2.246.562.24.00000000055'
// Ammatillisen tutkinnon osia useasta tutkinnosta (myös osittainen -> v2)
const useastaTutkinnosta = '1.2.246.562.24.00000000182'

test.describe('Ammatillinen v2 – visuaaliset regressiot', () => {
  // Baseline-kuvat ovat Linux-renderöityjä eivätkä kelpaa muilla
  // käyttöjärjestelmillä. Skipataan näkyvästi, jotta testien olemassaolo ei
  // jää huomaamatta. Ks. documentation/visual-testing.md
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Osittainen tutkinto, laajennettu katselunäkymä', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(osittainenTutkinto)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await oppijaPageV2.openAllOsasuoritukset()
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana'
      )
    ).toBeVisible()
    await takeFullPageScreenshot(
      page,
      'ammatillinen-osittainen-katselu-avattu.png'
    )
  })

  test('Osittainen tutkinto, muokkaustila', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(osittainenTutkinto)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    // Odotetaan enabled eikä pelkkää visible: Select renderöityy näkyviin heti,
    // mutta pitää syötteensä disabloituna kunnes koodisto on ladattu ja
    // vaihtoehdot laskettu. Pelkkä toBeVisible läpäisisi jo ennen sitä, jolloin
    // kuva voitaisiin ottaa kesken renderöinnin.
    await expect(
      page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.input')
    ).toBeEnabled()
    await takeFullPageScreenshot(page, 'ammatillinen-osittainen-muokkaus.png')
  })

  test('Useasta tutkinnosta, laajennettu katselunäkymä', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(useastaTutkinnosta)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await oppijaPageV2.openAllOsasuoritukset()
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana'
      )
    ).toBeVisible()
    await takeFullPageScreenshot(
      page,
      'ammatillinen-useasta-tutkinnosta-katselu-avattu.png'
    )
  })
})
