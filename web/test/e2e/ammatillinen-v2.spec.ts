import { expect, test } from './base'
import { virkailija } from './setup/auth'

test.describe('Osittaisen ammatillisen tutkinnon uusi käyttöliittymä', () => {
  test.use({ storageState: virkailija('kalle') })

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
  })
})
