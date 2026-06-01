import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:832-946 "Vieraan kielen valinta"
 * v2-editorille. Kielioppiaineiden (esim. B1, A1) koulutusmoduuli.kieli
 * on muokattavissa inline oppiainerivillä.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: kielioppiaineen kielivalinta', () => {
  test.use({ storageState: virkailija('kalle') })

  test('B1-kielen kielivalinnan muuttaminen valinnaiselle oppiaineelle', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    // Päättötodistus (tab 0) sisältää kaikki oppiaineet.
    // Valinnainen B1-kieli (ruotsi) on osasuoritukset-listan indeksissä 2.
    // V2:n oppiainenimi on kaksiosainen: oppiaineen nimi ("B1-kieli, ") +
    // inline-kielikenttä (view-modessa KieliNimiLowerView = "ruotsi").
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.nimi')
    ).toContainText('B1-kieli')
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.kieli.value')
    ).toContainText('ruotsi')

    // Siirry muokkaustilaan
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Muuta kieli saksaksi inline-Select-kentällä rivin otsikossa
    const kieliSelect = page.getByTestId(
      'oo.0.suoritukset.0.osasuoritukset.2.kieli.edit.input'
    )
    await expect(kieliSelect).toBeVisible()
    await kieliSelect.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^saksa$/ })
      .first()
      .click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Rivin kieli-osio päivittyy saksaksi (pienellä kirjaimella)
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.2.kieli.value')
    ).toContainText('saksa')
  })

  test('Uuden A-kielioppiaineen lisäys näyttää inline-kielivalinnan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const valinnaisetSection = page.getByTestId('oppiaineet-valinnaiset')
    await valinnaisetSection
      .getByTestId('oo.0.suoritukset.0.uusi-oppiaine.input')
      .click()
    await expect(
      page.locator('.Select__optionLabel').filter({ hasText: /^A-kieli$/ })
    ).toHaveCount(0)
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^A2-kieli$/ })
      .click()

    await expect(
      valinnaisetSection
        .locator('[data-testid$=".nimi"]')
        .filter({ hasText: /^A2-kieli/ })
    ).toBeVisible()

    const uusiKieliSelect = valinnaisetSection
      .locator('[data-testid$=".kieli.edit.input"]')
      .last()
    await expect(uusiKieliSelect).toBeVisible()
    await uusiKieliSelect.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^ranska$/ })
      .click()

    const uusiArvosana = valinnaisetSection
      .locator('[data-testid$=".arvosana.edit.input"]')
      .last()
    await uusiArvosana.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^9$/ })
      .click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    await expect(
      valinnaisetSection
        .locator('[data-testid$=".nimi"]')
        .filter({ hasText: /^A2-kieli/ })
    ).toBeVisible()
    await expect(
      valinnaisetSection.locator('[data-testid$=".kieli.value"]').last()
    ).toContainText('ranska')
  })
})
