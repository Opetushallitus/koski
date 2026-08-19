import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Validointivirheet pitää näkyä sillä kentällä, jota ne koskevat — myös silloin kun kentän arvo
 * puuttuu. Aiemmin util/optics.ts:n parsePath ei osannut muodostaa polkua tyhjälle arvolle, jolloin
 * virhe päätyi ainoana näkyviin "Poista suoritus" -painikkeelle, joka on kytketty koko suoritukset-
 * taulukkoon.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const kaikkiVirheet = '[data-testid$=".errors"]'

/**
 * Kentät, jotka on merkitty virheelliseksi omalla tilallaan. Mekaanisista
 * virheistä (tyhjä kenttä, kelvoton päivämäärä) ei näytetä tekstiä lainkaan,
 * vaan pelkkä punainen kenttä - ks. components-v2/forms/FieldErrors.
 */
const virheellisetKentät =
  '.TextEdit__input--error, .Select--error, .DateEdit__input--error, .NumberField__input--error'

test.describe('Perusopetuksen uusi käyttöliittymä: virheiden kohdistus', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Tyhjennetty luokka näyttää virheen luokkakentällä eikä muualla', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    // 8. vuosiluokka
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const luokka = page.getByTestId('oo.0.suoritukset.2.luokka.edit.input')
    await expect(luokka).toBeVisible()
    await expect(page.locator(kaikkiVirheet)).toHaveCount(0)

    await luokka.fill('')

    // Tyhjä pakollinen kenttä merkitään punaisella, ei tekstillä, joten
    // kohdistus näkyy siinä että täsmälleen yksi kenttä - luokka - on
    // merkitty virheelliseksi eikä tekstirivejä synny lainkaan.
    await expect(luokka).toHaveClass(/TextEdit__input--error/)
    await expect(page.locator(virheellisetKentät)).toHaveCount(1)
    await expect(page.locator(kaikkiVirheet)).toHaveCount(0)

    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()

    // Arvon palautus poistaa virheen
    await luokka.fill('8A')
    await expect(luokka).not.toHaveClass(/TextEdit__input--error/)
    await expect(page.locator(virheellisetKentät)).toHaveCount(0)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeEnabled()
  })

  test('Puuttuva arvosana vahvistetulla suorituksella näkyy vain osasuorituksella', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    // Päättötodistus on vahvistettu
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await page.getByPlaceholder('Lisää pakollinen oppiaine').click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^A2-kieli$/ })
      .first()
      .click()

    // Virhe kohdistuu lisätyn osasuorituksen arvosanaan eikä esim. päätason suoritukseen.
    const virheet = page.locator(kaikkiVirheet)
    await expect(virheet).toHaveCount(1)
    await expect(virheet.first()).toHaveAttribute(
      'data-testid',
      /^oo\.0\.suoritukset\.0\.osasuoritukset\.\d+\.errors$/
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()
  })
})
