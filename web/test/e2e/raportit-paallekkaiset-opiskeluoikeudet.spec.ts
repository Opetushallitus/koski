import { test, expect } from './base'
import { virkailija } from './setup/auth'

const raportinMuodostusAikakatkaisuMs = 30_000

test.describe('Päällekkäisten opiskeluoikeuksien raportti', () => {
  test.use({ storageState: virkailija('hki-tallentaja') })

  test.beforeEach(async ({ fixtures }) => {
    // Raportointikanta tarvitaan raporttinäkymän avaamiseen, ja reset tyhjentää
    // samalla aiempien ajojen massaluovutuskyselyt.
    await fixtures.reset(true)
  })

  test('Muodostetaan valitun organisaation raportti massaluovutuksena', async ({
    raportitPage
  }) => {
    await raportitPage.gotoPaallekkaisetOpiskeluoikeudet()
    await expect(raportitPage.raportit).toBeHidden()
    await expect(raportitPage.muodostaButton).toBeDisabled()

    await raportitPage.valitseOrganisaatio('Helsingin kaupunki')
    await raportitPage.syötäAikajakso('1.1.2018', '31.12.2019')
    const kysely = await raportitPage.muodostaJaLueKysely()
    expect(kysely).toMatchObject({
      format:
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
    })

    await expect(raportitPage.raporttiRivit).toHaveCount(1)
    const rivi = raportitPage.raporttiRivit.first()
    await expect(rivi).toContainText('Helsingin kaupunki')
    await expect(rivi).toContainText('1.1.2018 – 31.12.2019')

    await expect(raportitPage.latauslinkki(rivi)).toBeVisible({
      timeout: raportinMuodostusAikakatkaisuMs
    })
    await expect(raportitPage.salasana(rivi)).not.toBeEmpty()
    await expect(raportitPage.taustallaOhje).toBeHidden()
  })
})
