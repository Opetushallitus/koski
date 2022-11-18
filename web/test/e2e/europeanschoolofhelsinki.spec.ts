import { test, expect } from './base'

const ESH_OID = '1.2.246.562.24.00000000065'
const eshVuosiluokat = [
  'P1',
  // 'P2',
  'P3',
  'P4',
  'P5',
  'S1',
  'S2',
  'S3',
  'S4',
  'S5',
  'S6',
  'S7'
]
test.describe('European School of Helsinki', () => {
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset(false)
  })

  test.beforeEach(async ({ loginPage, oppijaPage }) => {
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    await oppijaPage.goto(ESH_OID)
  })

  test('Näyttää oppijan tiedot oikein', async ({ page, oppijaPage }) => {
    await expect(oppijaPage.oppijaHeading).toContainText(
      'Eurooppalainen, Emilia (050707A130V)'
    )
    await expect(oppijaPage.hetu).toContainText('050707A130V')
    await expect(oppijaPage.koulutusmoduuli).toContainText('S72023')
    await expect(oppijaPage.luokka).toContainText('S7A')
    await expect(oppijaPage.alkamispäivä).toContainText('1.8.2018')
    await expect(oppijaPage.toimipiste).toContainText(
      'Helsingin eurooppalainen koulu'
    )
  })

  test(`Lisää S7-vuosiluokan osasuoritukseen uuden alaosasuorituksen`, async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.clickSuoritusTabByLabel('S1')
    await oppijaPage.clickSuoritusTabByLabel('S7')
    await oppijaPage.avaaMuokkausnäkymä()

    await page
      .getByRole('button', {
        name: 'Laajenna suoritus Information and Communication Technology',
        expanded: false
      })
      .click()
    await page.getByRole('combobox', { name: 'Lisää alaosasuoritus' }).click()
    await page.getByRole('listitem', { name: 'Year mark' }).click()
  })

  test.describe('Vuosiluokan suoritukset', () => {
    for (const luokka of eshVuosiluokat) {
      test(`Lisää ${luokka}-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
        page,
        oppijaPage
      }) => {
        await oppijaPage.clickSuoritusTabByLabel(luokka)
        await oppijaPage.avaaMuokkausnäkymä()
        await page.getByRole('combobox', { name: 'Lisää osasuoritus' }).click()
        await page
          .getByRole('listitem', {
            name: 'Advanced studies of the second language'
          })
          .click()
      })
    }
  })
})
