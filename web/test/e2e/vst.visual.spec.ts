import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { Page } from '@playwright/test'
import {
  odotaAsettelunVakiintuminen,
  otaVakaaKuvakaappaus
} from './fragments/visualScreenshot'

/**
 * Visuaaliset regressiotestit vapaan sivistystyön (VST) käyttöliittymälle.
 * Ottaa koko sivun kuvakaappaukset laajennetusta katselunäkymästä ja
 * muokkaustilasta.
 */

const lukutaitokoulutus = '1.2.246.562.24.00000000107'
const jotpaKoulutus = '1.2.246.562.24.00000000140'
const kansanopisto = '1.2.246.562.24.00000000105'

const avaaKaikki = async (page: Page) => {
  await odotaAsettelunVakiintuminen(page)
  const avaaNappi = page.getByRole('button', { name: 'Avaa kaikki' })
  if (await avaaNappi.isVisible()) {
    await avaaNappi.click()
    await expect(
      page.getByRole('button', { name: 'Sulje kaikki' })
    ).toBeVisible()
  }
}

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
    await avaaKaikki(page)
    await otaVakaaKuvakaappaus(page, 'vst-lukutaito-katselu-avattu.png')
  })

  test('Lukutaitokoulutus, muokkaustila', async ({ page, vstOppijaPage }) => {
    await vstOppijaPage.goto(lukutaitokoulutus)
    await vstOppijaPage.edit()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).toBeVisible()
    await avaaKaikki(page)
    await otaVakaaKuvakaappaus(page, 'vst-lukutaito-muokkaus-avattu.png')
  })

  test('JOTPA-koulutus, laajennettu katselunäkymä', async ({
    page,
    vstOppijaPage
  }) => {
    await vstOppijaPage.goto(jotpaKoulutus)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await avaaKaikki(page)
    await otaVakaaKuvakaappaus(page, 'vst-jotpa-katselu-avattu.png')
  })

  test('Kansanopisto, laajennettu katselunäkymä', async ({
    page,
    vstOppijaPage
  }) => {
    await vstOppijaPage.goto(kansanopisto)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toBeVisible()
    await avaaKaikki(page)
    await otaVakaaKuvakaappaus(page, 'vst-kansanopisto-katselu-avattu.png')
  })

  test('Kansanopisto, muokkaustila', async ({ page, vstOppijaPage }) => {
    await vstOppijaPage.goto(kansanopisto)
    await vstOppijaPage.edit()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).toBeVisible()
    await avaaKaikki(page)
    await otaVakaaKuvakaappaus(page, 'vst-kansanopisto-muokkaus-avattu.png')
  })
})
