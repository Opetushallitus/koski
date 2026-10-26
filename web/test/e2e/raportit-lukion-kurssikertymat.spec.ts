import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Lukiokoulutuksen kurssikertymäraportti', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan raportti massaluovutuksena ja se ilmestyy omiin raportteihin', async ({
    raportitPage
  }) => {
    await raportitPage.gotoLukionKurssikertymat()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2012', '31.12.2016')
    await raportitPage.muodosta()

    await expect(raportitPage.taustallaOhje).toBeVisible()
    await expect(raportitPage.raporttiRivit).toHaveCount(1)

    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2012 – 31.12.2016')
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
