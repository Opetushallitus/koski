import type { Page } from '@playwright/test'
import { expect, test } from './base'
import { virkailija } from './setup/auth'

const kaisaOid = '1.2.246.562.24.00000000007'
const kaisaUrl = `${kaisaOid}?opiskeluoikeudenTyyppi=perusopetus&perusopetus-v2=true`

const tallenna = async (page: Page) => {
  await page.getByTestId('oo.0.opiskeluoikeus.save').click()
  await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toBeVisible({
    timeout: 15000
  })
}

const valitseSelectista = async (
  page: Page,
  testId: string,
  teksti: RegExp
) => {
  await page.getByTestId(testId).click()
  await page.locator('.Select__optionLabel').filter({ hasText: teksti }).click()
}

const suoritusPerusteRivi = (page: Page) =>
  page.locator('.KeyValueRow').filter({
    has: page.getByTestId('oo.0.suoritukset.0.koulutus')
  })

const suorituksenTiedotRivienOtsikot = (page: Page, suoritusIndex: number) =>
  page
    .getByTestId(`oo.0.suoritukset.${suoritusIndex}.koulutus`)
    .locator(
      'xpath=ancestor::ul[contains(concat(" ", normalize-space(@class), " "), " KeyValueTable ")][1]/li[contains(concat(" ", normalize-space(@class), " "), " KeyValueRow ")]'
    )
    .locator('.KeyValueRow__name')

const suorituksenTiedotRivi = (
  page: Page,
  suoritusIndex: number,
  fieldTestId: string
) =>
  page
    .getByTestId(`oo.0.suoritukset.${suoritusIndex}.${fieldTestId}`)
    .locator(
      'xpath=ancestor::li[contains(concat(" ", normalize-space(@class), " "), " KeyValueRow ")][1]'
    )

const expectLabelsInOrder = async (
  labelsLocator: ReturnType<typeof suorituksenTiedotRivienOtsikot>,
  labels: string[]
) => {
  const actual = await labelsLocator.allTextContents()
  let previousIndex = -1

  for (const label of labels) {
    const index = actual.findIndex(
      (actualLabel, actualIndex) =>
        actualIndex > previousIndex && actualLabel.trim() === label
    )
    expect(index, `${label} pitäisi löytyä järjestyksessä`).toBeGreaterThan(
      previousIndex
    )
    previousIndex = index
  }
}

test.describe('Perusopetuksen uusi käyttöliittymä: suoritusten tiedot regressiot', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Suorituksen tietorivit ovat vanhan käyttöliittymän järjestyksessä', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    await expect(suorituksenTiedotRivienOtsikot(page, 0)).toContainText([
      'Koulutus',
      'Oppilaitos / toimipiste',
      'Suoritustapa',
      'Suorituskieli',
      'Koulusivistyskieli'
    ])
    await expectLabelsInOrder(suorituksenTiedotRivienOtsikot(page, 0), [
      'Suorituskieli',
      'Koulusivistyskieli'
    ])

    const koulusivistyskieliRivi = suorituksenTiedotRivi(
      page,
      0,
      'koulusivistyskieli'
    )
    await koulusivistyskieliRivi.locator('.info-icon').click()
    await expect(koulusivistyskieliRivi.locator('.info-content')).toContainText(
      'Koulusivistyskieli määräytyy seuraavien suoritusten perusteella'
    )
    await expect(koulusivistyskieliRivi.locator('.info-content')).toContainText(
      'Peruskoulun tai lukion päättötodistuksessa hyväksytty arvosana äidinkielenä opiskellusta suomen tai ruotsin kielestä.'
    )
    await koulusivistyskieliRivi.locator('.info-content-close').click()

    await page.getByTestId('oo.0.suoritusTabs.2.tab').click()

    await expect(suorituksenTiedotRivienOtsikot(page, 2)).toContainText([
      'Luokka-aste',
      'Luokka',
      'Oppilaitos / toimipiste',
      'Alkamispäivä',
      'Suoritustapa',
      'Suorituskieli',
      'Muut suorituskielet',
      'Kielikylpykieli'
    ])
  })

  test('Päättötodistuksen koulusivistyskieli on viimeisenä myös muokkaustilassa', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await expectLabelsInOrder(suorituksenTiedotRivienOtsikot(page, 0), [
      'Koulutus',
      'Oppilaitos / toimipiste',
      'Suoritustapa',
      'Suorituskieli',
      'Muut suorituskielet',
      'Todistuksella näkyvät lisätiedot',
      'Koulusivistyskieli'
    ])
    const todistuksellaNäkyvätLisätiedot = page.locator(
      'textarea[data-testid="oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.edit.input"]'
    )
    await expect(todistuksellaNäkyvätLisätiedot).toBeVisible()

    await todistuksellaNäkyvätLisätiedot.fill('Ensimmäinen rivi\nToinen rivi')
    await tallenna(page)

    const lisätiedotValue = page.getByTestId(
      'oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.value'
    )
    await expect(lisätiedotValue).toContainText('Ensimmäinen rivi')
    await expect(lisätiedotValue).toContainText('Toinen rivi')
    await expect(lisätiedotValue).toHaveCSS('white-space', 'pre-line')
  })

  test('Vuosiluokan tyhjä suoritustapa näytetään muokkaustilassa', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    await page.getByTestId('oo.0.suoritusTabs.1.tab').click()
    expect(
      (await suorituksenTiedotRivienOtsikot(page, 1).allTextContents()).map(
        (label) => label.trim()
      )
    ).not.toContain('Suoritustapa')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await expectLabelsInOrder(suorituksenTiedotRivienOtsikot(page, 1), [
      'Luokka-aste',
      'Luokka',
      'Oppilaitos / toimipiste',
      'Alkamispäivä',
      'Suoritustapa',
      'Suorituskieli',
      'Muut suorituskielet',
      'Kielikylpykieli'
    ])
    await expect(
      page.getByTestId('oo.0.suoritukset.1.suoritustapa.edit.input')
    ).toBeVisible()
    await expect(
      page.getByTestId('oo.0.suoritukset.1.kielikylpykieli.edit.input')
    ).toBeVisible()
    await expect(
      page.getByTestId('oo.0.suoritukset.1.jääLuokalle.ohje')
    ).toHaveText(
      'Oppiaineiden arvioinnit syötetään 9. vuosiluokalla vain, jos oppilas jää luokalle'
    )
    expect(
      (await suorituksenTiedotRivienOtsikot(page, 1).allTextContents()).map(
        (label) => label.trim()
      )
    ).not.toContain('Osa-aikainen erityisopetus')
  })

  test('Tutkinnon perusteen diaarinumeron muutos tallentuu', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    await expect(suoritusPerusteRivi(page)).toContainText('104/011/2014')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await valitseSelectista(
      page,
      'oo.0.suoritukset.0.peruste.edit.input',
      /^1\/011\/2004\b/
    )

    await tallenna(page)

    await expect(suoritusPerusteRivi(page)).toContainText('1/011/2004')
  })

  test('Täydentävien oman äidinkielen opintojen arvosanat näkyvät kerran lisäysmodalissa', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    await page
      .getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.edit.input')
      .click()
    await expect(
      page.locator('.Select__optionLabel').filter({ hasText: /^10$/ })
    ).toBeVisible()
    await page.keyboard.press('Escape')

    await page
      .getByTestId('oo.0.suoritukset.0.omanÄidinkielenOpinnot.lisää')
      .click()
    await page.getByTestId('oo.0.suoritukset.0.modal.arvosana.input').click()

    const arvosanaLabels = await page
      .getByTestId('oo.0.suoritukset.0.modal.arvosana.options')
      .locator('.Select__optionLabel')
      .allTextContents()

    expect(arvosanaLabels).toHaveLength(10)
    expect(new Set(arvosanaLabels).size).toBe(arvosanaLabels.length)
    expect(arvosanaLabels).toContain('10 erinomainen')
    expect(arvosanaLabels).toContain('S hyväksytty')
  })

  test.describe('Organisaatiovalitsin pääkäyttäjänä', () => {
    test.use({ storageState: virkailija('pää') })

    test('näyttää nykyisen oppilaitoksen muokkaustilassa', async ({
      page,
      oppijaPage
    }) => {
      await oppijaPage.goto(kaisaUrl)
      await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

      const organisaatioInput = page.getByTestId(
        'oo.0.suoritukset.0.organisaatio.edit.input'
      )
      await expect(organisaatioInput).toHaveValue('Jyväskylän normaalikoulu')

      await organisaatioInput.click()
      await expect(
        page
          .locator('.Select__optionLabel')
          .filter({ hasText: /^Jyväskylän normaalikoulu$/ })
      ).toBeVisible()
    })
  })

  test('Ensimmäisen oppiaineen poisto ei korruptoi seuraavan oppiaineen arvosanaa', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(kaisaUrl)

    // 7. vuosiluokka on tab 3. Sen pakollisissa oppiaineissa kaikki
    // arvosanat ovat aluksi 4, joten muutos näkyy selvästi tallennuksen jälkeen.
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.nimi')
    ).toContainText('Äidinkieli')
    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.1.nimi')
    ).toContainText('B1-kieli')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.delete').click()

    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.nimi')
    ).toContainText('B1-kieli')

    await valitseSelectista(
      page,
      'oo.0.suoritukset.3.osasuoritukset.0.arvosana.edit.input',
      /^9$/
    )

    await tallenna(page)
    await page.getByTestId('oo.0.suoritusTabs.3.tab').click()

    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.nimi')
    ).toContainText('B1-kieli')
    await expect(
      page.getByTestId('oo.0.suoritukset.3.osasuoritukset.0.arvosana.value')
    ).toContainText('9')
    await expect(
      page
        .getByTestId('oppiaineet-pakolliset')
        .locator('[data-testid$=".nimi"]')
        .filter({ hasText: 'Äidinkieli' })
    ).not.toBeVisible()
  })
})
