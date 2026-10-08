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
 *
 * Kuten vanhassa käyttöliittymässä, mitätöintioikeudellinen käyttäjä, joka ei
 * voi muokata opiskeluoikeutta, poistaa suorituksen katselutilassa. Näin
 * oppilaitoksen pääkäyttäjä voi poistaa suorituksen myös lähdejärjestelmästä
 * siirretyltä opiskeluoikeudelta (tiedonsiirron mitätöintioikeus), jolla v2
 * ei salli muokkaustilaa kenellekään.
 */

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus`

// Ville Vuosiluokkalainen (010100-325X) = 1.2.246.562.24.00000000011
const villeOid = '1.2.246.562.24.00000000011'
const villeUrl = `${villeOid}?opiskeluoikeudenTyyppi=perusopetus`

// Lasse Luokallejäänyt (170186-6520) = kaksi 7. vuosiluokan suoritusta
const lasseOid = '1.2.246.562.24.00000000009'
const lasseUrl = `${lasseOid}?opiskeluoikeudenTyyppi=perusopetus`

// Pertti Perusopetuksensiirto (010100-071R) = lähdejärjestelmästä tuotu perusopetus
const perttiOid = '1.2.246.562.24.00000000059'
const perttiUrl = `${perttiOid}?opiskeluoikeudenTyyppi=perusopetus`

// Miia Monikoululainen (180497-112F): stadin-pää näkee vain Kulosaaren ala-asteen
// (Helsingin kaupunki) käyttöliittymästä tallennetun opiskeluoikeuden.
const miiaOid = '1.2.246.562.24.00000000012'
const miiaUrl = `${miiaOid}?opiskeluoikeudenTyyppi=perusopetus`

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

// Katselutilan poistopainike on valitun suorituksen testId-polussa.
const valitunSuorituksenPoisto = (page: Page, id: string) =>
  page.getByTestId(new RegExp(`^oo\\.0\\.suoritukset\\.\\d+\\.${id}$`))

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

    // Kaisalla on 4 päätason suoritusta. Näkymä avautuu viimeisimmälle
    // vuosiluokalle; valitaan oppimäärä (tab 0) ja aloitetaan muokkaus.
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
    // Muokkausoikeudellinen poistaa suorituksen vain muokkaustilassa.
    await expect(poistaSuoritusButton(page)).toHaveCount(0)
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

    // Valitaan oppimäärä (tab 0); näkymä avautuu viimeisimmälle vuosiluokalle.
    await page.getByTestId('oo.0.suoritusTabs.0.tab').click()
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

    // Saman vuosiluokan suoritukset järjestetään alkamispäivän mukaan
    // uusimmasta vanhimpaan, joten 7C (alkanut 16.8.2013) on tabissa 3 ja
    // 7A (15.8.2013) tabissa 4.
    await page.getByTestId('oo.0.suoritusTabs.4.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.4.luokka.edit.input')
    ).toHaveValue('7A')

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
    expect(deleteBody.luokka).toBe('7A')
    expect(deleteResponse.ok()).toBeTruthy()

    await oppijaPage.goto(lasseUrl)
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()
    await expect(
      page.getByTestId('oo.0.suoritukset.3.luokka.value')
    ).toHaveText('7C')
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

      test('näkee lähdejärjestelmäkontrollit ja voi poistaa suorituksen katselutilassa', async ({
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

        await suoritusTabs(page).filter({ hasText: '7. vuosiluokka' }).click()
        await valitunSuorituksenPoisto(page, 'button').click()
        await valitunSuorituksenPoisto(page, 'confirm').click()

        await expect(page.getByText('Suoritus poistettu')).toBeVisible()
        await expect(
          suoritusTabs(page).filter({ hasText: '7. vuosiluokka' })
        ).toHaveCount(0)
        await expect(suoritusTabs(page)).toHaveCount(3)
      })
    })
  })

  test.describe('Oppilaitoksen pääkäyttäjä käyttöliittymästä tallennetulla opiskeluoikeudella', () => {
    test.use({ storageState: virkailija('stadin-pää') })

    test('poistaa suorituksen katselutilassa ilman muokkausoikeutta', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(miiaUrl)
      await expectPerusopetusV2Loaded(page)
      await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)

      await suoritusTabs(page).filter({ hasText: '6. vuosiluokka' }).click()
      await valitunSuorituksenPoisto(page, 'button').click()
      await valitunSuorituksenPoisto(page, 'cancel').click()
      await expect(valitunSuorituksenPoisto(page, 'confirm')).toHaveCount(0)

      await valitunSuorituksenPoisto(page, 'button').click()
      await valitunSuorituksenPoisto(page, 'confirm').click()

      await expect(page.getByText('Suoritus poistettu')).toBeVisible()
      await expect(suoritusTabs(page)).toHaveText([
        'Päättötodistus',
        '7. vuosiluokka'
      ])
    })

    test('näyttää virheen, jos opiskeluoikeutta on muutettu sivun avaamisen jälkeen', async ({
      page,
      oppijaPage,
      fixtures
    }) => {
      await fixtures.reset()
      await oppijaPage.goto(miiaUrl)
      await expectPerusopetusV2Loaded(page)

      // Poistetaan 7. vuosiluokka suoraan rajapinnasta, jolloin näkymän
      // versionumero vanhenee.
      const vastaus = await page.request.get(
        `/koski/api/oppija/${miiaOid}/uiv2?class_refs=true`
      )
      const opiskeluoikeus = (await vastaus.json()).opiskeluoikeudet[0]
      const seitsemäs = opiskeluoikeus.suoritukset.find(
        (s: DeletePäätasonSuoritusRequest) =>
          s.koulutusmoduuli?.tunniste?.koodiarvo === '7'
      )
      const poisto = await page.request.post(
        `/koski/api/opiskeluoikeus/${opiskeluoikeus.oid}/${opiskeluoikeus.versionumero}/delete-paatason-suoritus`,
        { data: seitsemäs }
      )
      expect(poisto.ok()).toBeTruthy()

      await suoritusTabs(page).filter({ hasText: '6. vuosiluokka' }).click()
      await valitunSuorituksenPoisto(page, 'button').click()
      await valitunSuorituksenPoisto(page, 'confirm').click()

      await expect(page.getByTestId('globalErrors')).toContainText(
        'Yritetty päivittää vanhan version päälle'
      )
      await expect(valitunSuorituksenPoisto(page, 'button')).toBeVisible()
    })
  })
})
