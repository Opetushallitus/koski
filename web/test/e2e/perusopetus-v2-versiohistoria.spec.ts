import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1775-1862 "Opiskeluoikeuden versiot"
 * v2-editorille. V2:n versiohistoria-nappi aukaisee listan, jossa näkyy
 * tallennetut versionumerot. Versiolinkin kautta voi selata aiempia
 * versioita.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: versiohistoria', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Versiohistoria-nappi aukaisee listan versioista', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Versiohistoria-nappi näkyy opiskeluoikeuden otsikkopalkissa
    const button = page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button')
    await expect(button).toBeVisible()
    await expect(button).toContainText('Versiohistoria')

    // Klikkaa avataksesi lista
    await button.click()
    // Ainakin yksi versio (v1) löytyy listalta
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    ).toBeVisible()
  })

  test('Tallennus lisää uuden version versiohistoriaan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Aluksi lista sisältää v1:n (fixturin pohjaversio)
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    const v1Link = page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    await expect(v1Link).toBeVisible()

    // Sulje lista klikkaamalla versiohistoria-nappia uudelleen
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    // Tee muutos + tallennus
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

    // Muuta käyttäytymisen arvosanaa
    await page
      .getByTestId('oo.0.suoritukset.2.kayttaytyminen.kayttaytyminen.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^erinomainen$/ })
      .first()
      .click()
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Avaa versiohistoria: nyt pitäisi näkyä sekä v1 että v2
    await page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.button').click()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.1')
    ).toBeVisible()
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.versiohistoria.list.2')
    ).toBeVisible()
  })
})
