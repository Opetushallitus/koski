import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000
const helsinginKaupunki = '1.2.246.562.10.346830761110'

test.describe('Kunnan oppijat', () => {
  test.use({ storageState: virkailija('hki-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan kunnan raportti massaluovutuksena', async ({
    raportitPage
  }) => {
    await raportitPage.gotoKunnanOppijat()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.valitseOrganisaatio('Helsingin kaupunki')
    await raportitPage.syötäPäivä('1.9.2026')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      type: 'kunnanOppijat',
      organisaatioOid: helsinginKaupunki,
      paiva: '2026-09-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('Helsingin kaupunki')
    await expect(rivi).toContainText('1.9.2026')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
  })
})
