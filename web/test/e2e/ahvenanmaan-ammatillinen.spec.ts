import { Page } from '@playwright/test'
import { expect, test } from './base'
import { kansalainen, virkailija } from './setup/auth'

/**
 * Testit Ahvenanmaan ammatillisen koulutuksen v2-editorille.
 *
 * Ålänning Ebba (120502A417R) on valmistunut sosiaali- ja terveysalan
 * perustutkinnosta Ålands yrkesgymnasiumista. Tutkinto viittaa manner-Suomen
 * perusteeseen OPH-5085-2021, jota ei ole ePerusteiden mockidatassa, joten
 * tutkinnon osat näytetään yhdessä taulukossa ilman ryhmiä.
 *
 * Tutkinnon osat ovat manner-Suomen luokkia ja näytetään manner-Suomen
 * komponenteilla. Päätason suorituksilla ei ole järjestämismuotoja eikä
 * työssäoppimisjaksoja, ja suoritustapa on aina reformi.
 */

const ebba = '1.2.246.562.24.00000000232'

const suoritus = (page: Page, testId: string) =>
  page.getByTestId(`oo.0.suoritukset.0.${testId}`)

const rivi = (page: Page, otsikko: string) =>
  page.locator('.KeyValueRow', {
    has: page.getByText(otsikko, { exact: true })
  })

const lisätietoRivi = (page: Page, otsikko: string) =>
  page.locator('.EditorContainer__lisatiedot > .KeyValueTable > .KeyValueRow', {
    has: page.getByText(otsikko, { exact: true })
  })

const osaRivi = (page: Page, nimi: string) =>
  page.locator('.OsasuoritusRow', {
    has: page
      .locator('.OsasuoritusRow__cellContent')
      .getByText(nimi, { exact: true })
  })

const valitseArvosana = async (
  page: Page,
  kenttä: string,
  arvosana: string
) => {
  await suoritus(page, `${kenttä}.input`).click()
  await suoritus(
    page,
    `${kenttä}.options.Arviointiasteikko ammatillinen 1-5.arviointiasteikkoammatillinen15_${arvosana}.item`
  ).click()
}

const muokkaa = async (page: Page) => {
  await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
  await expect(page.getByTestId('oo.0.opiskeluoikeus.save')).toBeVisible()
}

test.describe('Ahvenanmaan ammatillisen koulutuksen käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Näyttää perustutkinnon tiedot ja tutkinnon osat', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(ebba)

    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Ålands yrkesgymnasium, sosiaali- ja terveysalan perustutkinto'
    )
    await expect(page.getByTestId('oo.0.suoritusTabs.0.tab')).toHaveText(
      'Sosiaali- ja terveysalan perustutkinto'
    )
    await expect(suoritus(page, 'koulutus')).toHaveText(
      'Sosiaali- ja terveysalan perustutkinto'
    )
    // Perustelinkki puuttuu, koska perustetta ei ole ePerusteiden mockidatassa.
    await expect(page.locator('.AmmatillinenTutkinto')).toContainText(
      'OPH-5085-2021'
    )
    await expect(suoritus(page, 'suorituskieli.value')).toHaveText('ruotsi')
    await expect(
      suoritus(page, 'suorituksenVahvistus.value.details')
    ).toContainText('Vahvistus: 29.5.2026 Maarianhamina')

    await expect(osaRivi(page, 'Kasvun tukeminen ja ohjaus')).toBeVisible()
    await expect(
      osaRivi(page, 'Matemaattis-luonnontieteellinen osaaminen')
    ).toBeVisible()
    await expect(suoritus(page, 'yhteensa')).toHaveText('31 osp')

    await oppijaPageV2.openAllOsasuoritukset()
    await expect(
      suoritus(page, 'osasuoritukset.0.properties.toimipiste')
    ).toHaveText('Ålands yrkesgymnasium')
    await expect(
      suoritus(
        page,
        'osasuoritukset.1.properties.osasuoritukset.0.properties.arviointi.0.arvosana'
      )
    ).toHaveText('3')
  })

  test('Muokattavina ovat vain Ahvenanmaan suorituksen ja lisätietojen kentät', async ({
    page,
    oppijaPage
  }) => {
    await oppijaPage.goto(ebba)
    await muokkaa(page)

    await expect(rivi(page, 'Osaamisen hankkimistapa')).toBeVisible()
    await expect(rivi(page, 'Koulutussopimukset')).toBeVisible()
    await expect(rivi(page, 'Painotettu keskiarvo')).toBeVisible()
    await expect(rivi(page, 'Suoritustapa')).toHaveCount(0)
    await expect(rivi(page, 'Järjestämismuodot')).toHaveCount(0)
    await expect(rivi(page, 'Työssäoppimisjaksot')).toHaveCount(0)
    // Vain tutkinnon osan/osien suorituksella olevia kenttiä ei näytetä
    await expect(rivi(page, 'Toinen tutkintonimike')).toHaveCount(0)

    await expect(
      page.locator(
        '.EditorContainer__lisatiedot > .KeyValueTable > .KeyValueRow'
      )
    ).toHaveCount(3)
    await expect(lisätietoRivi(page, 'Ulkomaanjaksot')).toBeVisible()
    await expect(lisätietoRivi(page, 'Koulutusvienti')).toBeVisible()
    await expect(
      lisätietoRivi(page, 'Siirtynyt tutkinnon uusiin perusteisiin')
    ).toBeVisible()
  })

  test('Tutkinnon osan lisäys tallentuu', async ({
    page,
    oppijaPage,
    oppijaPageV2
  }) => {
    await oppijaPage.goto(ebba)
    await muokkaa(page)

    await suoritus(page, 'uusi-muu-tutkinnonosa.input').click()
    await suoritus(page, 'uusi-muu-tutkinnonosa.options.100016.item').click()
    // Vahvistetulla suorituksella ei saa olla arvioimattomia osia.
    await valitseArvosana(page, 'osasuoritukset.2.arvosana', '4')
    await oppijaPageV2.tallenna()

    await expect(
      osaRivi(page, 'Huolto- ja korjaustyöt').getByLabel('Suoritus valmis')
    ).toBeVisible()
    await expect(suoritus(page, 'osasuoritukset.2.arvosana.value')).toHaveText(
      '4'
    )
  })
})

test.describe('Ahvenanmaan ammatillinen koulutus kansalaisen näkymässä', () => {
  test.use({ storageState: kansalainen('120502A417R') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.apiLoginAsUser('kalle', 'kalle')
    await fixtures.reset()
    await fixtures.apiLogout()
  })

  test('Näyttää tutkinnon ja tutkinnon osat', async ({
    page,
    kansalainenPage
  }) => {
    await kansalainenPage.goto()
    await kansalainenPage.openOpiskeluoikeusByIndex(0)

    await expect(suoritus(page, 'koulutus')).toHaveText(
      'Sosiaali- ja terveysalan perustutkinto'
    )
    await expect(osaRivi(page, 'Kasvun tukeminen ja ohjaus')).toBeVisible()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toHaveCount(0)
  })
})
