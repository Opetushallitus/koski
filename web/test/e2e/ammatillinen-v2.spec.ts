import { expect, test } from './base'
import { getVirkailijaSession, kansalainen, virkailija } from './setup/auth'
import { KoskiFixtures } from './fixtures/KoskiFixtures'

test.describe('Osittaisen ammatillisen tutkinnon uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Renderöi osittaisen tutkinnon tiedot', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Stadin ammatti- ja aikuisopisto, luonto- ja ympäristöalan perustutkinto, osittainen'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toContainText('Muokkaa');


    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.laajuus.value')).toContainText('35 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')).toContainText('3');

    await page.getByRole('button', { name: 'Avaa kaikki' }).click();

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana')).toContainText('3');

    // Laajuus ryhmittäin: hyväksytysti arvioidut / perusteen vaatimus
    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa.1')).toHaveText('35 / 135 osp');

    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')
  })

  test('Paikallisen tutkinnon osan osa-alueen kuvaus näkyy ja on muokattavissa', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    const kuvaus = 'oo.0.suoritukset.0.osasuoritukset.5.properties.osasuoritukset.0.properties.paikallinenKuvaus'

    await page.getByRole('button', { name: 'Avaa kaikki' }).click()
    await expect(page.getByTestId(`${kuvaus}.value`)).toHaveText('Matematiikan opinnot')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(`${kuvaus}.edit.input`).fill('Matematiikan opinnot\nToinen rivi')
    await oppijaPageV2.tallenna()

    await expect(page.getByTestId(`${kuvaus}.value`)).toHaveText('Matematiikan opinnot\nToinen rivi')
  })

  test('Kokonaisuuden ja opintovalmiuksia tukevien opintojen kuvaukset näkyvät', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    const os = 'oo.0.suoritukset.0.osasuoritukset'

    await page.getByRole('button', { name: 'Avaa kaikki' }).click()
    await expect(page.getByTestId(`${os}.2.properties.osasuoritukset.0.properties.kuvaus.value`)).toHaveText('Hoitotarpeen määrittäminen')
    await expect(page.getByTestId(`${os}.7.properties.osasuoritukset.0.properties.kuvaus.value`)).toHaveText('Lukion maantiedon oppimäärä')
    await expect(page.getByTestId(`${os}.7.properties.osasuoritukset.1.properties.kuvaus.value`)).toHaveText('Englannin kurssi')
  })

  test('Valtakunnalliselle tutkinnon osalle voi lisätä kuvauksen', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    const kuvaus = 'oo.0.suoritukset.0.osasuoritukset.0.properties.kuvaus'

    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand').click()
    await expect(page.getByTestId(`${kuvaus}.value`)).toHaveCount(0)

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId(`${kuvaus}.edit.input`).fill('Videotuotannon kuvaus')
    await oppijaPageV2.tallenna()

    await expect(page.getByTestId(`${kuvaus}.value`)).toHaveText('Videotuotannon kuvaus')
  })

  test('Osasuoritusten tiedonsiirroista tulevat kentät näkyvät', async ({ page, fixtures, oppijaPage }) => {
    await fixtures.muokkaaOpiskeluoikeutta('1.2.246.562.24.00000000056', (oo) => {
      const osat = oo.suoritukset[0].osasuoritukset
      osat[0].vahvistus.paikkakunta = { koodiarvo: '091', koodistoUri: 'kunta' }
      osat[3].näyttö.arviointi.hylkäyksenPeruste = { fi: 'Näyttö jäi kesken' }
      osat[4].tutkinto = {
        tunniste: { koodiarvo: '351301', koodistoUri: 'koulutus' },
        perusteenDiaarinumero: 'OPH-2762-2017'
      }
    })
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    const os = 'oo.0.suoritukset.0.osasuoritukset'

    await page.getByRole('button', { name: 'Avaa kaikki' }).click()
    await expect(page.getByTestId(`${os}.0.properties.vahvistus`)).toHaveText('31.5.2016 Helsinki Reijo Reksi, rehtori')
    await expect(page.getByTestId(`${os}.4.properties.tutkintoNimi`)).toHaveText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId(`${os}.5.properties.osasuoritukset.2.properties.alkamispäivä`)).toHaveText('1.1.2014')
    await expect(page.getByText('Näyttö jäi kesken')).toBeVisible()
  })

  test('Arvioinnin kuvausta ei näytetä, mutta se säilyy tallennettaessa', async ({ page, fixtures, oppijaPage, oppijaPageV2 }) => {
    const oid = '1.2.246.562.24.00000000056'
    await fixtures.muokkaaOpiskeluoikeutta(oid, (oo) => {
      oo.suoritukset[0].osasuoritukset[0].arviointi[0].kuvaus = { fi: 'Arvioinnin kuvaus' }
    })
    await oppijaPage.goto(oid)

    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand').click()
    await expect(page.getByText('Arvioinnin kuvaus')).toHaveCount(0)

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    expect(await page.locator('textarea').evaluateAll((els) => els.map((e) => (e as HTMLTextAreaElement).value))).not.toContain('Arvioinnin kuvaus')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.kuvaus.edit.input').fill('Muutettu tutkinnon osan kuvaus')
    await oppijaPageV2.tallenna()

    const oppija = await (await page.request.get(`/koski/api/oppija/${oid}`)).json()
    expect(oppija.opiskeluoikeudet[0].suoritukset[0].osasuoritukset[0].arviointi[0].kuvaus).toEqual({ fi: 'Arvioinnin kuvaus' })
  })

  test('Mukautettujen arvosanojen rivit näkyvät katselutilassa vain kun arvo on tosi', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055')

    // keskiarvoSisältääMukautettujaArvosanoja = true -> rivi näkyy
    await expect(page.getByTestId('oo.0.suoritukset.0.keskiarvo-sisaltaa-mukautettuja.value')).toContainText('Kyllä')

    // korotettuKeskiarvoSisältääMukautettujaArvosanoja puuttuu -> koko rivi
    // piilotetaan nimiötä myöten, ei jätetä tyhjää riviä
    await expect(page.getByTestId('oo.0.suoritukset.0.korotus-sisaltaa-mukautettuja.value')).toHaveCount(0)
    await expect(page.getByText('Korotus sisältää mukautettuja arvosanoja')).toHaveCount(0)

    // Muokkaustilassa molemmat ovat edelleen asetettavissa
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await expect(page.getByTestId('oo.0.suoritukset.0.keskiarvo-sisaltaa-mukautettuja.edit')).toBeVisible()
    await expect(page.getByTestId('oo.0.suoritukset.0.korotus-sisaltaa-mukautettuja.edit')).toBeVisible()
  })

  test('Lisätietojen aikajakson päivämäärän voi kirjoittaa näppäin kerrallaan', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page
      .locator('.EditorContainer__lisatiedot .KeyValueRow')
      .filter({ hasText: /^Majoitus/ })
      .getByRole('button', { name: 'Lisää', exact: true })
      .click()

    // fill() asettaisi arvon kerralla eikä paljastaisi, jos kenttä
    // remountataan ja menettää fokuksen jokaisen näppäilyn jälkeen
    const alku = page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.aikajakso.alku.input')
    await alku.pressSequentially('2.3.2024')
    await expect(alku).toHaveValue('2.3.2024')
    await expect(alku).toBeFocused()
  })

  test('Yhteisen osan lisääminen arvosanalla onnistuu', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.input').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.options.101053.item').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.uusi-yhteinen-osan-osa-alue.input').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.uusi-yhteinen-osan-osa-alue.options.AI.item').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.osasuoritukset.0.properties.arviointi.lisää-arviointi').click()
    await oppijaPageV2.tallenna()
  })

  test('Keskiarvon poistaminen onnistuuu', async ({page, oppijaPage, oppijaPageV2}) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.suoritukset.0.painotettu-keskiarvo.edit.input').clear()
    await oppijaPageV2.tallenna()
  })

  test('Opintojakson poistaminen ja lisääminen rahoituksella onnistuu', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click();
    await oppijaPageV2.tallenna()

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.add').click();
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.modal.tila.edit').getByText('Valmistunut').click();
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.modal.rahoitus.edit').getByText('Valtionosuusrahoitteinen').click();
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.modal.submit').click();
    await oppijaPageV2.tallenna()
  })
})

test.describe('Osittaisen ammatillisen tutkinnon useasta tutkinnosta virkailijan käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Lisää näyttö ammatillisen osittaiselle suoritukselle', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand').click()
    await page.getByText('Lisää ammattiosaamisen näyttö').click()
    await oppijaPageV2.tallenna()
  })

  test('Näytön suorituspaikan puuttuva kuvaus merkitään punaisella', async ({ page, oppijaPage }) => {
    // Ei 056:ta, jolle edellinen testi tallentaa näytön
    await oppijaPage.goto('1.2.246.562.24.00000000055')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand').click()
    await page.getByText('Lisää ammattiosaamisen näyttö').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.suorituspaikka.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.suorituspaikka.options.1.item').click()

    // Tyhjän pakollisen kentän virheestä ei tule tekstiä, vain punainen kenttä
    const kuvaus = page.getByPlaceholder('Kuvaus')
    await expect(kuvaus).toHaveClass(/TextEdit__input--error/)
    await kuvaus.fill('Muksulan päiväkoti')
    await expect(kuvaus).not.toHaveClass(/TextEdit__input--error/)
    await kuvaus.clear()
    await expect(kuvaus).toHaveClass(/TextEdit__input--error/)
  })

  test('Renderöi osittaisen suorituksen useasta tutkinnosta tiedot virkailijan käyttöliittymässä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Stadin ammatti- ja aikuisopisto, ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')

    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText('Ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.suoritukset.0.suoritustapa')).toContainText('Reformin mukainen näyttö')
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')).toContainText('suomi')
    await expect(page.getByTestId('oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.value')).toContainText('Suorittaa toista osaamisalaa')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')).toContainText('Suoritus valmis')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')).toContainText('Vahvistus: 31.5.2024 Helsinki')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0')).toContainText('Reijo Reksi (rehtori)')

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2')

    await page.getByRole('button', { name: 'Avaa kaikki' }).click()

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.peruste.value')).toContainText('OPH-5410-2021')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana')).toContainText('2')
    // Sekä muulla (0) että yhteisellä (4) tutkinnon osalla on oma toimipiste
    for (const osa of [0, 4]) {
      await expect(page.getByTestId(`oo.0.suoritukset.0.osasuoritukset.${osa}.properties.toimipiste`)).toHaveText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    }

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.laajuus.value')).toContainText('4 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.arvosana.value')).toContainText('3')

    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('102 osp')
  })

  test('Sallii luoda uuden opiskeluoikeuden', async ({ page, uusiOppijaPage }) => {
    await uusiOppijaPage.goTo('110205A134A')

    // Oppijan tiedot
    await page.getByTestId('uusiOpiskeluoikeus.oppija.etunimet.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.oppija.etunimet.input').fill('Ossi')
    await page.getByTestId('uusiOpiskeluoikeus.oppija.sukunimi.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.oppija.sukunimi.input').fill('Osittainen-useasta-tutkinnosta')
    // Oppilaitos
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.input').fill('Stadin ammatti')
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.options.1.2.246.562.10.52251087186.item').click()
    // Opiskeluoikeuden ja suorituksen valinta
    await page.getByTestId('uusiOpiskeluoikeus.modal.opiskeluoikeus.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.opiskeluoikeus.options.opiskeluoikeudentyyppi_ammatillinenkoulutus.item').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustyyppi.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustyyppi.options.suorituksentyyppi_ammatillinentutkintoosittainenuseastatutkinnosta.item').click()

    await uusiOppijaPage.submitAndExpectSuccess()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Stadin ammatti- ja aikuisopisto, ammatillisen tutkinnon osia useasta tutkinnosta')
  })

  test('Sallii luoda uuden paikallisen tutkinnon osan', async ({ fixtures, page, oppijaPage, oppijaPageV2 }) => {
    await fixtures.reset()
    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Avaa modaali
    await page.getByTestId('oo.0.suoritukset.0.lisaa-paikallinen-osa').click()
    // Syötä paikallisen tutkinnon osan tutkinto ja nimi modaalissa
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').fill('auto')
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.options.351301_OPH-2762-2017.item').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.paikallisen-osan-nimi.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.paikallisen-osan-nimi.input').fill('Tuulilasin vaihto')
    await page.getByTestId('oo.0.suoritukset.0.modal.confirm').click()
    // Syötä laajuus ja arvosana
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').fill('2')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.options.Arviointiasteikko ammatillinen T1-K3.arviointiasteikkoammatillinent1k3_3.item').click()

    await oppijaPageV2.tallenna()

    // Syötetyt tiedot rendataan oikein
    await expect(page.getByTestId('suoritus.0.osasuoritus.7.nimi')).toContainText('Tuulilasin vaihto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.value')).toContainText('2 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.value')).toContainText('3')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Autoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('OPH-2762-2017')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('3')
  })

  test('Sallii luoda uuden yhteisen tutkinnon osan', async ({ fixtures, page, oppijaPage, oppijaPageV2 }) => {
    await fixtures.reset() // Resetoi, koska luotamme testId:issä osasuoritusten taulukkoindeksiin

    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Avaa modaali
    await page.getByTestId('oo.0.suoritukset.0.lisaa-yhteinen-tutkinnon-osa').click()
    // Syötä paikallisen tutkinnon osan tutkinto ja nimi modaalissa
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').fill('auto')
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.options.351301_39/011/2014.item').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.uusi-yhteinen-tutkinnonosa.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.uusi-yhteinen-tutkinnonosa.options.101056.item').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.confirm').click()
    // Syötä laajuus ja arvosana
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').fill('7')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.options.Arviointiasteikko ammatillinen T1-K3.arviointiasteikkoammatillinent1k3_3.item').click()

    await oppijaPageV2.tallenna()

    // Syötetyt tiedot rendataan oikein
    await expect(page.getByTestId('suoritus.0.osasuoritus.7.nimi')).toContainText('Sosiaalinen ja kulttuurinen osaaminen')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.value')).toContainText('7 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.value')).toContainText('3')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Autoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('39/011/2014')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('3')
  })

  test('Sallii luoda uuden ammatillisen tutkinnon osan', async ({ fixtures, page, oppijaPage, oppijaPageV2 }) => {
    await fixtures.reset() // Resetoi, koska luotamme testId:issä osasuoritusten taulukkoindeksiin

    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()

    // Avaa modaali
    await page.getByTestId('oo.0.suoritukset.0.lisaa-ammatillisen-tutkinnon-osa').click()
    // Syötä paikallisen tutkinnon osan tutkinto ja nimi modaalissa
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.input').fill('auto')
    await page.getByTestId('oo.0.suoritukset.0.modal.tutkinto.options.351301_39/011/2014.item').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.uusi-muu-tutkinnonosa.input').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.uusi-muu-tutkinnonosa.options.100021.item').click()
    await page.getByTestId('oo.0.suoritukset.0.modal.confirm').click()
    // Syötä laajuus ja arvosana
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.edit.input').fill('30')
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.input').click()
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.options.Arviointiasteikko ammatillinen T1-K3.arviointiasteikkoammatillinent1k3_2.item').click()

    await oppijaPageV2.tallenna()

    // Syötetyt tiedot rendataan oikein
    await expect(page.getByTestId('suoritus.0.osasuoritus.7.nimi')).toContainText('Ruiskumaalaustyöt')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.laajuus.value')).toContainText('30 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.arvosana.value')).toContainText('2')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Autoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('39/011/2014')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('2')
  })

  test('Tallennetut myöntäjät ovat samat kuin vanhassa käyttöliittymässä', async ({ fixtures, page, oppijaPage }) => {
    // Vanha käyttöliittymä tallentaa myöntäjät suorituksen toimipisteelle
    // (Lehtikuusentien toimipaikka, ei oppilaitos) koulutustoimijan rajaamana.
    const vanhanKäyttöliittymänMyöntäjät =
      '/koski/api/preferences/1.2.246.562.10.42456023292/myöntäjät?koulutustoimijaOid=1.2.246.562.10.346830761110'
    const vahvistus = 'oo.0.suoritukset.0.suorituksenVahvistus'
    const modal = page.locator('.Modal')

    await fixtures.reset()
    const tallennus = await page.request.put(vanhanKäyttöliittymänMyöntäjät, {
      data: {
        key: 'Ville Vanha',
        value: {
          nimi: 'Ville Vanha',
          titteli: { fi: 'rehtori' },
          organisaatio: { oid: '1.2.246.562.10.42456023292' }
        }
      }
    })
    expect(tallennus.ok()).toBeTruthy()

    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click()
    await page.getByTestId('oo.0.opiskeluoikeus.tila.edit.items.1.remove').click()
    await page.getByTestId(`${vahvistus}.edit.merkitseKeskeneräiseksi`).click()
    await page.getByTestId(`${vahvistus}.edit.merkitseValmiiksi`).click()
    await modal.locator('[data-testid$="organisaatiohenkilöt.edit.add.input"]').click()

    await expect(
      modal.locator('.Select__optionLabel').filter({ hasText: 'Ville Vanha (rehtori)' })
    ).toBeVisible()

    await modal.locator('.Select__optionLabel').filter({ hasText: 'Lisää henkilö' }).click()
    await modal.locator('[data-testid$="newHenkilö.nimi.input"]').fill('Veera Uusi')
    await modal.locator('[data-testid$="newHenkilö.titteli.input"]').fill('rehtori')
    await page.getByTestId(`${vahvistus}.edit.modal.submit`).click()

    await expect
      .poll(async () => {
        const response = await page.request.get(vanhanKäyttöliittymänMyöntäjät)
        return ((await response.json()) as Array<{ nimi: string }>).map((h) => h.nimi)
      })
      .toContain('Veera Uusi')
  })
})

test.describe('Osittaisen ammatillisen tutkinnon useasta tutkinnosta kansalaisen näkymä', () => {
  test.beforeAll(async ({ browser }, testInfo) => {
    // Pakotetaan fixture-reset käyttämään virkailijan istuntoa
    const virkailijaSessionPath = await getVirkailijaSession(
      testInfo,
      'kalle',
      'kalle'
    )
    const ctx = await browser.newContext({
      storageState: virkailijaSessionPath
    })
    const page = await ctx.newPage()
    await new KoskiFixtures(page).reset()
  })
  test.use({ storageState: kansalainen('011007A1489') })

  test('Renderöi osittaisen suorituksen useasta tutkinnosta tiedot kansalaiselle', async ({ page, kansalainenPage }) => {
    await kansalainenPage.goto()

    await expect(page.locator('.header__heading')).toContainText('Opintoni')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Ammatillisen tutkinnon osia useasta tutkinnosta')

    await page.locator('.OpiskeluoikeusTitle__expand').click()

    // Opiskeluoikeuden tiedot
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.tila')).toContainText('Valmistunut')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.0.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.tila')).toContainText('Läsnä')

    // Opiskeluoikeuden lisätiedot
    await expect(page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.alku')).toContainText('1.9.2022')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.loppu')).toContainText('1.9.2023')

    // Suorituksen tiedot
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText('Ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.suoritukset.0.suoritustapa')).toContainText('Reformin mukainen näyttö')
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')).toContainText('suomi')

    // Tutkinnon osat
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2')

    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.expand').click()

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.peruste.value')).toContainText('OPH-5410-2021')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana')).toContainText('2')

    // Osan osa-alueet:
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.laajuus.value')).toContainText('4 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.arvosana.value')).toContainText('3')

    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.expand').click()
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.properties.arviointi.0.arvosana')).toContainText('3')

    // Laajuudet
    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('102 osp')
  })

  test('Sivulla ei saavutettavuusvirheitä', async ({
    kansalainenPage,
    makeAxeBuilder
  }) => {
    await kansalainenPage.goto()
    const accessibilityScanResults = await makeAxeBuilder().analyze()
    expect(accessibilityScanResults.violations).toEqual([])
  })
})
