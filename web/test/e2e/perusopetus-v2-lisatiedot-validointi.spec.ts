import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:579-607 "Opiskeluoikeuden lisätiedot >
 * Päivämäärän syöttö" v2-editorille. Varmistaa että virheellinen
 * aikajakso-alkupäivämäärä estää tallennuksen.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: lisätiedot päivämäärävalidointi', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Joustava perusopetus: virheellinen alkupäivä estää tallennuksen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await page
      .locator('.EditorContainer__lisatiedot .KeyValueRow')
      .filter({ hasText: 'Joustava perusopetus' })
      .getByRole('button', { name: 'Lisää', exact: true })
      .click()

    // Anna virheellinen alkupäivämäärä joustavaPerusopetus-riville.
    const alkuInput = page.getByTestId(
      'oo.0.opiskeluoikeus.lisätiedot.joustavaPerusopetus.aikajakso.alku.input'
    )
    await expect(alkuInput).toBeVisible()
    await alkuInput.fill('34.9.2000')
    await alkuInput.blur()

    // Tallennus estyy joko frontin validaatiossa tai backendin hylkäämänä.
    const saveButton = page.getByTestId('oo.0.opiskeluoikeus.save')
    if (await saveButton.isDisabled()) {
      await expect(saveButton).toBeDisabled()
    } else {
      await saveButton.click()
      await expect(page.getByTestId('globalErrors')).toBeVisible({
        timeout: 15000
      })
    }

    // Korjaa päivämäärä
    await alkuInput.fill('1.1.2020')
    await alkuInput.blur()

    // Tallenna on jälleen enabloitu
    await expect(saveButton).toBeEnabled()
  })
})
