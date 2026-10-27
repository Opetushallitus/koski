import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Lukiokoulutuksen suoritustietoraportti (lukio 2019)', () => {
  test.use({ storageState: virkailija('jyvaskyla-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan raportti massaluovutuksena valitun päivän kotikunnalla', async ({
    raportitPage
  }) => {
    await raportitPage.gotoLukio2019Suoritustiedot()
    await expect(raportitPage.raportit).toBeHidden()

    await raportitPage.syötäAikajakso('1.1.2019', '31.12.2021')
    await raportitPage.rajaaOsasuorituksetAikajaksolle()
    await raportitPage.syötäKotikuntaPvm('1.1.2020')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      kotikuntaPvm: '2020-01-01',
      osasuoritustenAikarajaus: true
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('1.1.2019 – 31.12.2021')
    await expect(rivi).toContainText('Aikajaksolta')
    await expect(raportitPage.kotikuntaPvm(rivi)).toHaveText('1.1.2020')
    await expect(
      raportitPage.raportit.locator('th', { hasText: 'Kurssit' })
    ).toHaveCount(1)

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
