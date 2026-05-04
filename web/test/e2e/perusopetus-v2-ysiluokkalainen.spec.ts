import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Testit 9. vuosiluokan oppilaan perusopetus-v2-editorille.
 * Portattu tiedostosta `web/test/spec/perusopetusSpec_3.js` lines 621-767.
 *
 * Ysiluokkalainen Ylermi (160932-311V) on 9. luokan oppilas, jolla on
 * päättötodistus kesken ja vuosiluokkien 8–9 suoritukset. Editorin tabit
 * sortataan: 0 = Päättötodistus (oppimäärä), 1 = 9. vuosiluokka, 2 = 8. vuosiluokka.
 *
 * V1 vs. v2 erot:
 *  - V1 valitsi oletuksena viimeisimmän keskeneräisen suorituksen tabiksi;
 *    v2 aloittaa aina ensimmäisestä (Päättötodistus).
 *  - V1 esitäytti oppiaineet automaattisesti kun `jääLuokalle` = true;
 *    v2 näyttää taulukon mutta käyttäjä lisää oppiaineet manuaalisesti.
 *  - V1:n "merkitse valmiiksi" -dialogissa on "Siirretään seuraavalle luokalle"
 *    -kytkin, jota v2:ssa ei ole; v2:ssa `jääLuokalle`-kenttä on itse
 *    suorituksen editorissa.
 */

const ysiluokkalainenOid = '1.2.246.562.24.00000000010'
const ysiluokkalainenUrl = `${ysiluokkalainenOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: 9. vuosiluokan oppilas', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Aluksi: Päättötodistus on oletustabi, 9. vuosiluokkatabilla oppiaineet piilossa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    // Oletustabi v2:ssa on tab 0 (Päättötodistus)
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toContainText(
      'Päättötodistus'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
      '9. vuosiluokka'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.2.tab')).toContainText(
      '8. vuosiluokka'
    )

    // Siirry 9. vuosiluokan tabille
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await expect(page.getByTestId('oo.0.suoritukset.1.koulutus')).toContainText(
      '9. vuosiluokka'
    )

    // jääLuokalle = false → oppiaineet eivät näy
    await expect(page.locator('.oppiaineet')).not.toBeVisible()
  })

  test('Muokkaustilassa 9. vuosiluokalla: vahvistus-nappeja ei näy ilman tallentamattomia muutoksia', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Tallenna-nappi on disabloitu (ei muutoksia vielä)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeDisabled()

    // Peruuta toimii
    await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible()
  })

  test('Kun oppilas jää luokalle: oppiaineet-taulukko tulee näkyviin', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Oppiaineet piilossa ennen jääLuokalle-kytkintä
    await expect(page.locator('.oppiaineet')).not.toBeVisible()

    // Aseta jääLuokalle = true
    await page
      .getByTestId('oo.0.suoritukset.1.jääLuokalle.edit.input')
      .check()

    // Oppiaineet-taulukko tulee näkyviin
    await expect(page.locator('.oppiaineet')).toBeVisible()
  })

  test('jääLuokalle=true + pakollisen oppiaineen lisäys + arvosana + tallennus', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Aseta jääLuokalle = true, jotta oppiaineet tulevat näkyviin
    await page
      .getByTestId('oo.0.suoritukset.1.jääLuokalle.edit.input')
      .check()

    // Lisää Matematiikka pakolliseksi oppiaineeksi
    const uusiOppiaineInput = page
      .locator('[data-testid$=".uusi-oppiaine.input"]')
      .first()
    await uusiOppiaineInput.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Matematiikka$/ })
      .first()
      .click()

    // Matematiikka ilmestyy pakollisiin oppiaineisiin
    const oppiaineNimet = page.locator(
      '[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".nimi"]'
    )
    await expect(oppiaineNimet).toHaveCount(1)
    await expect(oppiaineNimet.first()).toContainText('Matematiikka')

    // Anna arvosana 5
    const arvosanaSelect = page
      .locator('[data-testid*="oo.0.suoritukset.1.osasuoritukset"]')
      .locator('[data-testid$=".arvosana.edit.input"]')
      .first()
    await arvosanaSelect.click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^5/ })
      .first()
      .click()

    // Tallenna
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Ollaan edelleen 9. vuosiluokan tabilla, arvosana näkyy
    await expect(
      page.locator('[data-testid^="oo.0.suoritukset.1.osasuoritukset."][data-testid$=".arvosana.value"]').first()
    ).toContainText('5')
  })

  test('9. vuosiluokan merkitseminen valmiiksi vahvistus-dialogin kautta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(90000)
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    // 1. Aseta jääLuokalle ja lisää yksi pakollinen oppiaine arvosanalla
    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId('oo.0.suoritukset.1.jääLuokalle.edit.input')
      .check()

    await page
      .locator('[data-testid$=".uusi-oppiaine.input"]')
      .first()
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^Matematiikka$/ })
      .first()
      .click()

    await page
      .locator('[data-testid*="oo.0.suoritukset.1.osasuoritukset"]')
      .locator('[data-testid$=".arvosana.edit.input"]')
      .first()
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^8/ })
      .first()
      .click()

    // 2. Avaa merkitse valmiiksi -dialogi
    const merkitseValmiiksiBtn = page.getByTestId(
      'oo.0.suoritukset.1.suorituksenVahvistus.edit.merkitseValmiiksi'
    )
    await expect(merkitseValmiiksiBtn).toBeVisible()
    await merkitseValmiiksiBtn.click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // 3. Täytä vahvistus: lisää myöntäjähenkilö (päivä + paikkakunta esitäytetty)
    await modal
      .locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]')
      .click()
    await modal
      .locator('.Select__optionLabel')
      .filter({ hasText: 'Lisää henkilö' })
      .click()
    await modal
      .locator('[data-testid$="newHenkilö.nimi.input"]')
      .fill('Reijo Reksi')
    await modal
      .locator('[data-testid$="newHenkilö.titteli.input"]')
      .fill('rehtori')

    // 4. Submit dialogi
    await page
      .getByTestId(
        'oo.0.suoritukset.1.suorituksenVahvistus.edit.modal.submit'
      )
      .click()
    await expect(modal).not.toBeVisible()

    // 5. Tallenna opiskeluoikeus
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // 6. Vahvistustiedot näkyvät
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.1.suorituksenVahvistus.value.status'
      )
    ).toContainText('Suoritus valmis')
    await expect(
      page.getByTestId(
        'oo.0.suoritukset.1.suorituksenVahvistus.value.henkilö.0'
      )
    ).toContainText('Reijo Reksi')
  })
})
