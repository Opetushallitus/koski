import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('IB-suoritustietoraportti', () => {
  test.use({ storageState: virkailija('hki-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan valitun suorituksen raportti massaluovutuksena valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoIBSuoritustiedot()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.valitseRaportinTyyppi('Pre-IB-opintojen suoritukset')
    await raportitPage.syötäAikajakso('1.1.2012', '31.12.2016')
    await raportitPage.rajaaOsasuorituksetAikajaksolle()
    await raportitPage.syötäKotikuntaPvm('1.1.2015')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      raportinTyyppi: 'preiboppimaara',
      osasuoritustenAikarajaus: true,
      kotikuntaPvm: '2015-01-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2012 – 31.12.2016')
    await expect(raportitPage.raportinTyyppi(rivi)).toHaveText(
      'Pre-IB-opintojen suoritukset'
    )
    await expect(rivi).toContainText('Aikajaksolta')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2015')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
