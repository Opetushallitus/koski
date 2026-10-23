import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1669-1723 "Navigointi pois sivulta"
 * v2-editorille. V2 käyttää window.beforeunload-tapahtumaa (useConfirmUnload
 * EditorContainerissa) kun muokkaustilassa on tallentamattomia muutoksia.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus`

test.describe('Perusopetuksen uusi käyttöliittymä: navigointi pois sivulta', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Muokkaustila ilman muutoksia — navigointi onnistuu ilman vahvistusta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // hasChanged = false, joten ei tallentamattomia muutoksia
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.editStatus')
    ).toContainText('Ei tallentamattomia muutoksia')

    // Navigoi toiseen oppijaan — ei dialogia
    let dialogShown = false
    page.on('dialog', async (d) => {
      dialogShown = true
      await d.dismiss()
    })
    await oppijaPage.goto(
      '1.2.246.562.24.00000000001?opiskeluoikeudenTyyppi=perusopetus'
    )
    // Odota että navigointi tapahtuu (tai epäonnistuu)
    await page.waitForLoadState('domcontentloaded')
    expect(dialogShown).toBe(false)
  })

  test('Muokkaustila tallentamattomilla muutoksilla — beforeunload-tapahtuma estetään', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Siirry muokkaustilaan ja tee muutos (esim. muuta käyttäytymisen
    // arvosanaa 8. vuosiluokalla)
    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .getByTestId('oo.0.suoritukset.2.kayttaytyminen.kayttaytyminen.input')
      .click()
    await page
      .locator('.Select__optionLabel')
      .filter({ hasText: /^10$/ })
      .first()
      .click()

    // Tallentamattomat muutokset tunnistetaan: Tallenna on enabled, ei
    // "Ei tallentamattomia muutoksia" -tekstiä
    await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeEnabled()

    // Synteettinen tapahtuma tarkistaa eston ilman selaimen vahvistusdialogia.
    const prevented = await page.evaluate(() => {
      const event = new Event('beforeunload', { cancelable: true })
      const beforeDispatch = event.defaultPrevented
      window.dispatchEvent(event)
      return { beforeDispatch, afterDispatch: event.defaultPrevented }
    })
    expect(prevented).toEqual({ beforeDispatch: false, afterDispatch: true })
  })
})
