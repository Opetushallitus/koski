import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

/**
 * Portattu perusopetusSpec_2.js:1526-1618 "Päätason suorituksen poistaminen >
 * Nuorten perusopetus" v2-editorille. Testataan vain perusflowta:
 * mitätöintilinkki, vahvistusnäkymä ja peruminen sekä vahvistettu poisto.
 *
 * Aikuisten perusopetus jää v2-porttauksen ulkopuolelle, koska
 * aikuistenperusopetus-opiskeluoikeudelle ei ole v2-editoriadapteria.
 * V1:n lähdejärjestelmällisen opiskeluoikeuden pääkäyttäjäpoistoa ei myöskään
 * portata näkyväksi kontrolliksi: v2 estää muokkaustilan kaikilta
 * lähdejärjestelmäkytkennällisiltä opiskeluoikeuksilta.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

// Ville Vuosiluokkalainen (010100-325X) = 1.2.246.562.24.00000000011
const villeOid = '1.2.246.562.24.00000000011'
const villeUrl = `${villeOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

// Lasse Luokallejäänyt (170186-6520) = kaksi 7. vuosiluokan suoritusta
const lasseOid = '1.2.246.562.24.00000000009'
const lasseUrl = `${lasseOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

// Pertti Perusopetuksensiirto (010100-071R) = lähdejärjestelmästä tuotu perusopetus
const perttiOid = '1.2.246.562.24.00000000059'
const perttiUrl = `${perttiOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

type DeletePäätasonSuoritusRequest = {
  luokka?: string
  koulutusmoduuli?: {
    tunniste?: {
      koodiarvo?: string
    }
  }
}

const poistaSuoritusButton = (page: Page) =>
  page.getByTestId('oo.0.suoritukset.0.button')

const expectPerusopetusV2Loaded = async (page: Page) => {
  await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()
}

const suoritusTabs = (page: Page) =>
  page.locator('[data-testid^="oo.0.suoritusTabs."][data-testid$=".tab"]')

const expectVuosiluokkaTabs = async (page: Page) => {
  const tabs = suoritusTabs(page)
  await expect(tabs).toHaveCount(3)
  await expect(tabs.filter({ hasText: '7. vuosiluokka' })).toHaveCount(1)
  await expect(tabs.filter({ hasText: '8. vuosiluokka' })).toHaveCount(1)
  await expect(tabs.filter({ hasText: '9. vuosiluokka' })).toHaveCount(1)
}

test.describe('Perusopetuksen uusi käyttöliittymä: päätason suorituksen poisto', () => {
  test.use({ storageState: virkailija('kalle') })

  test('Poista suoritus -painike ja peruuta-flow', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    // Kaisalla on 4 päätason suoritusta — aloitetaan muokkaus oppimäärä-tabilla
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Poista suoritus -painike näkyy
    const poistoBtn = page.getByTestId('oo.0.suoritukset.0.button')
    await expect(poistoBtn).toBeVisible()
    await expect(poistoBtn).toContainText('Poista suoritus')
    await poistoBtn.click()

    // Vahvistus- ja peruuta-painikkeet näkyvät
    const confirmBtn = page.getByTestId('oo.0.suoritukset.0.confirm')
    const cancelBtn = page.getByTestId('oo.0.suoritukset.0.cancel')
    await expect(confirmBtn).toBeVisible()
    await expect(confirmBtn).toContainText('Vahvista poisto')
    await expect(cancelBtn).toBeVisible()

    // Peruuta palaa alkutilaan
    await cancelBtn.click()
    await expect(poistoBtn).toBeVisible()
    await expect(confirmBtn).not.toBeVisible()

    // Kaikki 4 tabia yhä näkyvissä
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toBeVisible()
    await expect(page.getByTestId('oo.0.suoritusTabs.3.tab')).toBeVisible()
  })

  test('Päättötodistuksen poisto jättää vuosiluokat jäljelle', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(kaisaUrl)

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Poista päätason oppimäärä (tab 0 = Päättötodistus)
    await page.getByTestId('oo.0.suoritukset.0.button').click()
    await page.getByTestId('oo.0.suoritukset.0.confirm').click()

    // Poisto on välitön — tabit päivittyvät näyttämään 3 vuosiluokkaa
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toContainText(
      '9. vuosiluokka',
      { timeout: 15000 }
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.1.tab')).toContainText(
      '8. vuosiluokka'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.2.tab')).toContainText(
      '7. vuosiluokka'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.3.tab')).not.toBeVisible()

    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
      timeout: 15000
    })
    await expect(
      page.getByTestId('oo.0.opiskeluoikeus.cancelEdit')
    ).not.toBeVisible()

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.cancelEdit').click()
    await expectVuosiluokkaTabs(page)
  })

  test('Toistetun vuosiluokan poisto lähettää valitun suorituksen backendille', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    test.setTimeout(60000)
    await fixtures.reset()
    await oppijaPage.goto(lasseUrl)

    await page.evaluate(() => {
      if ('DISABLE_EXIT_HOOKS' in window) {
        window.DISABLE_EXIT_HOOKS = true
      }
    })

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Tab 4 on jälkimmäinen 7. vuosiluokan suoritus (luokka 7C)
    await page.getByTestId('oo.0.suoritusTabs.4.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.4.luokka.edit.input')
    ).toHaveValue('7C')

    const deleteRequestPromise = page.waitForRequest(
      (request) =>
        request.method() === 'POST' &&
        request.url().includes('/delete-paatason-suoritus')
    )
    const deleteResponsePromise = page.waitForResponse(
      (response) =>
        response.request().method() === 'POST' &&
        response.url().includes('/delete-paatason-suoritus')
    )

    await page.getByTestId('oo.0.suoritukset.4.button').click()
    await page.getByTestId('oo.0.suoritukset.4.confirm').click()

    const deleteRequest = await deleteRequestPromise
    const deleteResponse = await deleteResponsePromise
    const deleteBody =
      deleteRequest.postDataJSON() as DeletePäätasonSuoritusRequest

    expect(deleteBody.koulutusmoduuli?.tunniste?.koodiarvo).toBe('7')
    expect(deleteBody.luokka).toBe('7C')
    expect(deleteResponse.ok()).toBeTruthy()

    await oppijaPage.goto(lasseUrl)
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.3.luokka.value')
    ).toHaveText('7A')
    await expect(page.getByTestId('oo.0.suoritusTabs.4.tab')).not.toBeVisible()
  })

  test('Yhden päätason suorituksen opiskeluoikeudessa Poista-painiketta ei näytetä', async ({
    page,
    oppijaPage,
    fixtures
  }) => {
    await fixtures.reset()
    await oppijaPage.goto(villeUrl)

    // Villellä on vain yksi päätason suoritus (7. vuosiluokka)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Poista-painiketta ei näy
    await expect(
      page.getByTestId('oo.0.suoritukset.0.button')
    ).not.toBeVisible()
  })

  test.describe('Lähdejärjestelmästä tuotu nuorten perusopetus', () => {
    test.describe('oppilaitoksen tallentaja', () => {
      test.use({ storageState: virkailija('tallentaja') })

      test('ei voi poistaa päätason suoritusta', async ({
        page,
        oppijaPage,
        fixtures
      }) => {
        await fixtures.reset()
        await oppijaPage.goto(perttiUrl)
        await expectPerusopetusV2Loaded(page)

        await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(
          0
        )
        await expect(poistaSuoritusButton(page)).toHaveCount(0)
      })
    })

    test.describe('oppilaitoksen pääkäyttäjä', () => {
      test.use({ storageState: virkailija('stadin-pää') })

      test('näkee lähdejärjestelmäkontrollit muttei v2-päätason poistoa', async ({
        page,
        oppijaPage,
        fixtures
      }) => {
        await fixtures.reset()
        await oppijaPage.goto(perttiUrl)
        await expectPerusopetusV2Loaded(page)

        await expect(
          page.getByTestId('oo.0.opiskeluoikeus.invalidate.button')
        ).toBeVisible()
        await expect(
          page.getByTestId('oo.0.opiskeluoikeus.puraKytkenta.button')
        ).toBeVisible()
        await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(
          0
        )
        await expect(poistaSuoritusButton(page)).toHaveCount(0)
      })
    })
  })
})
