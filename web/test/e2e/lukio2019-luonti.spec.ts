import { expect, test } from './base'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija } from './setup/auth'

/**
 * Lukion 2019 opiskeluoikeuden luonti. Luontidialogi on uutta
 * käyttöliittymää (KoskiUusiOppijaPage), mutta syntyvä suoritus avautuu
 * vanhaan käyttöliittymään (LukioOpiskeluoikeus) - lukion suoritusnäkymää
 * ei ole vielä migroitu.
 */

const hetu = '140981-334Y'
const aloituspäivä = new Date(2018, 0, 1)

const luoNuortenLukio = async (uusiOppijaPage: KoskiUusiOppijaPage) => {
  await uusiOppijaPage.goTo(hetu)
  await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
  await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
  await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
  await uusiOppijaPage.controls.oppimäärä.setByLabel('Lukion oppimäärä')
  await uusiOppijaPage.controls.peruste.setByLabel(
    'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
  )
  await uusiOppijaPage.fill({ aloituspäivä, rahoitus: 'Valtionosuusrahoitteinen koulutus' })
  await uusiOppijaPage.submitAndExpectSuccess()
}

test.describe('Lukiokoulutus 2019: opiskeluoikeuden luonti', () => {
  test.use({ storageState: virkailija('kalle') })

  test.beforeEach(async ({ fixtures }) => {
    await fixtures.reset()
  })

  test('Nuorten lukion oppimäärä voidaan luoda, oppiaineita ei esitäytetä', async ({
    uusiOppijaPage,
    oppijaPage
  }) => {
    await luoNuortenLukio(uusiOppijaPage)

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
    await expect(opiskeluoikeus.kenttä('koulutusmoduuli')).toHaveText(
      'Lukion oppimäärä309902OPH-2263-2019'
    )
    await expect(opiskeluoikeus.kenttä('oppimäärä')).toHaveText(
      'Lukio suoritetaan nuorten opetussuunnitelman mukaan'
    )
    await expect(opiskeluoikeus.kenttä('toimipiste')).toHaveText(
      'Jyväskylän normaalikoulu'
    )
    await expect(opiskeluoikeus.kenttä('suorituskieli')).toHaveText('suomi')

    await expect(opiskeluoikeus.oppiaineet.locator('.oppiaine-rivi')).toHaveCount(0)
  })
})
