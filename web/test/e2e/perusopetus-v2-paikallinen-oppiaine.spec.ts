import type { Locator } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1285-1367 "Uuden paikallisen oppiaineen
 * lisääminen" v2-editorille.
 *
 * V2 toteutus:
 * - Uusi "Lisää paikallinen oppiaine" -valinta oppiaineen pudotusvalikossa
 * - Uuden paikallisen oppiaineen rivi sisältää koodi/nimi/kuvaus-kentät
 * - Tallennuksen jälkeen oppiaine säilyy preferenceissä, jolloin se löytyy
 *   pudotusvalikosta myös seuraavilla muokkauskerroilla
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const expectLabelsInOrder = async (labelsLocator: Locator, labels: string[]) => {
  const actual = await labelsLocator.allTextContents()
  let previousIndex = -1

  for (const label of labels) {
    const index = actual.findIndex(
      (actualLabel, actualIndex) =>
        actualIndex > previousIndex && actualLabel.trim() === label
    )
    expect(index, `${label} pitäisi löytyä järjestyksessä`).toBeGreaterThan(
      previousIndex
    )
    previousIndex = index
  }
}

test.describe('Perusopetuksen uusi käyttöliittymä: paikallinen oppiaine', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Paikallisen oppiaineen lisäys, kentät ja tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Päättötodistus (tab 0) käyttää groupattua näkymää pakolliset/valinnaiset
    // -jaolla. Lisätään uusi paikallinen valinnainen oppiaine.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    const valinnaisetDropdown = page
      .locator('[data-testid="oppiaineet-valinnaiset"]')
      .locator('[data-testid$=".uusi-oppiaine.input"]')

    await valinnaisetDropdown.click()
    await page
      .locator('[data-testid$=".uusi-paikallinen-oppiaine.item"]')
      .first()
      .click()

    // Uusi paikallinen oppiaine lisätään suoritusten loppuun. Päättötodistuksella
    // on 23 oppiainetta fixturessa, uuden osasuorituksen indeksi on 23.
    const newIndex = 23
    const koodiInput = page.getByTestId(
      `oo.0.suoritukset.0.osasuoritukset.${newIndex}.paikallinenKoodi.edit.input`
    )
    const nimiInput = page.getByTestId(
      `oo.0.suoritukset.0.osasuoritukset.${newIndex}.paikallinenNimi.edit.input`
    )
    const kuvausInput = page.locator(
      `textarea[data-testid="oo.0.suoritukset.0.osasuoritukset.${newIndex}.properties.paikallinenKuvaus.edit.input"]`
    )

    await expect(koodiInput).toBeVisible()
    await expect(nimiInput).toBeVisible()
    await expect(kuvausInput).toBeVisible()
    await expect(
      page.getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.properties.luokkaAste.edit.input`
      )
    ).toBeVisible()

    const properties = kuvausInput.locator('xpath=ancestor::section[2]')
    await expectLabelsInOrder(properties.locator('.OsasuoritusPropertyLabel'), [
      'Kuvaus',
      'Yksilöllistetty oppimäärä',
      'Rajattu oppimäärä',
      'Painotettu opetus',
      'Suorituskieli',
      'Suoritustapa',
      'Luokka-aste'
    ])

    // Täytä kentät
    await koodiInput.fill('TNS')
    await nimiInput.fill('Tanssi')
    await kuvausInput.fill('Kurssilla tanssitaan paljon\nMyös paritanssia')

    // Anna arvosana
    await page
      .getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.arvosana.edit.input`
      )
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^7$/ })
      .first()
      .click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Tarkista että Tanssi 7 näkyy oppiaineissa
    await expect(
      page.getByTestId(`oo.0.suoritukset.0.osasuoritukset.${newIndex}.nimi`)
    ).toContainText('Tanssi')
    await expect(
      page.getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.arvosana.value`
      )
    ).toContainText('7')

    const kuvausValue = page.getByTestId(
      `oo.0.suoritukset.0.osasuoritukset.${newIndex}.properties.paikallinenKuvaus.value`
    )
    if (!(await kuvausValue.isVisible())) {
      await page
        .getByTestId(`oo.0.suoritukset.0.osasuoritukset.${newIndex}.expand`)
        .click()
    }
    await expect(kuvausValue).toContainText('Kurssilla tanssitaan paljon')
    await expect(kuvausValue).toContainText('Myös paritanssia')
    await expect(kuvausValue).toHaveCSS('white-space', 'pre-line')
  })

  test('Tallennettu paikallinen oppiaine löytyy dropdownista uudelleen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(90000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()

    // Lisää paikallinen Tanssi päättötodistukselle ja tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const valinnaisetDropdown = page
      .locator('[data-testid="oppiaineet-valinnaiset"]')
      .locator('[data-testid$=".uusi-oppiaine.input"]')
    await valinnaisetDropdown.click()
    await page
      .locator('[data-testid$=".uusi-paikallinen-oppiaine.item"]')
      .first()
      .click()

    const newIndex = 23
    await page
      .getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.paikallinenKoodi.edit.input`
      )
      .fill('TNS')
    await page
      .getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.paikallinenNimi.edit.input`
      )
      .fill('Tanssi')
    await page
      .getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.properties.paikallinenKuvaus.edit.input`
      )
      .fill('Tanssia')
    await page
      .getByTestId(
        `oo.0.suoritukset.0.osasuoritukset.${newIndex}.arvosana.edit.input`
      )
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^7$/ })
      .first()
      .click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Lataa sivu uudelleen (varmistetaan että preferences ladataan)
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Avaa valinnaisten dropdown — Tanssin pitäisi löytyä sieltä
    const dropdown = page
      .locator('[data-testid="oppiaineet-valinnaiset"]')
      .locator('[data-testid$=".uusi-oppiaine.input"]')
    await dropdown.click()

    await expect(
      page.locator('.Select__optionLabel').filter({ hasText: /^Tanssi$/ })
    ).toBeVisible({ timeout: 10000 })

    // Tanssi löytyy myös pakollisten dropdownista
    await page.keyboard.press('Escape')
    const pakollisetDropdown = page
      .locator('[data-testid="oppiaineet-pakolliset"]')
      .locator('[data-testid$=".uusi-oppiaine.input"]')
    await pakollisetDropdown.click()
    await expect(
      page.locator('.Select__optionLabel').filter({ hasText: /^Tanssi$/ })
    ).toBeVisible()
  })
})
