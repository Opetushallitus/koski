import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Aikuisten perusopetuksen suoritustietoraportti', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan valitun suorituksen raportti massaluovutuksena', async ({
    raportitPage
  }) => {
    await raportitPage.gotoAikuistenPerusopetusSuoritustiedot()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.valitseRaportinTyyppi('Päättövaihe')
    await raportitPage.syötäAikajakso('1.1.2008', '31.12.2019')
    await raportitPage.rajaaOsasuorituksetAikajaksolle()
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      raportinTyyppi: 'päättövaihe',
      osasuoritustenAikarajaus: true
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2008 – 31.12.2019')
    await expect(raportitPage.raportinTyyppi(rivi)).toHaveText('Päättövaihe')
    await expect(rivi).toContainText('Aikajaksolta')
    await expect(
      raportitPage.raportit.locator('th', { hasText: /^Suoritus$/ })
    ).toHaveCount(1)
    await expect(
      raportitPage.raportit.locator('th', { hasText: /^Osasuoritukset$/ })
    ).toHaveCount(1)

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
