import { expect, test } from './base'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija } from './setup/auth'

/**
 * Nuorten perusopetuksen opiskeluoikeuden luonti ja luodun opiskeluoikeuden
 * näyttäminen uudessa käyttöliittymässä.
 *
 * Tyhjä, Tero (230872-7258) on fixtureissa oppija, jolla ei ole
 * opiskeluoikeuksia.
 */

const hetu = '230872-7258'
const aloituspäivä = new Date(2018, 0, 1)

const esitäytetytOppiaineet = [
  'Äidinkieli ja kirjallisuus,',
  'A1-kieli,',
  'B1-kieli,',
  'Matematiikka',
  'Biologia',
  'Maantieto',
  'Fysiikka',
  'Kemia',
  'Terveystieto',
  'Uskonto/Elämänkatsomustieto',
  'Historia',
  'Yhteiskuntaoppi',
  'Musiikki',
  'Kuvataide',
  'Käsityö',
  'Liikunta',
  'Kotitalous',
  'Opinto-ohjaus'
]

// Sama dialogi avautuu sekä uuden oppijan lisäyksessä että olemassa olevan
// oppijan "Lisää opiskeluoikeus" -linkistä.
const täytäPerusopetus = async (uusiOppijaPage: KoskiUusiOppijaPage) => {
  await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
  // Avaimella, koska tekstinä "Perusopetus" osuisi myös aikuisten ja taiteen
  // perusopetukseen.
  await uusiOppijaPage.controls.opiskeluoikeus.set(
    'opiskeluoikeudentyyppi_perusopetus'
  )
  await uusiOppijaPage.fill({
    suorituskieli: 'ruotsi',
    aloituspäivä,
    opiskeluoikeudenTila: 'Läsnä'
  })
}

const luoOpiskeluoikeus = async (uusiOppijaPage: KoskiUusiOppijaPage) => {
  await uusiOppijaPage.goTo(hetu)
  await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
  await täytäPerusopetus(uusiOppijaPage)
  await uusiOppijaPage.submitAndExpectSuccess()
}

test.describe('Perusopetuksen uusi käyttöliittymä: opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Luotu opiskeluoikeus avautuu uudessa käyttöliittymässä esitäytetyin oppiainein', async ({
    page,
    uusiOppijaPage
  }) => {
    await luoOpiskeluoikeus(uusiOppijaPage)

    await expect(
      page.getByTestId(/^oo\.0\.suoritusTabs\.\d+\.tab$/)
    ).toHaveText(['Päättötodistus'])
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toHaveText(
      'Perusopetus'
    )
    await expect(
      page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')
    ).toHaveText('ruotsi')
    await expect(page.locator('.opiskeluoikeuksientiedot')).toContainText(
      '104/011/2014'
    )
    await expect(
      page.getByTestId(/^oo\.0\.suoritukset\.0\.osasuoritukset\.\d+\.nimi$/)
    ).toHaveText(esitäytetytOppiaineet)
  })

  test('Vastaavaa opiskeluoikeutta ei voi lisätä oppijalle toista kertaa', async ({
    page,
    uusiOppijaPage
  }) => {
    await luoOpiskeluoikeus(uusiOppijaPage)

    await page
      .getByTestId('opiskeluoikeustyypit-navigation')
      .getByText('Lisää opiskeluoikeus')
      .click()
    await täytäPerusopetus(uusiOppijaPage)
    await uusiOppijaPage.controls.submit.button.click()

    await expect(page.getByTestId('error')).toHaveText(
      'Vastaava opiskeluoikeus on jo olemassa.'
    )
  })

  test('Selaimen paluu lisäyksen jälkeen ei salli samaa opiskeluoikeutta uudelleen', async ({
    page,
    uusiOppijaPage
  }) => {
    await luoOpiskeluoikeus(uusiOppijaPage)

    await page.goBack()
    await expect(page).toHaveURL(/\/koski\/uusioppija#hetu=/)
    // Oppija on nyt olemassa, joten nimikentät ovat valmiiksi täytetyt ja lukitut.
    await expect(
      uusiOppijaPage.$.uusiOpiskeluoikeus.oppija.etunimet.elem
    ).toBeDisabled()
    await täytäPerusopetus(uusiOppijaPage)
    await uusiOppijaPage.controls.submit.button.click()

    await expect(page.getByTestId('error')).toHaveText(
      'Vastaava opiskeluoikeus on jo olemassa.'
    )
  })
})
