import { expect, test } from './base'
import { virkailija } from './setup/auth'
import {
  avaaOsasuoritusrivit,
  otaVakaaKuvakaappaus
} from './fragments/visualScreenshot'

// Toiminnalliset testit: ahvenanmaan-perusopetus.spec.ts
// Ks. documentation/visual-testing.md
//
// Oppiaineen kentät (Mukautettu oppimäärä, Suorituskieli, Suoritustapa) ovat
// tässä fixtuurissa tyhjiä, joten ne renderöityvät vain muokkaustilassa. Siksi
// vain muokkaustilan kuva otetaan rivit avattuina; katselunäkymissä ei ole
// laajennettavia rivejä.

const oppija = '1.2.246.562.24.00000000190'
const url = `${oppija}?opiskeluoikeudenTyyppi=ahvenanmaanperusopetus`

// Oletustabi (9. vuosiluokka) on tyhjä, joten kuvat otetaan tabeista 0 ja 2.
const päättötodistusTab = 'oo.0.suoritusTabs.0.tab'
const vuosiluokkaTab = 'oo.0.suoritusTabs.2.tab'

test.describe('Ahvenanmaan perusopetus – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Päättötodistus, katselunäkymä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(url)
    await page.getByTestId(päättötodistusTab).click()
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')
    ).toBeVisible()
    await otaVakaaKuvakaappaus(page, 'ahvenanmaa-paattotodistus-katselu.png')
  })

  test('8. vuosiluokka, katselunäkymä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await expect(page.locator('.oppiaineet')).toBeVisible()
    await otaVakaaKuvakaappaus(page, 'ahvenanmaa-vuosiluokka-katselu.png')
  })

  test('8. vuosiluokka, laajennettu muokkaustila', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(url)
    await page.getByTestId(vuosiluokkaTab).click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.2.osasuoritukset.0.arvosana.edit.input'
      )
    ).toBeVisible()
    await avaaOsasuoritusrivit(page)
    await otaVakaaKuvakaappaus(page, 'ahvenanmaa-vuosiluokka-muokkaus.png')
  })
})
