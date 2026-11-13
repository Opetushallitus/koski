import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Lukio-v2:n kahdeksas muokkausominaisuus - mutta ensimmäinen, joka ei ole
 * portti olemassa olevasta vanhan käyttöliittymän testistä: osaamisen
 * tunnustaminen kurssi-/opintojaksotasolla. Vahvistettu suoraan koodista
 * (grep app/kurssi/*.jsx), että vanha käyttöliittymä ei koskaan ole
 * tukenut `tunnustettu`-kentän muokkausta lukiolle, vaikka kenttä on ollut
 * skeemassa koko ajan (Lukio2019.scala: LukionModuulinSuoritusOppiaineissa2019
 * ym.) - tämä on siis aidosti uusi, ei portattu ominaisuus. Matala riski
 * silti: käyttöliittymä on olemassa oleva, jo ammatillinen-v2:n ja IB:n
 * käyttämä geneerinen komponentti (OsaamisenTunnustusEdit/-View,
 * components-v2/opiskeluoikeus/TunnustusField.tsx), ei uutta koodia.
 *
 * Ei VARIANTIT-rinnakkaisajoa - vanhalla käyttöliittymällä ei ole mitään
 * vastaavaa ajettavaksi.
 */
test.describe('Lukiokoulutus 2019: moduulin osaamisen tunnustaminen', () => {
  test.use({ storageState: virkailija('pää') })

  // Uusilukio, Ulla (250605A518Y)
  const oid = '1.2.246.562.24.00000000015'

  test.beforeEach(async ({ fixtures, page }) => {
    await fixtures.reset()
    const params = new URLSearchParams({ 'lukio-v2': 'true' })
    await page.goto(`/koski/oppija/${oid}?${params.toString()}`)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    // Testioppijan ainoa suoritus on jo "valmis" - osaamisen tunnustamisen
    // lisäys ei itsessään leimaa mitään päivämäärää, mutta poistetaan
    // viimeisin tila silti samaa varovaisuusperiaatetta noudattaen kuin
    // muissakin tämän tiedoston kaltaisissa speceissä.
    await page
      .locator(
        '[data-testid^="oo.0.opiskeluoikeus.tila.edit.items."][data-testid$=".remove"]'
      )
      .click()
  })

  test('Osaamisen tunnustaminen voidaan lisätä kurssille', async ({
    page
  }) => {
    const ai = page.locator('[data-koodiarvo="AI"]')
    const ai1 = ai.locator('li.kurssi').filter({
      has: page.locator('.tunniste', { hasText: /^ÄI1$/ })
    })

    // Tunnustettu on muokattavissa vain kurssin tietojen muokkausmodaalin
    // kautta (klikkaus kurssin tunnisteeseen) - ei enää rivin omana kenttänä.
    await ai1.locator('.tunniste').click()
    const modaali = page.locator('[role="dialog"]')
    await modaali.locator('[data-testid$=".tunnustettu.edit.add"]').click()
    await modaali
      .locator('[data-testid$=".tunnustettu.edit.selite.input"]')
      .fill('Tunnustettu aiemmasta opinnosta')
    await modaali
      .locator('[data-testid$=".tunnustettu.edit.bool.label"]')
      .click()
    await modaali.getByRole('button', { name: 'Sulje' }).click()

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    const ai1Again = page
      .locator('[data-koodiarvo="AI"]')
      .locator('li.kurssi')
      .filter({ has: page.locator('.tunniste', { hasText: /^ÄI1$/ }) })
    await expect(
      ai1Again.locator('[data-testid$=".tunnustettu.selite"]')
    ).toHaveText('Tunnustettu aiemmasta opinnosta')
    await expect(
      ai1Again.locator('[data-testid$=".tunnustettu.rahoituksenPiirissä"]')
    ).toHaveText('Kyllä')
  })
})
