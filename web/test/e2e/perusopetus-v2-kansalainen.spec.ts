import { Page } from '@playwright/test'
import { expect, test } from './base'
import { kansalainen } from './setup/auth'

/**
 * Perusopetus uudessa käyttöliittymässä kansalaisen näkymissä: omat tiedot ja
 * suoritusjako. Kaisa Koululainen (220109-784L) on kirjautuneena omilla
 * tunnuksillaan.
 *
 * Kansalaisen näkymissä opiskeluoikeuksilla on oma oo.N-testId-juurensa
 * oppilaitoksittain, joten indeksin sijaan perusopetus valitaan otsikosta.
 */

const jyväskylänNormaalikoulu = '1.2.246.562.10.14613773812'

const suoritustabit = (page: Page) =>
  page.getByTestId(/^oo\.\d+\.suoritusTabs\.\d+\.tab$/)

test.describe('Perusopetuksen uusi käyttöliittymä: kansalaisen näkymät', () => {
  test.use({ storageState: kansalainen('220109-784L') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test('Omat tiedot: perusopetus avautuu ilman muokkaustoimintoja', async ({
    page,
    kansalainenPage
  }) => {
    await kansalainenPage.goto()
    await page
      .getByTestId(/^oo\.\d+\.opiskeluoikeus\.expand$/)
      .filter({ hasText: 'Perusopetus' })
      .click()

    await expect(suoritustabit(page)).toHaveText([
      'Päättötodistus',
      '9. vuosiluokka',
      '8. vuosiluokka',
      '7. vuosiluokka'
    ])
    await expect(page.getByTestId(/opiskeluoikeus\.edit$/)).toHaveCount(0)
    await expect(page.getByText('Versiohistoria')).toHaveCount(0)

    // Oppija näkee omasta suorituksestaan myös luottamuksellisen
    // yksilöllistetyn oppimäärän merkinnän.
    await suoritustabit(page).filter({ hasText: 'Päättötodistus' }).click()
    await expect(
      page.getByTestId(/^oo\.\d+\.suoritukset\.0\.osasuoritukset\.11\.footnote$/)
    ).toHaveText('*')
  })

  test('Suoritusjako: jaettu vuosiluokka näkyy suoritusotteella', async ({
    page,
    kansalainenPage
  }) => {
    await kansalainenPage.goto()
    await kansalainenPage.openJaaSuoritustietoja()
    await kansalainenPage
      .suoritustietoLabel(
        jyväskylänNormaalikoulu,
        'perusopetuksenvuosiluokka',
        '8'
      )
      .click()
    await kansalainenPage.jaaValitsemasiOpinnotButton().click()

    const suoritusotePopup = page.waitForEvent('popup')
    await kansalainenPage.katsoSuoritusoteLink().click()
    const ote = await suoritusotePopup

    await ote.getByTestId(/^oo\.\d+\.opiskeluoikeus\.expand$/).click()
    await expect(suoritustabit(ote)).toHaveText(['8. vuosiluokka'])
    await expect(
      ote.getByTestId(/^oo\.\d+\.suoritukset\.0\.luokka\.value$/)
    ).toHaveText('8C')
  })
})
