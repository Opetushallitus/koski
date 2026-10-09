import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('VST vapaatavoitteisen koulutuksen raportti', () => {
  test.use({ storageState: virkailija('varsinaissuomi-oppilaitos-pää') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoVstVapaatavoitteinen()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2022', '31.12.2023')
    await raportitPage.muodosta()

    await expect(raportitPage.taustallaOhje).toBeVisible()
    await expect(raportitPage.raporttiRivit).toHaveCount(1)

    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2022 – 31.12.2023')
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
