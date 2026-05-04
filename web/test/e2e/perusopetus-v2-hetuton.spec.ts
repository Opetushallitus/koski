import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_1.js "Hetuton oppija" -testi v2-editorille.
 * Hetuttoman oppijan (OID 99999999123) opiskeluoikeus on
 * ysinOpiskeluoikeusKesken — sama kuin ysiluokkalaisella. Testaa että
 * v2-editori renderöi opiskeluoikeuden tilan ilman hetua.
 */

const hetutonOid = '1.2.246.562.24.99999999123'
const url = `${hetutonOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: hetuton oppija', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Näyttää opiskeluoikeuden tilan ja voimassaoloajan', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(url)

    // Voimassaoloaika alkaa 15.8.2008 ilman päättymispäivää
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
    ).toContainText('15.8.2008')

    // Tila on Läsnä
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Läsnä')

    // Suoritukset-tabit: Päättötodistus + 9/8 vuosiluokat
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toContainText(
      'Päättötodistus'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
      '9. vuosiluokka'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.2.tab')).toContainText(
      '8. vuosiluokka'
    )
  })
})
