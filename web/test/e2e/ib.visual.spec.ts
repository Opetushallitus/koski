import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'

// Toiminnalliset testit: ib.spec.ts
// Ks. documentation/visual-testing.md

// Samalla oppijalla on kaksi päätason suoritusta: 0 = Pre-IB, 1 = IB-tutkinto.
const preIB2015JaTutkinto = '1.2.246.562.24.00000000060'
const preIB2019 = '1.2.246.562.24.00000000062'

test.describe('IB – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Pre-IB 2015, katselunäkymä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(preIB2015JaTutkinto)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await takeFullPageScreenshot(page, 'ib-pre-ib-2015-katselu.png')
  })

  test('Pre-IB 2015, muokkaustila', async ({
    page,
    oppijaPage,
    ibOppijaPage
  }) => {
    await oppijaPage.goto(preIB2015JaTutkinto)
    await ibOppijaPage.edit()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).toBeVisible()
    await takeFullPageScreenshot(page, 'ib-pre-ib-2015-muokkaus.png')
  })

  test('IB-tutkinto, katselunäkymä', async ({
    page,
    oppijaPage,
    ibOppijaPage
  }) => {
    await oppijaPage.goto(preIB2015JaTutkinto)
    await ibOppijaPage.selectSuoritus(1)
    await expect(page.getByTestId('oo.0.suoritukset.1.koulutus')).toBeVisible()
    await takeFullPageScreenshot(page, 'ib-tutkinto-katselu.png')
  })

  test('Pre-IB 2019, katselunäkymä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto(preIB2019)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await takeFullPageScreenshot(page, 'ib-pre-ib-2019-katselu.png')
  })
})
