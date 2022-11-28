import { test, expect } from './base'

const ESH_OID = '1.2.246.562.24.00000000065'

test.describe('European School of Helsinki', () => {
  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset(false)
  })

  test.beforeEach(async ({ loginPage, oppijaPage }) => {
    await loginPage.apiLoginAsUser('kalle', 'kalle')
    await oppijaPage.goto(ESH_OID)
  })

  test('Näyttää oppijan tiedot oikein', async ({ oppijaPage }) => {
    await expect(oppijaPage.oppijaHeading).toContainText(
      'Eurooppalainen, Emilia (050707A130V)'
    )
    await expect(oppijaPage.hetu).toContainText('050707A130V')
    await expect(oppijaPage.koulutusmoduuli).toContainText(
      'European Baccalaureate2023'
    )
    await expect(oppijaPage.luokka).toContainText('S7 EN')
    await expect(oppijaPage.toimipiste).toContainText(
      'Helsingin eurooppalainen koulu'
    )
  })

  test(`Lisää S7-vuosiluokan osasuoritukseen uuden alaosasuorituksen`, async ({
    customPage,
    oppijaPage
  }) => {
    await oppijaPage.clickSuoritusTabByLabel('S1', 'first')
    await oppijaPage.clickSuoritusTabByLabel('S7', 'first')
    await oppijaPage.avaaMuokkausnäkymä()

    await customPage
      .getByRole('button', {
        name: 'Laajenna suoritus Information and Communication Technology',
        expanded: false
      })
      .click()

    await customPage
      .getByRole('combobox', { name: 'Lisää alaosasuoritus' })
      .click()
    await customPage.getByRole('listitem', { name: 'Year mark' }).click()
  })

  test.describe('Primary-vuosiluokan suoritukset', () => {
    test(`Lisää P1-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('P1', 'first')
      await oppijaPage.avaaMuokkausnäkymä()
      await customPage
        .getByRole('combobox', { name: 'Lisää osasuoritus' })
        .click()
      await customPage
        .getByRole('listitem', {
          name: 'Advanced studies of the second language'
        })
        .click()
      await oppijaPage.tallenna()
    })
    test(`Lisää P2-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('P2', 'first')
      await oppijaPage.avaaMuokkausnäkymä()
      await customPage
        .getByRole('combobox', { name: 'Lisää osasuoritus' })
        .click()
      await customPage
        .getByRole('listitem', {
          name: 'Advanced studies of the second language'
        })
        .click()
      await oppijaPage.tallenna()
    })
    test(`Lisää P3-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('P3', 'first')
      await oppijaPage.avaaMuokkausnäkymä()
      await customPage
        .getByRole('combobox', { name: 'Lisää osasuoritus' })
        .click()
      await customPage
        .getByRole('listitem', {
          name: 'Advanced studies of the second language'
        })
        .click()
      await oppijaPage.tallenna()
    })
    test(`Lisää P4-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('P4', 'first')
      await oppijaPage.avaaMuokkausnäkymä()
      await customPage
        .getByRole('combobox', { name: 'Lisää osasuoritus' })
        .click()
      await customPage
        .getByRole('listitem', {
          name: 'Advanced studies of the second language'
        })
        .click()
      await oppijaPage.tallenna()
    })
    test(`Lisää P5-vuosiluokan suoritukseen uuden osasuorituksen`, async ({
      customPage,
      oppijaPage
    }) => {
      await oppijaPage.clickSuoritusTabByLabel('P1', 'first')
      await oppijaPage.avaaMuokkausnäkymä()
      await customPage
        .getByRole('combobox', { name: 'Lisää osasuoritus' })
        .click()
      await customPage
        .getByRole('listitem', {
          name: 'Advanced studies of the second language'
        })
        .click()
      await oppijaPage.tallenna()
    })

    test.afterEach(async ({ customPage }) => {
      // Tämä pitää muistaa kutsua jokaisen testin päätteeksi, jotta saman kontekstin jakava sivuobjekti sulkeutuu oikein. Muuten selainikkunat jäävät päälle.
      await customPage.close()
    })
  })

  test.describe('Luodessa opiskeluoikeutta', async () => {
    test(`Autofillaa S1-vuosiluokan osasuoritukset oikein`, async ({
      uusiOppijaPage
    }) => {
      // Satunnaisesti generoitu hetu
      await uusiOppijaPage.lisaaOppija({
        hetu: '110363-155S',
        aloituspäivä: new Date(),
        curriculum: '2023',
        etunimet: '',
        luokkaAste: '',
        opiskeluoikeudenTila: '',
        opiskeluoikeus: 'European School of Helsinki',
        oppilaitos: '',
        sukunimi: '',
        suorituskieli: ''
      })
    })
  })
})
