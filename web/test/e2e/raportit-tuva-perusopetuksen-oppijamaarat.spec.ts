import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Tutkintokoulutukseen valmentavan koulutuksen oppijamääräraportti (perusopetuksen järjestämislupa)', () => {
  test.use({ storageState: virkailija('stadinammattiopisto-admin') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan päivän raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoTuvaPerusopetuksenOppijamaarat()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäPäivä('1.1.2022')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({ paiva: '2022-01-01' })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2022')
    await expect(
      raportitPage.raportit.locator('th', { hasText: /^Päivä$/ })
    ).toHaveCount(1)

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
