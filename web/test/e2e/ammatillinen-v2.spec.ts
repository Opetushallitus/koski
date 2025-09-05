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
    await expect(page.getByTestId('oo.0.suoritukset.0.0.osaamisala')).toContainText('Ajoneuvotekniikan osaamisala');
    await expect(page.getByTestId('oo.0.suoritukset.0.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.laajuus.value')).toContainText('11 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.arvosana.value')).toContainText('2');

    await page.getByRole('button', { name: 'Avaa kaikki' }).click();

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.tutkintoNimi')).toContainText('Ajoneuvoalan perustutkinto');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.peruste.value')).toContainText('OPH-5410-2021');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.arviointi.0.arvosana')).toContainText('2');

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.laajuus.value')).toContainText('4 osp');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.arvosana.value')).toContainText('3');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.4.properties.osasuoritukset.0.arvosana.value')).toContainText('3');

    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('187 osp');

  })
})
