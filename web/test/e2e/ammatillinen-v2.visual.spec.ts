import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { Page } from '@playwright/test'
import { otaVakaaKuvakaappaus } from './fragments/visualScreenshot'

/**
 * Visuaaliset regressiotestit ammatillisen tutkinnon uudelle (v2)
 * käyttöliittymälle. Ottaa koko sivun kuvakaappaukset laajennetusta
 * katselunäkymästä ja muokkaustilasta.
 */

// Osittainen ammatillinen tutkinto (renderöityy v2:na ?ammatillinen-v2=true -lipulla)
const osittainenTutkinto = '1.2.246.562.24.00000000055'
// Ammatillisen tutkinnon osia useasta tutkinnosta (renderöityy v2:na oletuksena)
const useastaTutkinnosta = '1.2.246.562.24.00000000182'

// Avaa kaikki osasuoritukset, jotta myös alikentät näkyvät kuvassa.
const avaaKaikki = async (page: Page) => {
  const nappi = page.getByRole('button', { name: 'Avaa kaikki' })
  if (await nappi.isVisible()) {
    await nappi.click()
  }
}

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
    oppijaPage
  }) => {
    await oppijaPage.goto(`${osittainenTutkinto}?ammatillinen-v2=true`)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await avaaKaikki(page)
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana'
      )
    ).toBeVisible()
    await otaVakaaKuvakaappaus(
      page,
      'ammatillinen-osittainen-katselu-avattu.png'
    )
  })

  test('Osittainen tutkinto, muokkaustila', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(`${osittainenTutkinto}?ammatillinen-v2=true`)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.input')
    ).toBeVisible()
    await otaVakaaKuvakaappaus(page, 'ammatillinen-osittainen-muokkaus.png')
  })

  test('Useasta tutkinnosta, laajennettu katselunäkymä', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(useastaTutkinnosta)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await avaaKaikki(page)
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana'
      )
    ).toBeVisible()
    await otaVakaaKuvakaappaus(
      page,
      'ammatillinen-useasta-tutkinnosta-katselu-avattu.png'
    )
  })
})
