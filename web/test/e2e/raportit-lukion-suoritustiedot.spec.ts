import { test, expect } from './base'
import { virkailija } from './setup/auth'
import { formatFinnishDate } from '../../app/date/date'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Lukiokoulutuksen suoritustietoraportti', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan raportit massaluovutuksena nykyisellä ja valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoLukionSuoritustiedot()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2012', '31.12.2016')
    const oletuskysely = await raportitPage.muodostaJaLueKysely()
    expect(oletuskysely.kotikuntaPvm).toBeUndefined()

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const oletusrivi = raportitPage.raporttiRivit.first()
    await expect(oletusrivi).toContainText('1.1.2012 – 31.12.2016')
    await expect(oletusrivi).toContainText('Kaikki')
    await expect(raportitPage.kotikuntaPvm(oletusrivi)).toHaveText(
      formatFinnishDate(new Date())!
    )
    await expect(raportitPage.latauslinkki(oletusrivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })

    await raportitPage.rajaaOsasuorituksetAikajaksolle()
    await raportitPage.syötäKotikuntaPvm('1.1.2015')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      kotikuntaPvm: '2015-01-01',
      osasuoritustenAikarajaus: true
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(2)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('Aikajaksolta')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2015')
    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(
      raportitPage.raportit.locator('th', { hasText: 'Kurssit' })
    ).toHaveCount(1)
  })
})
