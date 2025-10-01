import { expect, test } from './base'
import { getVirkailijaSession, kansalainen, virkailija } from './setup/auth'
import { KoskiFixtures } from './fixtures/KoskiFixtures'

test.describe('Osittaisen ammatillisen tutkinnon uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Renderöi osittaisen tutkinnon tiedot', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055?ammatillinen-v2=true')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText(
      'Stadin ammatti- ja aikuisopisto, luonto- ja ympäristöalan perustutkinto, osittainen'
    )
    await expect(page.getByTestId('oo.0.opiskeluoikeus.edit')).toContainText('Muokkaa');


    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.laajuus.value')).toContainText('35 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')).toContainText('3');

    await page.getByRole('button', { name: 'Avaa kaikki' }).click();

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana')).toContainText('3');

    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('35 osp');

    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')
  })

  test('Yhteisen osan lisääminen arvosanalla onnistuu', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055?ammatillinen-v2=true')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.input').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.options.101053.item').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.uusi-yhteinen-osan-osa-alue.input').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.uusi-yhteinen-osan-osa-alue.options.AI.item').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.1.properties.osasuoritukset.0.properties.arviointi.lisää-arviointi').click()
    await oppijaPageV2.tallenna()
  })

  test('Keskiarvon poistaminen onnistuuu', async ({page, oppijaPage, oppijaPageV2}) => {
    await oppijaPage.goto('1.2.246.562.24.00000000056?ammatillinen-v2=true')
    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.suoritukset.0.painotettu-keskiarvo.edit.input').clear()
    await oppijaPageV2.tallenna()
  })

  test('Opintojakson poistaminen ja lisääminen rahoituksella onnistuu', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055?ammatillinen-v2=true')

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

  test('Renderöi osittaisen suorituksen useasta tutkinnosta tiedot virkailijan käyttöliittymässä', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000182')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Stadin ammatti- ja aikuisopisto, ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')

    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText('Ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.suoritukset.0.suoritustapa')).toContainText('Reformin mukainen näyttö')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.tutkintonimike.value')).toContainText('Automekaanikko')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.osaamisala')).toContainText('Ajoneuvotekniikan osaamisala')
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')).toContainText('suomi')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.järjestämismuoto')).toContainText('Koulutuksen järjestäminen oppilaitosmuotoisena')
    await expect(page.getByTestId('oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.value')).toContainText('Suorittaa toista osaamisalaa')

    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')).toContainText('Suoritus valmis')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')).toContainText('Vahvistus: 31.5.2024 Stadin ammatti- ja aikuisopisto')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0')).toContainText('Reijo Reksi (rehtori)')

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2')

    await page.getByRole('button', { name: 'Avaa kaikki' }).click()

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.peruste.value')).toContainText('OPH-5410-2021')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana')).toContainText('2')

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
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('OPH-2762-2017')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.organisaatio.value')).toContainText('–')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('3')
  })

  test('Sallii luoda uuden yhteisen tutkinnon osan', async ({ fixtures, page, oppijaPage, oppijaPageV2 }) => {
    fixtures.reset() // Resetoi, koska luotamme testId:issä osasuoritusten taulukkoindeksiin

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
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('39/011/2014')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.organisaatio.value')).toContainText('–')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('3')
  })

  test('Sallii luoda uuden ammatillisen tutkinnon osan', async ({ fixtures, page, oppijaPage, oppijaPageV2 }) => {
    fixtures.reset() // Resetoi, koska luotamme testId:issä osasuoritusten taulukkoindeksiin

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
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.peruste.value')).toContainText('39/011/2014')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.organisaatio.value')).toContainText('–')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.7.properties.arviointi.0.arvosana')).toContainText('2')
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
    await page.getByTestId('oo.0.opiskeluoikeus.lisätiedotButton').click()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.alku')).toContainText('1.9.2022')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.lisätiedot.0.loppu')).toContainText('1.9.2023')

    // Suorituksen tiedot
    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText('Ammatillisen tutkinnon osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.suoritukset.0.suoritustapa')).toContainText('Reformin mukainen näyttö')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.tutkintonimike.value')).toContainText('Automekaanikko')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.osaamisala')).toContainText('Ajoneuvotekniikan osaamisala')
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')).toContainText('suomi')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.alku')).toContainText('1.9.2022')
    await expect(page.getByTestId('oo.0.suoritukset.0.0.järjestämismuoto')).toContainText('Koulutuksen järjestäminen oppilaitosmuotoisena')
    await expect(page.getByTestId('oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.value')).toContainText('Suorittaa toista osaamisalaa')

    // Tutkinnon osat
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp')
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2')

    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.expand').click()

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka')
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
