import { expect, test } from './base'
import { virkailija } from './setup/auth'

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

test.describe('Osittaisen ammatillisen tutkinnon useasta tutkinnosta käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeAll(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Renderöi osittaisen suorituksen useasta tutkinnosta tiedot', async ({ page, oppijaPage }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000182?ammatillinen-v2=true')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Stadin ammatti- ja aikuisopisto, ammatillisen tutkinnon osa/osia useasta tutkinnosta')
    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')

    await expect(page.getByTestId('oo.0.suoritukset.0.koulutus')).toContainText('Ammatillisen tutkinnon osa/osia useasta tutkinnosta');
    await expect(page.getByTestId('oo.0.suoritukset.0.suoritustapa')).toContainText('Reformin mukainen näyttö');
    await expect(page.getByTestId('oo.0.suoritukset.0.0.tutkintonimike.value')).toContainText('Automekaanikko');
    await expect(page.getByTestId('oo.0.suoritukset.0.0.osaamisala')).toContainText('Ajoneuvotekniikan osaamisala');
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituskieli.value')).toContainText('suomi');
    await expect(page.getByTestId('oo.0.suoritukset.0.0.järjestämismuoto')).toContainText('Koulutuksen järjestäminen oppilaitosmuotoisena');
    await expect(page.getByTestId('oo.0.suoritukset.0.todistuksellaNäkyvätLisätiedot.value')).toContainText('Suorittaa toista osaamisalaa');

    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.status')).toContainText('Suoritus valmis');
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.details')).toContainText('Vahvistus: 31.5.2024 Stadin ammatti- ja aikuisopisto');
    await expect(page.getByTestId('oo.0.suoritukset.0.suorituksenVahvistus.value.henkilö.0')).toContainText('Reijo Reksi (rehtori)');

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2');

    await page.getByRole('button', { name: 'Avaa kaikki' }).click();

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.peruste.value')).toContainText('OPH-5410-2021');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana')).toContainText('2');

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.laajuus.value')).toContainText('4 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.arvosana.value')).toContainText('3');

    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('187 osp');
  })

  test('Sallii luoda uuden opiskeluoikeuden', async ({ page, uusiOppijaPage }) => {
    await uusiOppijaPage.goTo('110205A134A')

    // Oppijan tiedot
    await page.getByTestId('uusiOpiskeluoikeus.oppija.etunimet.input').click();
    await page.getByTestId('uusiOpiskeluoikeus.oppija.etunimet.input').fill('Ossi')
    await page.getByTestId('uusiOpiskeluoikeus.oppija.sukunimi.input').click();
    await page.getByTestId('uusiOpiskeluoikeus.oppija.sukunimi.input').fill('Osittainen-useasta-tutkinnosta')
    // Oppilaitos
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.input').click();
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.input').fill('Stadin ammatti')
    await page.getByTestId('uusiOpiskeluoikeus.modal.oppilaitos.options.1.2.246.562.10.52251087186.item').click()
    // Opiskeluoikeuden ja suorituksen valinta
    await page.getByTestId('uusiOpiskeluoikeus.modal.opiskeluoikeus.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.opiskeluoikeus.options.opiskeluoikeudentyyppi_ammatillinenkoulutus.item').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustyyppi.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustyyppi.options.suorituksentyyppi_ammatillinentutkintoosittainenuseastatutkinnosta.item').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustapa.input').click()
    await page.getByTestId('uusiOpiskeluoikeus.modal.suoritustapa.options.ammatillisentutkinnonsuoritustapa_reformi.item').click()

    await uusiOppijaPage.submitAndExpectSuccess()
    await expect(page.getByTestId('oo.0.opiskeluoikeus.nimi')).toContainText('Stadin ammatti- ja aikuisopisto, ammatillisen tutkinnon osa/osia useasta tutkinnosta')
  })
})
