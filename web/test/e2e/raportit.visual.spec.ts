import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'
import { KoskiRaportitPage } from './pages/virkailija/KoskiRaportitPage'

const avaa = async (raportitPage: KoskiRaportitPage, polku: string) => {
  await raportitPage.goto(polku)
  await expect(raportitPage.päivitysaika).toBeVisible()
}

test.describe('Raportit – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset(true)
  })

  test.beforeEach(async ({ page }) => {
    // Päivämääräkenttien oletusarvo on kuluva päivä
    await page.clock.setFixedTime(new Date('2025-12-24T12:00:00Z'))
  })

  test('Päivän raportti kotikuntapäivällä', async ({ page, raportitPage }) => {
    await avaa(raportitPage, '/esiopetus/esiopetuksenraportti')
    await expect(raportitPage.valittuRaportti).toHaveText(
      'Opiskeluoikeus- ja suoritustiedot'
    )
    await expect(raportitPage.kotikuntaPvmKenttä).toHaveValue('24.12.2025')
    await takeFullPageScreenshot(page, 'raportit-paiva-kotikunta.png')
  })

  test('Vuosiluokan raportti', async ({ page, raportitPage }) => {
    await avaa(raportitPage, '/perusopetus/perusopetuksenvuosiluokka')
    await expect(raportitPage.valittuRaportti).toHaveText(
      'Opiskeluoikeus- ja suoritustiedot'
    )
    await takeFullPageScreenshot(page, 'raportit-vuosiluokka.png')
  })

  test('Aikajakson raportti', async ({ page, raportitPage }) => {
    await avaa(raportitPage, '/lukio/lukiokurssikertyma')
    await expect(raportitPage.valittuRaportti).toHaveText(
      'Tunnusluvut: kurssikertymät'
    )
    await takeFullPageScreenshot(page, 'raportit-aikajakso.png')
  })

  test('Aikajakson raportti osasuoritusten rajauksella', async ({
    page,
    raportitPage
  }) => {
    await avaa(raportitPage, '/lukio/lukionsuoritustietojentarkistus')
    await expect(raportitPage.valittuRaportti).toHaveText(
      'Opiskeluoikeus- ja suoritustiedot'
    )
    await takeFullPageScreenshot(page, 'raportit-aikajakso-rajaus.png')
  })

  test('Aikajakson raportti tyyppivalinnalla yhden raportin kategoriassa', async ({
    page,
    raportitPage
  }) => {
    await avaa(raportitPage, '/ib/ibsuoritustietojentarkistus')
    await expect(raportitPage.otsikko).toHaveText('IB-koulutuksen raportit')
    await takeFullPageScreenshot(page, 'raportit-aikajakso-tyyppivalinta.png')
  })

  test('Päällekkäisten opiskeluoikeuksien raportti ilman organisaatiovalintaa', async ({
    page,
    raportitPage
  }) => {
    await avaa(raportitPage, '/muut/paallekkaisetopiskeluoikeudet')
    await expect(raportitPage.valittuRaportti).toHaveText(
      'Päällekkäiset opiskeluoikeudet'
    )
    await expect(raportitPage.organisaatiokenttä).toHaveValue('')
    await takeFullPageScreenshot(page, 'raportit-paallekkaiset.png')
  })

  test.describe('Pääkäyttäjä', () => {
    test.use({ storageState: virkailija('pää') })

    test('Organisaatiolista auki', async ({ page, raportitPage }) => {
      await avaa(raportitPage, '/lukio/lukionsuoritustietojentarkistus')
      await raportitPage.organisaatiokenttä.click()
      await expect(raportitPage.organisaatiolistaAuki).toBeVisible()
      await takeFullPageScreenshot(page, 'raportit-organisaatiot-auki.png')
    })
  })
})
