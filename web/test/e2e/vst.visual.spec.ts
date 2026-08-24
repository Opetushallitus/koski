import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'

/**
 * Visuaaliset regressiotestit vapaan sivistystyön (VST) käyttöliittymälle.
 * Ottaa koko sivun kuvakaappaukset laajennetusta katselunäkymästä ja
 * muokkaustilasta.
 */

const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const kansanopisto = '1.2.246.562.24.00000000105'

test.describe('VST – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lukutaitokoulutus, laajennettu katselunäkymä', async ({
    page,
    vstOppijaPage
  }) => {
    await vstOppijaPage.goto(lukutaitokoulutus)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await vstOppijaPage.openAllOsasuoritukset()
    await takeFullPageScreenshot(page, 'vst-lukutaito-katselu-avattu.png')
  })

  test('Lukutaitokoulutus, muokkaustila', async ({ page, vstOppijaPage }) => {
    await vstOppijaPage.goto(lukutaitokoulutus)
    await vstOppijaPage.edit()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).toBeVisible()
    await vstOppijaPage.openAllOsasuoritukset()
    await takeFullPageScreenshot(page, 'vst-lukutaito-muokkaus-avattu.png')
  })

  test('JOTPA-koulutus, laajennettu katselunäkymä', async ({
    page,
    vstOppijaPage
  }) => {
    await vstOppijaPage.goto(jotpaKoulutus)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await vstOppijaPage.openAllOsasuoritukset()
    await takeFullPageScreenshot(page, 'vst-jotpa-katselu-avattu.png')
  })

  test('Kansanopisto, laajennettu katselunäkymä', async ({
    page,
    vstOppijaPage
  }) => {
    await vstOppijaPage.goto(kansanopisto)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await vstOppijaPage.openAllOsasuoritukset()
    await takeFullPageScreenshot(page, 'vst-kansanopisto-katselu-avattu.png')
  })

  test('Kansanopisto, muokkaustila', async ({ page, vstOppijaPage }) => {
    await vstOppijaPage.goto(kansanopisto)
    await vstOppijaPage.edit()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).toBeVisible()
    await vstOppijaPage.openAllOsasuoritukset()
    await takeFullPageScreenshot(page, 'vst-kansanopisto-muokkaus-avattu.png')
  })
})
