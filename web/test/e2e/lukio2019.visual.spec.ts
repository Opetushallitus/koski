import { expect, test } from './base'
import { virkailija } from './setup/auth'
import { takeFullPageScreenshot } from './fragments/fullPageScreenshot'

/**
 * Visuaaliset regressiotestit lukio 2019 -koulutusmuodon uudelle (v2)
 * käyttöliittymälle.
 *
 * Toiminnalliset testit: lukio2019-*.spec.ts, omattiedot-lukio2019.spec.ts
 * Ks. documentation/visual-testing.md
 *
 * Kuvat otetaan:
 * - Ullan "Lukion oppimäärä" -suorituksesta (katselu + muokkaus), jolla on
 *   laaja kirjo sisältöä samassa näkymässä: valtakunnallisia ja paikallisia
 *   oppiaineita, kieliaineen kieli, oma äidinkielen opinnot -lohko,
 *   puhvikoe/suullisen kielitaidon kokeet/lukiodiplomit-rivit sekä
 *   lukiodiplomit/muut suoritukset/teemaopinnot
 *   (MuidenLukioOpintojenSuoritus2019) -rivit kursseineen.
 * - Urhon "Lukion aineopinnot" -suorituksesta, jolla on sekä oppiaineen
 *   tasolla että päätason suorituksella merkitty "suoritettu erityisenä
 *   tutkintona" - kattaa **-alaviitteen ja sen laajuuslaskennan.
 */

const ulla = '1.2.246.562.24.00000000015'
const urho = '1.2.246.562.24.00000000016'

test.describe('Lukio 2019 v2 – visuaaliset regressiot', () => {
  test.skip(
    process.platform !== 'linux',
    'Visuaalitestit ajetaan vain Linuxilla. Paikallinen ajo: make visual-test'
  )

  test.use({ storageState: virkailija('pää') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lukion oppimäärä, katselunäkymä', async ({ page, oppijaPageV2 }) => {
    await oppijaPageV2.gotoWithQueryParams(ulla, { 'lukio-v2': 'true' })
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi')
    ).toBeVisible()
    await takeFullPageScreenshot(page, 'lukio-oppimaara-katselu.png')
  })

  test('Lukion oppimäärä, muokkaustila', async ({ page, oppijaPageV2 }) => {
    await oppijaPageV2.gotoWithQueryParams(ulla, { 'lukio-v2': 'true' })
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
    await takeFullPageScreenshot(page, 'lukio-oppimaara-muokkaus.png')
  })

  test('Lukion aineopinnot, erityinen tutkinto, katselunäkymä', async ({
    page,
    oppijaPageV2
  }) => {
    await oppijaPageV2.gotoWithQueryParams(urho, { 'lukio-v2': 'true' })
    await expect(
      page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.nimi')
    ).toBeVisible()
    await takeFullPageScreenshot(
      page,
      'lukio-aineopinnot-erityinen-tutkinto-katselu.png'
    )
  })
})
