import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Muun ammatillisen koulutuksen suoritustietoraportti', () => {
  test.use({ storageState: virkailija('stadinammattiopisto-admin') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoMuuAmmatillinen()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2024', '31.12.2024')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      type: 'muuAmmatillinen',
      alku: '2024-01-01',
      loppu: '2024-12-31'
    })

    await expect(raportitPage.taustallaOhje).toBeVisible()
    await expect(raportitPage.raporttiRivit).toHaveCount(1)

    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2024 – 31.12.2024')
    await expect(
      raportitPage.raportit.locator('th', { hasText: 'Tutkinnon osat' })
    ).toHaveCount(0)

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
