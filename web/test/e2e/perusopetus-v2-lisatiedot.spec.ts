import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Porttaa perusopetusSpec_2.js:481-706 "Tietojen muuttaminen >
 * Opiskeluoikeuden tiedot > Opiskeluoikeuden lisätiedot" -osion
 * lisätietojen elinkaaren sekä päiväys-/aikajaksokäyttäytymisen v2-editorille.
 *
 * Vanhan Mocha-blokin skipatut Erityisen tuen päätös- ja Tukimuodot-osiot
 * jätetään tietoisesti pois: tämä tiedosto kattaa vain lisätietojen
 * näkyvyyden, tallentamisen ja aikajaksojen käsittelyn.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const editButton = 'oo.0.opiskeluoikeus.edit'
const saveButton = 'oo.0.opiskeluoikeus.save'
const cancelEditButton = 'oo.0.opiskeluoikeus.cancelEdit'
const lisatiedotButton = 'oo.0.opiskeluoikeus.lisätiedotButton'

const lisatieto = (field: string) => `oo.0.opiskeluoikeus.lisätiedot.${field}`

const aikajaksoInput = (
  field: string,
  indexOrPart: number | 'alku' | 'loppu',
  part?: 'alku' | 'loppu'
) => {
  const suffix =
    typeof indexOrPart === 'number'
      ? `${indexOrPart}.aikajakso.${part}`
      : `aikajakso.${indexOrPart}`
  return `${lisatieto(field)}.${suffix}.input`
}

const lisatiedotRow = (page: Page, label: string) =>
  page
    .locator('.EditorContainer__lisatiedot .KeyValueRow')
    .filter({ hasText: label })

const addAikajakso = async (page: Page, label: string) => {
  await lisatiedotRow(page, label)
    .getByRole('button', { name: 'Lisää', exact: true })
    .click()
}

const fillSingleAikajakso = async (
  page: Page,
  field: string,
  alku: string,
  loppu: string
) => {
  await page.getByTestId(aikajaksoInput(field, 'alku')).fill(alku)
  await page.getByTestId(aikajaksoInput(field, 'loppu')).fill(loppu)
}

const saveChanges = async (page: Page) => {
  await page.getByTestId(saveButton).click()
  await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })
}

const expectSaveBlockedOrRejected = async (page: Page) => {
  const save = page.getByTestId(saveButton)
  if (await save.isDisabled()) {
    await expect(save).toBeDisabled()
  } else {
    await save.click()
    await expect(page.getByTestId('globalErrors')).toBeVisible({
      timeout: 15000
    })
  }

  await expect(page.getByTestId(cancelEditButton)).toBeVisible()
}

test.describe('Perusopetuksen uusi käyttöliittymä: opiskeluoikeuden lisätiedot', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Tyhjät lisätiedot piilotetaan view-tilassa ja lisätty arvo säilyy piilotuksen yli', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    await expect(page.getByTestId(lisatiedotButton)).not.toBeVisible()
    await expect(
      page.getByTestId(`${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.value`)
    ).not.toBeVisible()

    await page.getByTestId(editButton).click()
    await expect(page.getByTestId(lisatiedotButton)).toBeVisible()
    await expect(
      lisatiedotRow(page, 'Perusopetuksen aloittamista lykätty')
    ).toHaveCount(0)
    await expect(lisatiedotRow(page, 'Tukimuodot')).toHaveCount(0)
    await expect(lisatiedotRow(page, 'Tehostetun tuen päätös')).toHaveCount(0)
    await expect(
      lisatiedotRow(page, 'Oikeus maksuttomaan asuntolapaikkaan')
    ).toHaveCount(0)

    await page
      .getByTestId(`${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.edit.input`)
      .check()

    await page.getByTestId(lisatiedotButton).click()
    await expect(
      page.getByTestId(
        `${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.edit.input`
      )
    ).not.toBeVisible()

    await page.getByTestId(lisatiedotButton).click()
    await expect(
      page.getByTestId(
        `${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.edit.input`
      )
    ).toBeChecked()

    await saveChanges(page)

    const vuosiluokkiinSitoutumaton = page.getByTestId(
      `${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.value`
    )
    await expect(vuosiluokkiinSitoutumaton).toContainText('Kyllä')

    await page.getByTestId(lisatiedotButton).click()
    await expect(vuosiluokkiinSitoutumaton).not.toBeVisible()

    await page.getByTestId(lisatiedotButton).click()
    await expect(vuosiluokkiinSitoutumaton).toContainText('Kyllä')

    await oppijaPage.goto(kaisaUrl)
    await expect(
      page.getByTestId(`${lisatieto('vuosiluokkiinSitoutumatonOpetus')}.value`)
    ).toContainText('Kyllä')
  })

  test('Joustava perusopetus: validi päivämääräväli tallentuu', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId(editButton).click()

    await addAikajakso(page, 'Joustava perusopetus')
    await fillSingleAikajakso(
      page,
      'joustavaPerusopetus',
      '1.1.2010',
      '31.12.2010'
    )

    await saveChanges(page)

    await expect(
      page.getByTestId(`${lisatieto('joustavaPerusopetus')}.alku`)
    ).toContainText('1.1.2010')
    await expect(
      page.getByTestId(`${lisatieto('joustavaPerusopetus')}.loppu`)
    ).toContainText('31.12.2010')

    await oppijaPage.goto(kaisaUrl)
    await expect(
      page.getByTestId(`${lisatieto('joustavaPerusopetus')}.alku`)
    ).toContainText('1.1.2010')
    await expect(
      page.getByTestId(`${lisatieto('joustavaPerusopetus')}.loppu`)
    ).toContainText('31.12.2010')

    await page.getByTestId(editButton).click()
    await page.getByTestId(`${lisatieto('joustavaPerusopetus')}.delete`).click()
    await saveChanges(page)

    await expect(
      page.getByTestId(`${lisatieto('joustavaPerusopetus')}.alku`)
    ).not.toBeVisible()
  })

  test('Joustava perusopetus: virheellinen päivämääräväli estää tallennuksen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId(editButton).click()

    await addAikajakso(page, 'Joustava perusopetus')
    await fillSingleAikajakso(
      page,
      'joustavaPerusopetus',
      '2.1.2010',
      '1.1.2010'
    )

    await expectSaveBlockedOrRejected(page)
  })

  test('Ulkomaanjaksot: jaksoja voi lisätä ja poistaa listalta', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId(editButton).click()

    await addAikajakso(page, 'Ulkomaanjaksot')
    await page
      .getByTestId(aikajaksoInput('ulkomaanjaksot', 0, 'alku'))
      .fill('1.1.2010')
    await page
      .getByTestId(aikajaksoInput('ulkomaanjaksot', 0, 'loppu'))
      .fill('31.1.2010')

    await addAikajakso(page, 'Ulkomaanjaksot')
    await page
      .getByTestId(aikajaksoInput('ulkomaanjaksot', 1, 'alku'))
      .fill('1.2.2010')
    await page
      .getByTestId(aikajaksoInput('ulkomaanjaksot', 1, 'loppu'))
      .fill('28.2.2010')

    await expect(
      page.getByTestId(aikajaksoInput('ulkomaanjaksot', 0, 'alku'))
    ).toHaveValue('1.1.2010')
    await expect(
      page.getByTestId(aikajaksoInput('ulkomaanjaksot', 1, 'alku'))
    ).toHaveValue('1.2.2010')

    await saveChanges(page)

    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.alku`)
    ).toContainText('1.1.2010')
    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.1.alku`)
    ).toContainText('1.2.2010')

    await page.getByTestId(editButton).click()
    await page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.delete`).click()
    // DateInput on listakohdissa uncontrolled, joten indeksien siirtymistä ei
    // tarkisteta muokkaustilan syötekentästä vaan tallennetusta näkymästä.
    await saveChanges(page)

    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.alku`)
    ).toContainText('1.2.2010')
    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.loppu`)
    ).toContainText('28.2.2010')
    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.1.alku`)
    ).not.toBeVisible()

    await page.getByTestId(editButton).click()
    await page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.delete`).click()
    await saveChanges(page)

    await expect(
      page.getByTestId(`${lisatieto('ulkomaanjaksot')}.0.alku`)
    ).not.toBeVisible()
  })
})
