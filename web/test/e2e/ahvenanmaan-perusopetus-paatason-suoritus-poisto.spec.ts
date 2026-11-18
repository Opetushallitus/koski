import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Ahvenanmaan perusopetuksen päätason suorituksen poisto v2-editorissa.
 *
 * Opiskeluoikeus luodaan uusi oppija -dialogissa, jolloin sillä on vain
 * päättötodistuksen suoritus (Avgångsbetyg). Vuosiluokat lisätään editorissa.
 */

const oppilaitos = 'Övernäs skola'
const oppimääränKey = 'suorituksentyyppi_ahvenanmaanperusopetuksenoppimaara'
const hetu = '210310A247H'

const editButton = 'oo.0.opiskeluoikeus.edit'
const saveButton = 'oo.0.opiskeluoikeus.save'

type Suoritus = {
  tyyppi: { koodiarvo: string }
  koulutusmoduuli: { tunniste: { koodiarvo: string } }
}

const tallennetutSuoritukset = async (page: Page): Promise<string[]> => {
  const oid = page.url().match(/oppija\/(1\.2\.[\d.]+)/)?.[1]
  expect(oid).toBeDefined()
  const response = await page.request.get(`/koski/api/oppija/${oid}`)
  expect(response.ok()).toBeTruthy()
  const oppija = await response.json()
  return oppija.opiskeluoikeudet[0].suoritukset.map((s: Suoritus) =>
    s.tyyppi.koodiarvo === 'ahvenanmaanperusopetuksenvuosiluokka'
      ? s.koulutusmoduuli.tunniste.koodiarvo
      : 'oppimäärä'
  )
}

test.describe('Ahvenanmaan perusopetuksen päätason suorituksen poisto', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures, uusiOppijaPage, page }) => {
    test.setTimeout(90000)
    await fixtures.reset()
    await uusiOppijaPage.goTo(hetu)
    await uusiOppijaPage.fill({
      etunimet: 'Anton',
      sukunimi: 'Ålänning',
      oppilaitos,
      opiskeluoikeus: 'Ahvenanmaan perusopetus',
      aloituspäivä: new Date(2018, 7, 1),
      opiskeluoikeudenTila: 'Läsnä'
    })
    await uusiOppijaPage.controls.oppimäärä.set(oppimääränKey)
    await uusiOppijaPage.submitAndExpectSuccess()

    await page.getByTestId(editButton).click()
    await lisääVuosiluokka(page, '1', '1A', '15.8.2018')
    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible()
    expect(await tallennetutSuoritukset(page)).toEqual(['oppimäärä', '1'])
  })

  test('tallennetun vuosiluokan poisto onnistuu', async ({ page }) => {
    await page.getByTestId(editButton).click()
    await page
      .getByTestId('oo.0.suoritusTabs.1.tab')
      .filter({ hasText: '1. vuosiluokka' })
      .click()
    await page.getByTestId('oo.0.suoritukset.1.button').click()
    await page.getByTestId('oo.0.suoritukset.1.confirm').click()

    await expect(page.getByTestId(editButton)).toBeVisible()
    await expect(page.getByTestId('globalErrors')).toHaveCount(0)
    expect(await tallennetutSuoritukset(page)).toEqual(['oppimäärä'])
  })

  test('tallentamattoman vuosiluokan poisto ei poista tallennettua vuosiluokkaa', async ({
    page
  }) => {
    // 2. vuosiluokka lajitellaan tallennetun 1. vuosiluokan edelle.
    await page.getByTestId(editButton).click()
    await lisääVuosiluokka(page, '2', '2A', '15.8.2019')
    await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
      '2. vuosiluokka'
    )
    await page.getByTestId('oo.0.suoritukset.1.button').click()
    await page.getByTestId('oo.0.suoritukset.1.confirm').click()

    const tabs = page.locator(
      '[data-testid^="oo.0.suoritusTabs."][data-testid$=".tab"]'
    )
    await expect(tabs.filter({ hasText: '2. vuosiluokka' })).toHaveCount(0)
    await expect(tabs.filter({ hasText: '1. vuosiluokka' })).toHaveCount(1)
    expect(await tallennetutSuoritukset(page)).toEqual(['oppimäärä', '1'])

    await page.getByTestId(saveButton).click()
    await expect(page.getByTestId(editButton)).toBeVisible()
    expect(await tallennetutSuoritukset(page)).toEqual(['oppimäärä', '1'])
  })
})

async function lisääVuosiluokka(
  page: Page,
  luokkaAste: string,
  luokka: string,
  alkamispäivä: string
) {
  await page
    .getByRole('button', { name: /lisää vuosiluokan suoritus/i })
    .click()

  const modal = page.locator('.Modal')
  await expect(modal).toBeVisible()

  const tunnisteInput = page.getByTestId(
    'oo.0.modal.uusiVuosiluokanSuoritus.tunniste.input'
  )
  if (!(await tunnisteInput.inputValue()).startsWith(`${luokkaAste}.`)) {
    await tunnisteInput.click()
    await modal
      .locator('.Select__optionLabel')
      .filter({ hasText: new RegExp(`^${luokkaAste}\\. vuosiluokka`) })
      .first()
      .click()
  }

  await page
    .getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.luokka.input')
    .fill(luokka)
  const alkamispäiväInput = page.getByTestId(
    'oo.0.modal.uusiVuosiluokanSuoritus.alkamispäivä.edit.input'
  )
  await alkamispäiväInput.fill(alkamispäivä)
  await alkamispäiväInput.blur()

  await page.getByTestId('oo.0.modal.uusiVuosiluokanSuoritus.submit').click()
  await expect(modal).not.toBeVisible()
}
