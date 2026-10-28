import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Perusopetuksen vuosiluokkaraportti', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan vuosiluokan raportti massaluovutuksena valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoPerusopetuksenVuosiluokka()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäPäivä('1.1.2012')
    await raportitPage.valitseVuosiluokka('9')
    await raportitPage.syötäKotikuntaPvm('1.1.2011')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      paiva: '2012-01-01',
      vuosiluokka: '9',
      kotikuntaPvm: '2011-01-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2012')
    await expect(raportitPage.vuosiluokka(rivi)).toHaveText('9')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2011')
    await expect(
      raportitPage.raportit.locator('th', { hasText: /^Vuosiluokka$/ })
    ).toHaveCount(1)

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
