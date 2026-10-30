import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Ammatillisen koulutuksen tutkinnon osa/osia -suoritustietoraportti', () => {
  test.use({ storageState: virkailija('stadinammattiopisto-admin') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan aikajaksolle rajattu raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoAmmatillinenOsittainenSuoritustiedot()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2024', '31.12.2024')
    await raportitPage.rajaaOsasuorituksetAikajaksolle()
    await raportitPage.muodosta()

    await expect(raportitPage.taustallaOhje).toBeVisible()
    await expect(raportitPage.raporttiRivit).toHaveCount(1)

    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2024 – 31.12.2024')
    await expect(rivi).toContainText('Aikajaksolta')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
