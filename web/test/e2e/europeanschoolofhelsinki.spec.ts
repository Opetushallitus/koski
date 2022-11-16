import { test, expect } from './base'

test.describe('European School of Helsinki', () => {
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test.beforeEach(async ({ loginPage, virkailijaPage, oppijaHaku }) => {
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    await virkailijaPage.goto()
    const hakutulokset = await oppijaHaku.search('050707A130V')
    await hakutulokset.clickOnFirst()
  })

  test('Näyttää oppijan tiedot oikein', async ({ page, oppijaPage }) => {
    await expect(oppijaPage.oppijaHeading).toContainText(
      'Eurooppalainen, Emilia (050707A130V)'
    )
    await expect(oppijaPage.hetu).toContainText('050707A130V')
    expect(page.getByTestId('koulutusmoduuli-value')).toContainText(
      'Class S7 2023'
    )
    expect(page.getByTestId('luokka-value')).toContainText('S7A')
    expect(page.getByTestId('alkamispäivä-value')).toContainText('1.8.2018')
    expect(page.getByTestId('toimipiste-value')).toContainText(
      'Helsingin eurooppalainen koulu'
    )
    expect(page.getByTestId('suorituskieli-value')).toContainText('englanti')
  })
  test.skip('Näyttää S7-luokan osasuoritukset oikein', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.clickSuoritusTab(0)
    expect(page.getByText('Physical Education')).toBeVisible()
    expect(page.getByText('First language, sloveeni')).toBeVisible()
    expect(page.getByText('Mathematics')).toBeVisible()
    await oppijaPage.avaaKaikkiOsasuoritukset()
    expect(page.getByTestId('Physical Education - A')).toBeVisible()
    expect(page.getByTestId('Physical Education - B')).toBeVisible()
    expect(page.getByTestId('Physical Education - Year mark')).toBeVisible()
    expect(page.getByTestId('First language, sloveeni - A')).toBeVisible()
    expect(page.getByTestId('First language, sloveeni - B')).toBeVisible()
    expect(
      page.getByTestId('First language, sloveeni - Year mark')
    ).toBeVisible()
    expect(page.getByTestId('Mathematics - A')).toBeVisible()
    expect(page.getByTestId('Mathematics - B')).toBeVisible()
    expect(page.getByTestId('Mathematics - Year mark')).toBeVisible()
  })
  test.skip('Lisää A- alaosasasuorituksen oikein', async ({ oppijaPage }) => {
    await oppijaPage.avaaMuokkausnäkymä()
    // Physical Education - A
    const osasuoritus = oppijaPage.getTutkinnonOsa("tutkinnon-osa-PE")
    await osasuoritus.avaa()
    // await oppijaPage.osasuoritus(0).sulje();
    const dropdown = osasuoritus.osasuoritusDropdown()
    // await dropdown.valitse('A')
  })
})
