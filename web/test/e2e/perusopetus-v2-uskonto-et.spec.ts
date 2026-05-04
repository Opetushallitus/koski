import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1497-1523
 * "Uskonto/Elämänkatsomustieto -vaihtoehdossa näkyy mukana koodiarvo"
 * v2-editorille. Koodistosta löytyy sekä KT (Katolinen uskonto) että ET
 * (Elämänkatsomustieto), molemmat samalla nimellä "Uskonto/
 * Elämänkatsomustieto". V2 erottaa ne liittämällä koodiarvon nimen perään.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: uskonto/ET pudotusvalikossa', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Uskonto/ET-vaihtoehdot näkyvät koodiarvo-suffiksilla', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    // 7. vuosiluokka: ei valinnaisia/pakollisia tähän vaiheeseen — avataan
    // 8. vuosiluokka (tab 2) jossa groupattu näkymä + dropdown valinnaisille.
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Avaa valinnaisten pudotusvalikko (ryhmässä on oma uusi-oppiaine)
    await page
      .locator('[data-testid="oppiaineet-valinnaiset"]')
      .locator('[data-testid$=".uusi-oppiaine.input"]')
      .click()

    // Pudotusvalikossa löytyy erillisinä "Uskonto/Elämänkatsomustieto KT" ja
    // "Uskonto/Elämänkatsomustieto ET"
    await expect(
      page.locator('.Select__optionLabel').filter({
        hasText: /^Uskonto\/Elämänkatsomustieto KT$/
      })
    ).toBeVisible()
    await expect(
      page.locator('.Select__optionLabel').filter({
        hasText: /^Uskonto\/Elämänkatsomustieto ET$/
      })
    ).toBeVisible()

    // Pelkkä nimi ilman koodiarvoa ei esiinny (kaikki vaihtoehdot erotettu)
    await expect(
      page.locator('.Select__optionLabel').filter({
        hasText: /^Uskonto\/Elämänkatsomustieto$/
      })
    ).toHaveCount(0)
  })
})
