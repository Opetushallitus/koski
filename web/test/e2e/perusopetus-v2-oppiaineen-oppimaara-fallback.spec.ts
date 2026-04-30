import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Varmistetaan, että perusopetus-v2-editoria käytetään vain silloin, kun
 * kaikki päätason suoritukset ovat joko PerusopetuksenVuosiluokanSuoritus
 * tai NuortenPerusopetuksenOppimääränSuoritus. Muuten (esim. aineopiskelijan
 * NuortenPerusopetuksenOppiaineenOppimääränSuoritus) näytetään v1-editori.
 *
 * NuortenPerusopetus-ErityinenTutkinto (hetu 060675-2471) käyttää
 * ExamplesPerusopetus.useampiNuortenPerusopetuksenOppiaineenOppimääränSuoritus-
 * SamassaOppiaineessaEriLuokkaAsteella-fixtureä, jossa on useita
 * NuortenPerusopetuksenOppiaineenOppimääränSuoritus-suorituksia.
 */

const oid = '1.2.246.562.24.00000000133'
const url = `${oid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen v1-fallback oppiaineen oppimäärän suoritukselle', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Opiskeluoikeudessa oppiaineen oppimäärä → v1-editori, ei v2-testId:itä', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(url)

    // V2-editorin juuritestiId:tä (oo.0.opiskeluoikeus.*) ei saa löytyä
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toHaveCount(0)

    // V1-editorin tyypilliset DOM-rakenteet (.opiskeluoikeus, .suoritus) löytyvät
    await expect(page.locator('.opiskeluoikeus').first()).toBeVisible()
  })
})
