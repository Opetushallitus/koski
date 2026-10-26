import { expect, test } from './base'
import { KoskiUusiOppijaPage } from './pages/oppija/KoskiUusiOppijaPage'
import { virkailija } from './setup/auth'

/**
 * Lukion 2019 opiskeluoikeuden luonti. Luontidialogi on uutta
 * käyttöliittymää (KoskiUusiOppijaPage), mutta syntyvä suoritus avautuu
 * vanhaan käyttöliittymään (LukioOpiskeluoikeus) - lukion suoritusnäkymää
 * ei ole vielä migroitu.
 */

const aloituspäivä = new Date(2018, 0, 1)

const luoLukio = async (
  uusiOppijaPage: KoskiUusiOppijaPage,
  tiedot: { hetu: string; oppimäärä: string; peruste: string }
) => {
  await uusiOppijaPage.goTo(tiedot.hetu)
  await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
  await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
  await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
  await uusiOppijaPage.controls.oppimäärä.setByLabel(tiedot.oppimäärä)
  await uusiOppijaPage.controls.peruste.setByLabel(tiedot.peruste)
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
    await luoLukio(uusiOppijaPage, {
      hetu: '140981-334Y',
      oppimäärä: 'Lukion oppimäärä',
      peruste: 'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
    })

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

  test('Aikuisten lukion oppimäärä voidaan luoda', async ({
    uusiOppijaPage,
    oppijaPage
  }) => {
    await luoLukio(uusiOppijaPage, {
      hetu: '170491-517B',
      oppimäärä: 'Lukion oppimäärä',
      peruste:
        'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
    })

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('Lukion oppimäärä')
    await expect(opiskeluoikeus.kenttä('koulutusmoduuli')).toHaveText(
      'Lukion oppimäärä309902OPH-2267-2019'
    )
    await expect(opiskeluoikeus.kenttä('oppimäärä')).toHaveText(
      'Lukio suoritetaan aikuisten opetussuunnitelman mukaan'
    )
    await expect(opiskeluoikeus.kenttä('toimipiste')).toHaveText(
      'Jyväskylän normaalikoulu'
    )
    await expect(opiskeluoikeus.kenttä('suorituskieli')).toHaveText('suomi')
  })

  test('Nuorten lukion aineopinnot voidaan luoda', async ({
    uusiOppijaPage,
    oppijaPage
  }) => {
    await luoLukio(uusiOppijaPage, {
      hetu: '260613-0652',
      oppimäärä: 'Lukion aineopinnot',
      peruste: 'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
    })

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
    await expect(opiskeluoikeus.kenttä('koulutusmoduuli')).toHaveText(
      'Lukion aineopinnotOPH-2263-2019'
    )
    await expect(opiskeluoikeus.kenttä('toimipiste')).toHaveText(
      'Jyväskylän normaalikoulu'
    )
    await expect(opiskeluoikeus.kenttä('suorituskieli')).toHaveText('suomi')
  })

  test('Aikuisten lukion aineopinnot voidaan luoda', async ({
    uusiOppijaPage,
    oppijaPage
  }) => {
    await luoLukio(uusiOppijaPage, {
      hetu: '051009-409T',
      oppimäärä: 'Lukion aineopinnot',
      peruste:
        'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
    })

    const opiskeluoikeus = oppijaPage.getLukioOpiskeluoikeus('lukion aineopinnot')
    await expect(opiskeluoikeus.kenttä('koulutusmoduuli')).toHaveText(
      'Lukion aineopinnotOPH-2267-2019'
    )
    await expect(opiskeluoikeus.kenttä('toimipiste')).toHaveText(
      'Jyväskylän normaalikoulu'
    )
    await expect(opiskeluoikeus.kenttä('suorituskieli')).toHaveText('suomi')
  })

  // Regressiotesti: "Lukion oppiaineen oppimäärä" on 2015-OPS:n yksittäisen
  // oppiaineen suoritus, eri oppimäärä kuin "Lukion oppimäärä"/"Lukion
  // aineopinnot" - sen peruste-valikon ei pidä tarjota 2019-OPS:n
  // diaarinumeroita, vaikka ne näkyvät muiden lukio-oppimäärien valikoissa.
  test('Lukion oppiaineen oppimäärän peruste-valikko ei näytä 2019-OPS vaihtoehtoja', async ({
    uusiOppijaPage
  }) => {
    await uusiOppijaPage.goTo('230872-7258')
    await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
    await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
    await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
    await uusiOppijaPage.controls.oppimäärä.setByLabel(
      'Lukion oppiaineen oppimäärä'
    )

    const perusteet = await uusiOppijaPage.controls.peruste.options()
    expect(perusteet).not.toContain(
      'OPH-2267-2019 Aikuisten lukiokoulutuksen opetussuunnitelman perusteet 2019'
    )
    expect(perusteet).not.toContain(
      'OPH-2263-2019 Lukion opetussuunnitelman perusteet 2019'
    )
  })
})
