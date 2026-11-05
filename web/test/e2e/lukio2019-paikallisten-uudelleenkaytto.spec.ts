import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Lukio-v2:n yhdeksäs muokkausominaisuus - toinen, joka ei ole portti
 * olemassa olevasta vanhan käyttöliittymän testistä: organisaatiokohtainen
 * paikallisten oppiaineiden ja kurssien uudelleenkäyttö (ks.
 * LukioEditor.tsx:n useSyncPaikallisetOppiaineetJaKurssit,
 * LukionOppiaineet.tsx:n UusiLukionOppiaine, UusiLukionModuuli.tsx). Ei
 * vanhan UI:n testiä, koska vanha lukio-UI ei koskaan rakentanut tätä
 * ominaisuutta - ei kuitenkaan deliberaattia poissulkua (vrt. oma äidinkieli
 * -tunnustaminen), vain tekemättä jäänyt. Sama mekanismi kuin
 * perusopetus-v2:lla jo on (ks. perusopetus-v2-paikallinen-oppiaine.spec.ts:n
 * "Tallennettu paikallinen oppiaine löytyy dropdownista uudelleen"), vain
 * lukio-kohtainen - ks. appstate/preferences.tsx:n usePreferences,
 * PaikallinenLukionOppiaine2019/LukionPaikallinenOpintojakso2019:n
 * StorablePreference-piirre.
 *
 * Ei VARIANTIT-rinnakkaisajoa - vanhalla käyttöliittymällä ei ole mitään
 * vastaavaa ajettavaksi.
 */
test.describe('Lukiokoulutus 2019: paikallisten oppiaineiden ja kurssien uudelleenkäyttö', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .locator(
        '[data-testid^="oo.0.opiskeluoikeus.tila.edit.items."][data-testid$=".remove"]'
      )
      .click()
  })

  test('Tallennettu paikallinen oppiaine löytyy "Lisää oppiaine" -valikosta uudelleen', async ({
    page
  }) => {
    const uusiOppiaine = page.getByTestId('oo.0.suoritukset.0.uusi-oppiaine')
    await uusiOppiaine.locator('.Select__toggle').click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Lisää\.\.\.$/ })
      .click()

    const rivi = page.locator('.LukionOppiaineet__taulukko tbody tr').last()
    await rivi
      .locator('[data-testid$=".paikallinenKoodi.edit.input"]')
      .fill('PREFTEST')
    await rivi
      .locator('[data-testid$=".paikallinenNimi.edit.input"]')
      .fill('Preferenssitesti')
    await rivi
      .locator('[data-testid$=".paikallinenKuvaus.edit.input"]')
      .fill('Testikuvaus')
    await rivi.locator('.LukionOppiaineet__arvosana .Select__toggle').click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^9$/ })
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Poistetaan rivi suorituksesta - tämä ei poista itse tallennettua
    // preferenssiä organisaatiolta.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .locator('[data-koodiarvo="PREFTEST"] [data-testid$=".poisto"]')
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    await page.reload()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const uusiOppiaine2 = page.getByTestId('oo.0.suoritukset.0.uusi-oppiaine')
    await uusiOppiaine2.locator('.Select__toggle').click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Preferenssitesti$/ })
      .click()

    const rivi2 = page.locator('[data-koodiarvo="PREFTEST"]')
    await expect(
      rivi2
        .locator('.LukionOppiaineet__kuvaus')
        .locator('[data-testid$=".paikallinenKuvaus.edit.input"]')
    ).toHaveValue('Testikuvaus')
  })

  test('Tallennettu paikallinen kurssi löytyy "Lisää osasuoritus" -valikosta uudelleen', async ({
    page
  }) => {
    const ai = page.locator('[data-koodiarvo="AI"]')
    await ai.getByText('Lisää osasuoritus').click()
    const dialog = page.locator('[role="dialog"]')
    await dialog.locator('.Select__toggle').click()
    await dialog.locator('.Select__input').fill('paikallinen')
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Lisää paikallinen kurssi\.\.\.$/ })
      .click()
    await dialog
      .locator('[data-testid$=".paikallinenKoodi.input"]')
      .fill('KPREF')
    await dialog
      .locator('[data-testid$=".paikallinenNimi.input"]')
      .fill('Kurssipreferenssi')
    await dialog
      .locator('[data-testid$=".paikallinenKuvaus.input"]')
      .fill('Kurssin testikuvaus')
    await dialog.locator('.LaajuusField input').fill('2')
    await dialog.getByRole('button', { name: 'Lisää' }).click()

    const kprefUusi = ai.locator('li.kurssi').filter({
      has: page.locator('.tunniste', { hasText: /^KPREF$/ })
    })
    await kprefUusi.locator('.arvosana .Select__toggle').click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^9$/ })
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Poistetaan kurssi suorituksesta - preferenssi säilyy organisaatiolla.
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    const aiAgain = page.locator('[data-koodiarvo="AI"]')
    await aiAgain
      .locator('li.kurssi')
      .filter({ has: page.locator('.tunniste', { hasText: /^KPREF$/ }) })
      .locator('[data-testid$=".poisto"]')
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    await page.reload()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    const ai2 = page.locator('[data-koodiarvo="AI"]')
    await ai2.getByText('Lisää osasuoritus').click()
    const dialog2 = page.locator('[role="dialog"]')
    await dialog2.locator('.Select__toggle').click()
    await dialog2.locator('.Select__input').fill('KPREF')
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /KPREF Kurssipreferenssi/ })
      .click()

    const kprefAgain = ai2.locator('li.kurssi').filter({
      has: page.locator('.tunniste', { hasText: /^KPREF$/ })
    })
    await expect(kprefAgain).toBeVisible()
  })
})
