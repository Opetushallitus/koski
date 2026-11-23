import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000
const övernäsSkola = '1.2.246.562.10.31756453904'

test.describe('Ahvenanmaan perusopetuksen vuosiluokkaraportti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan vuosiluokan raportti massaluovutuksena valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoAhvenanmaanPerusopetuksenVuosiluokka()
    await expect(raportitPage.valittuKategoria).toHaveText(
      'Ahvenanmaan perusopetus'
    )
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäPäivä('15.5.2026')
    await raportitPage.valitseVuosiluokka('9')
    await raportitPage.syötäKotikuntaPvm('1.1.2026')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      type: 'ahvenanmaanPerusopetuksenVuosiluokka',
      organisaatioOid: övernäsSkola,
      paiva: '2026-05-15',
      vuosiluokka: '9',
      kotikuntaPvm: '2026-01-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('15.5.2026')
    await expect(raportitPage.vuosiluokka(rivi)).toHaveText('9')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2026')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
  })
})
