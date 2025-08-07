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
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.arvosana.value')).toContainText('kiitettävä');

    await page.getByRole('button', { name: 'Avaa kaikki' }).click();

    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.organisaatio.value')).toContainText('Stadin ammatti- ja aikuisopisto, Lehtikuusentien toimipaikka');
    await expect(page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.arviointi.0.arvosana')).toContainText('kiitettävä');

    await expect(page.getByTestId('oo.0.suoritukset.0.yhteensa')).toContainText('35 osp');

    await expect(page.getByTestId('oo.0.opiskeluoikeus.tila.value.items.1.rahoitus')).toContainText('Valtionosuusrahoitteinen koulutus')
  })

  test('Yhteisen osan lisääminen arvosanalla onnistuu', async ({ page, oppijaPage, oppijaPageV2 }) => {
    await oppijaPage.goto('1.2.246.562.24.00000000055?ammatillinen-v2=true')

    await page.getByTestId('oo.0.opiskeluoikeus.edit').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.input').click();
    await page.getByTestId('oo.0.suoritukset.0.uusi-yhteinen-tutkinnonosa.options.101053.item').click();
    await page.locator('section').filter({ hasText: '⧖Viestintä- ja' }).getByTestId('oo.0.suoritukset.0.osasuoritukset.0.expand').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.uusi-yhteinen-osan-osa-alue.input').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.uusi-yhteinen-osan-osa-alue.options.AI.item').click();
    await page.getByTestId('oo.0.suoritukset.0.osasuoritukset.0.properties.osasuoritukset.0.properties.arviointi.lisää-arviointi').click()
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
