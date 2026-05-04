import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1862-1915 "Useita opiskeluoikeuksia"
 * v2-editorille. Miia Monikoululainen (180497-112F) kuuluu kahteen
 * perusopetuksen opiskeluoikeuteen (Jyväskylän + Kulosaaren). V2 renderöi
 * molemmat peräkkäin (oo.0 ja oo.1), ja kummankin suoritustabit ovat
 * riippumattomia toisistaan.
 */

const miiaOid = '1.2.246.562.24.00000000012'
const miiaUrl = `${miiaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: useita opiskeluoikeuksia', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Molempien opiskeluoikeuksien suoritustabit näkyvät riippumattomasti', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(miiaUrl)

    // Molemmat opiskeluoikeudet renderöityvät omine TestIdRootteineen, joten
    // molemmilla on sama `oo.0.*`-alkuiset testId-polut. Erotetaan ne
    // oppilaitoksen nimellä.
    const jyvaskyla = page
      .getByRole('listitem')
      .filter({ hasText: 'Jyväskylän normaalikoulu' })
    const kulosaari = page
      .getByRole('listitem')
      .filter({ hasText: 'Kulosaaren ala-aste' })

    await expect(
      jyvaskyla.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible()
    await expect(
      kulosaari.getByTestId('oo.0.opiskeluoikeus.edit')
    ).toBeVisible()

    // Molemmilla on omat suoritustabit
    await expect(jyvaskyla.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()
    await expect(kulosaari.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()

    // Klikkaa Jyväskylän toista tabia — Kulosaaren ei muutu
    const jyvaskylaTab1 = jyvaskyla.getByTestId('oo.0.suoritusTabs.1.tab')
    await expect(jyvaskylaTab1).toBeVisible()
    await jyvaskylaTab1.click()

    // Kulosaari pysyy omallaan (tab 0 yhä näkyvissä)
    await expect(kulosaari.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()
  })
})
