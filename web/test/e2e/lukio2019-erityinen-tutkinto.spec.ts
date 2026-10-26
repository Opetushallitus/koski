import { expect, test } from './base'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija } from './setup/auth'

test.describe('Lukiokoulutus 2019: erityinen tutkinto', () => {
  test.use({ storageState: virkailija('pää') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Erityisenä tutkintona suoritetun oppiaineen alaviite ja laajuus näytetään oikein', async ({
    virkailijaPage,
    oppijaHaku,
    oppijaPage
  }) => {
    await virkailijaPage.goto()
    await (await oppijaHaku.search('010705A6119')).clickOnFirst()
    await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
    const bi = opiskeluoikeus.oppiaineRivi('BI')
    await expect(bi.locator('.title .footnote-hint')).toHaveAttribute(
      'title',
      'Erityisenä tutkintona suoritettu oppiaine'
    )
    await expect(bi.locator('.laajuus:not(.arvioitu)')).toHaveText('6')
    await expect(bi.locator('.laajuus.arvioitu')).toHaveText('6')

    const ai = opiskeluoikeus.oppiaineRivi('ÄI')
    await expect(ai.locator('.title .footnote-hint')).toHaveCount(0)

    await expect(
      opiskeluoikeus.container.getByText(
        '** = erityisenä tutkintona suoritettu oppiaine'
      )
    ).toBeVisible()

    await expect(opiskeluoikeus.container.locator('.kurssit-yhteensä')).toHaveText(
      'Arvioitujen osasuoritusten laajuus yhteensä: 167,5Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 164,0'
    )
  })

  test('Erityinen tutkinto voidaan merkitä oppiaineen tasolla', async ({
    virkailijaPage,
    oppijaHaku,
    oppijaPage
  }) => {
    await virkailijaPage.goto()
    await (await oppijaHaku.search('010705A6119')).clickOnFirst()
    await oppijaPage.selectOpiskeluoikeus('lukiokoulutus')
    await oppijaPage.avaaMuokkausnäkymä()

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
    await opiskeluoikeus.lisääOppiaine('Psykologia')
    const ps = opiskeluoikeus.oppiaineRivi('PS')
    await opiskeluoikeus.erityinenTutkintoCheckbox(ps).check()
    await opiskeluoikeus.laajuusInput(ps).fill('6')
    await opiskeluoikeus.arvosanaDropdown(ps).selectOptionByClick('9')

    await oppijaPage.tallenna()

    const psAgain = opiskeluoikeus.oppiaineRivi('PS')
    await expect(psAgain.locator('.title .footnote-hint')).toHaveAttribute(
      'title',
      'Erityisenä tutkintona suoritettu oppiaine'
    )
    await expect(psAgain.locator('.laajuus:not(.arvioitu)')).toHaveText('6')
    await expect(psAgain.locator('.laajuus.arvioitu')).toHaveText('6')

    // Lipun asettaminen yhdelle oppiaineelle ei vaikuta muihin.
    const ai = opiskeluoikeus.oppiaineRivi('ÄI')
    await expect(ai.locator('.title .footnote-hint')).toHaveCount(0)
  })

  test.describe('suoritustason lippu', () => {
    test.use({ storageState: virkailija('kalle') })

    const luoAikuistenLukio = async (uusiOppijaPage: KoskiUusiOppijaPage) => {
      await uusiOppijaPage.goTo('130307A208A')
      await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
      await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
      await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
      await uusiOppijaPage.controls.oppimäärä.setByLabel('Lukion oppimäärä')
      await uusiOppijaPage.controls.peruste.setByLabel(
        'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
      )
      await uusiOppijaPage.fill({
        aloituspäivä: new Date(2018, 0, 1),
        rahoitus: 'Valtionosuusrahoitteinen koulutus'
      })
      await uusiOppijaPage.submitAndExpectSuccess()
    }

    test('Erityinen tutkinto voidaan merkitä suorituksen tasolla', async ({
      uusiOppijaPage,
      oppijaPage
    }) => {
      // Suoritustason lippu vaikuttaa kaikkiin oppiaineisiin (ks. muistio
      // LukioOpiskeluoikeus.erityinenTutkintoCheckbox) - käytetään siksi
      // juuri luotua, tyhjää aikuisten oppimäärää, ei valmiiksi täytettyä
      // kiinteää fixtuuria.
      await luoAikuistenLukio(uusiOppijaPage)

      await oppijaPage.avaaMuokkausnäkymä()
      const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
      await opiskeluoikeus.erityinenTutkintoCheckbox().check()

      await opiskeluoikeus.lisääOppiaine('Biologia')
      const bi = opiskeluoikeus.oppiaineRivi('BI')
      await opiskeluoikeus.laajuusInput(bi).fill('12')
      await opiskeluoikeus.arvosanaDropdown(bi).selectOptionByClick('9')

      await oppijaPage.tallenna()

      const biAgain = opiskeluoikeus.oppiaineRivi('BI')
      await expect(biAgain.locator('.title .footnote-hint')).toHaveAttribute(
        'title',
        'Erityisenä tutkintona suoritettu oppiaine'
      )
      await expect(biAgain.locator('.laajuus:not(.arvioitu)')).toHaveText('12')
      await expect(biAgain.locator('.laajuus.arvioitu')).toHaveText('12')
      await expect(opiskeluoikeus.container.locator('.kurssit-yhteensä')).toHaveText(
        'Arvioitujen osasuoritusten laajuus yhteensä: 12,0Hyväksytysti arvioitujen osasuoritusten laajuus yhteensä: 12,0'
      )
    })
  })
})
