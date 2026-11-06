import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Lukiokoulutuksen opiskelijamääräraportti (lukio, DIA, IB, International School, ESH)', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan päivän raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoLukiokoulutuksenOpiskelijamaarat()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäPäivä('15.1.2016')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({ paiva: '2016-01-15' })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('15.1.2016')
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
