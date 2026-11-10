import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000
const helsinginKaupunki = '1.2.246.562.10.346830761110'

test.describe('Esiopetusraportti', () => {
  test.use({ storageState: virkailija('hki-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan koulutustoimijan raportti massaluovutuksena valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoEsiopetus()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.valitseOrganisaatio('Helsingin kaupunki')
    await raportitPage.syötäPäivä('1.1.2015')
    await raportitPage.syötäKotikuntaPvm('1.1.2014')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      type: 'esiopetus',
      organisaatioOid: helsinginKaupunki,
      paiva: '2015-01-01',
      kotikuntaPvm: '2014-01-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('Helsingin kaupunki')
    await expect(rivi).toContainText('1.1.2015')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2014')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
  })

  test('Muodostetaan ostopalvelu/palveluseteli-raportti massaluovutuksena', async ({
    raportitPage
  }) => {
    await raportitPage.gotoEsiopetus()

    await raportitPage.valitseOrganisaatio('Ostopalvelu/palveluseteli')
    await raportitPage.syötäPäivä('1.1.2015')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      type: 'esiopetus',
      organisaatioOid: 'OSTOPALVELUTAIPALVELUSETELI',
      paiva: '2015-01-01'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('Ostopalvelu/palveluseteli')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
  })
})
