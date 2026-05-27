import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Porttaa perusopetusSpec_2.js "Tietojen muuttaminen > Opiskeluoikeuden tila"
 * -osion validointi- ja päättymispäivätestejä v2-editorille.
 */

const ysiluokkalainenOid = '1.2.246.562.24.00000000010'
const ysiluokkalainenUrl = `${ysiluokkalainenOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const editButton = 'oo.0.opiskeluoikeus.edit'
const saveButton = 'oo.0.opiskeluoikeus.save'
const cancelEditButton = 'oo.0.opiskeluoikeus.cancelEdit'
const tilaAddButton = 'oo.0.opiskeluoikeus.tila.edit.add'
const tilaModalDateInput = 'oo.0.opiskeluoikeus.tila.edit.modal.date.edit.input'
const tilaModalSubmit = 'oo.0.opiskeluoikeus.tila.edit.modal.submit'

const tilaOption = (tila: string) =>
  `oo.0.opiskeluoikeus.tila.edit.modal.tila.edit.options.${tila}`

const openTilaModal = async (page: Page) => {
  await page.getByTestId(tilaAddButton).click()
  const modal = page.locator('.Modal')
  await expect(modal).toBeVisible()
  return modal
}

const addTila = async (page: Page, tila: string, date?: string) => {
  const modal = await openTilaModal(page)
  if (date) {
    await page.getByTestId(tilaModalDateInput).fill(date)
  }
  await page.getByTestId(tilaOption(tila)).click()
  await page.getByTestId(tilaModalSubmit).click()
  await expect(modal).not.toBeVisible()
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

test.describe('Perusopetuksen uusi käyttöliittymä: tilan validointi', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Valmistunut-tila disabloitu kun suoritus on kesken', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    // Avaa tilanlisäysdialogi
    await page.getByTestId(editButton).click()
    await page.getByTestId(tilaAddButton).click()

    const modal = page.locator('.Modal')
    await expect(modal).toBeVisible()

    // Valmistunut-radio on disabloitu
    const valmistunutRadio = page.getByTestId(
      'oo.0.opiskeluoikeus.tila.edit.modal.tila.edit.options.valmistunut'
    )
    await expect(valmistunutRadio).toBeDisabled()

    // Muut tilat (esim. eronnut) eivät ole disabloituja
    const eronnutRadio = page.getByTestId(
      'oo.0.opiskeluoikeus.tila.edit.modal.tila.edit.options.eronnut'
    )
    await expect(eronnutRadio).toBeEnabled()
  })

  test('Virheellinen tilan päivämäärä estää tallennuksen', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)
    await page.getByTestId(editButton).click()

    // V2:n vakaa virheellisen päivämäärän polku on tilarivin oma
    // päivämääräeditori: syöte jää kenttään, mutta tallennus estyy.
    await page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.0.date.edit.input')
      .fill('11.1.200')

    await expectSaveBlockedOrRejected(page)
  })

  test('Viimeisintä tilaa aikaisempi uusi tila hylätään', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.date')
    ).toContainText('15.8.2008')
    await page.getByTestId(editButton).click()

    await addTila(page, 'eronnut', '14.8.2008')

    await expectSaveBlockedOrRejected(page)
  })

  test('Viimeisimmän tilan kanssa samalle päivälle lisätty uusi tila hylätään', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.date')
    ).toContainText('15.8.2008')
    await page.getByTestId(editButton).click()

    // Perusopetus-v2 ei tue useita saman päivän tiloja käyttöliittymässä;
    // sama päivämäärä varmistetaan siksi hylkäyksenä.
    await addTila(page, 'eronnut', '15.8.2008')

    await expectSaveBlockedOrRejected(page)
  })

  test('Eronnut-tilan lisäys näkyy opiskeluoikeuden voimassaoloaikassa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Alkutila: valmistunut 4.6.2016 → voimassaoloaika päättyy siihen
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
    ).toContainText('4.6.2016')

    // Tyhjennä valmistunut-tila ja vahvistus (että voidaan lisätä uusi tila)
    await page.getByTestId(editButton).click()
    await page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
      .click()
    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
      )
      .click()
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({
      timeout: 15000
    })

    // Voimassaoloaika ei enää päätty 4.6.2016:n, päättymispäivä puuttuu
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
    ).not.toContainText('4.6.2016')

    // Lisää eronnut-tila
    await page.getByTestId(editButton).click()
    await addTila(page, 'eronnut')
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({
      timeout: 15000
    })

    // Voimassaoloaika näyttää päättymispäivän (tänään-päivä)
    const voimassaoloaika = await page
      .getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
      .textContent()
    // Formaatti: "Opiskeluoikeuden voimassaoloaika: 15.8.2008 — d.m.yyyy"
    expect(voimassaoloaika).toMatch(/15\.8\.2008 — \d+\.\d+\.\d{4}/)
  })

  test('Päättyneellä opiskeluoikeudella (valmistunut) tilaa ei voi lisätä', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId(editButton).click()

    // Kaisalla on valmistunut-tila → tilan lisäys-painike ei ole näkyvissä
    await expect(page.getByTestId(tilaAddButton)).not.toBeVisible()
  })

  test('Peruutettu-tilan voi lisätä ja poistaa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    await page.getByTestId(editButton).click()
    await addTila(page, 'peruutettu')
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Peruutettu')
    const peruutettuVoimassaoloaika = await page
      .getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
      .textContent()
    expect(peruutettuVoimassaoloaika).toMatch(/15\.8\.2008 — \d+\.\d+\.\d{4}/)

    await page.getByTestId(editButton).click()
    await expect(page.getByTestId(tilaAddButton)).not.toBeVisible()
    await page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
      .click()
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Läsnä')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).not.toContainText('Peruutettu')
  })

  test('Väliaikaisesti keskeytynyt -tila ei aseta päättymispäivää', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Tyhjennä valmistunut-tila ja vahvistus
    await page.getByTestId(editButton).click()
    await page
      .getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove')
      .click()
    await page
      .getByTestId(
        'oo.0.suoritukset.0.suorituksenVahvistus.edit.merkitseKeskeneräiseksi'
      )
      .click()
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({
      timeout: 15000
    })

    // Lisää väliaikaisesti keskeytynyt -tila
    await page.getByTestId(editButton).click()
    await addTila(page, 'valiaikaisestikeskeytynyt')
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({
      timeout: 15000
    })

    // Voimassaoloaika on edelleen "15.8.2008 —" ilman päättymispäivää
    const voimassaoloaika = await page
      .getByTestId('oo.0.opiskeluoikeus.voimassaoloaika')
      .textContent()
    // Formaatti: "Opiskeluoikeuden voimassaoloaika: 15.8.2008 —"
    // (Tyhjä päättymispäivä: viiva ilman seuraavaa päivämäärää)
    expect(voimassaoloaika).toMatch(/15\.8\.2008 —\s*$/)
  })

  test('Tulevaisuuden väliaikaisesti keskeytynyt ei muuta nykyistä aktiivista tilaa', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(ysiluokkalainenUrl)

    await page.getByTestId(editButton).click()
    await addTila(page, 'valiaikaisestikeskeytynyt', '9.5.2117')
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible({ timeout: 15000 })

    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')
    ).toContainText('Väliaikaisesti keskeytynyt')
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.tila')
    ).toContainText('Läsnä')

    const currentTilaRow = page.locator('.OpiskeluoikeudenTila-viimeisin')
    await expect(currentTilaRow).toContainText('Läsnä')
    await expect(currentTilaRow).not.toContainText('Väliaikaisesti keskeytynyt')
  })
})
