import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1724-1785 "Virhetilanteet" v2-editorille.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

test.describe('Perusopetuksen uusi käyttöliittymä: tallennusvirheet', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Kun tallennus epäonnistuu: muokkaustila säilyy ja Tallenna aktivoituu uudelleen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Tee muutos: todistuksella näkyvät lisätiedot -kenttä oppimäärätabilla
    await page
      .getByTestId(
        'oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.edit.input'
      )
      .fill('Virhetesti')

    // Reititä PUT /api/oppija palauttamaan 500 ennen tallennusta
    await page.route('**/koski/api/oppija**', async (route) => {
      if (route.request().method() === 'PUT') {
        await route.fulfill({
          status: 500,
          contentType: 'application/json',
          body: JSON.stringify([
            {
              key: 'internal.error',
              message: 'Simuloitu palvelinvirhe testissä'
            }
          ])
        })
      } else {
        await route.continue()
      }
    })

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()

    // Editori pysyy muokkaustilassa (ei palaa view-modeen) ja Tallenna on
    // jälleen kutsuttavissa uudestaan
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible({
      timeout: 10000
    })
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeEnabled()

    // Poistetaan reititys ja tallennetaan uudestaan
    await page.unroute('**/koski/api/oppija**')
    await page.getByTestId('oo.0.opiskeluoikeus.save').click()

    // Onnistuneen tallennuksen jälkeen palataan view-modeen
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })

    // Muutettu kenttä näkyy
    await expect(
      page
        .locator('.KeyValueRow')
        .filter({ hasText: 'Todistuksella näkyvät lisätiedot' })
    ).toContainText('Virhetesti')
  })

  test('Kun tallennuksen jälkeinen oppijan uudelleenlataus epäonnistuu: näytetään v2-latausvirhe', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await page
      .getByTestId(
        'oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.edit.input'
      )
      .fill('Reload-virhetesti')

    let tallennusOnnistui = false
    let estäSeuraavaV2Reload = false
    let v2ReloadEpäonnistettu = false

    await page.route(
      (url) =>
        url.pathname === '/koski/api/oppija' &&
        url.searchParams.get('class_refs') === 'false',
      async (route) => {
        if (route.request().method() === 'PUT') {
          const response = await route.fetch()
          tallennusOnnistui = response.ok()
          await route.fulfill({ response })
        } else {
          await route.continue()
        }
      }
    )

    await page.route(
      (url) => url.pathname === `/koski/api/oppija/${kaisaOid}/uiv2`,
      async (route) => {
        if (
          route.request().method() === 'GET' &&
          tallennusOnnistui &&
          estäSeuraavaV2Reload
        ) {
          v2ReloadEpäonnistettu = true
          await route.fulfill({
            status: 500,
            contentType: 'application/json',
            body: JSON.stringify([
              {
                key: 'internal.error',
                message: 'Simuloitu latausvirhe testissä'
              }
            ])
          })
        } else {
          await route.continue()
        }
      }
    )

    await page.getByTestId('oo.0.opiskeluoikeus.save').click()

    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })
    expect(tallennusOnnistui).toBe(true)

    // V2 ei tee legacy-editorin automaattista /api/editor-uudelleenlatausta
    // tallennuksen jälkeen. Käyttäjälle näkyvä v2-latausvirhe syntyy seuraavalla
    // oppijan uiv2-datan latauksella.
    estäSeuraavaV2Reload = true
    await page.reload()

    await expect(
      page.locator('.opiskeluoikeuksientiedot .error')
    ).toContainText(
      'Näkymää ei saada ladattua. Yritä hetken päästä uudelleen.',
      { timeout: 10000 }
    )
    expect(v2ReloadEpäonnistettu).toBe(true)
  })
})
