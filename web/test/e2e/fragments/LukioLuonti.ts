import { KoskiUusiOppijaPage } from '../pages/oppija/KoskiUusiOppijaPage'

const aloituspäivä = new Date(2018, 0, 1)

export const luoLukio = async (
  uusiOppijaPage: KoskiUusiOppijaPage,
  tiedot: { hetu: string; oppimäärä: string; peruste: string }
) => {
  await uusiOppijaPage.goTo(tiedot.hetu)
  await uusiOppijaPage.fill({ etunimet: 'Tero', sukunimi: 'Tyhjä' })
  await uusiOppijaPage.fill({ oppilaitos: 'Jyväskylän normaalikoulu' })
  await uusiOppijaPage.controls.opiskeluoikeus.setByLabel('Lukiokoulutus')
  await uusiOppijaPage.controls.oppimäärä.setByLabel(tiedot.oppimäärä)
  await uusiOppijaPage.controls.peruste.setByLabel(tiedot.peruste)
  await uusiOppijaPage.fill({
    aloituspäivä,
    rahoitus: 'Valtionosuusrahoitteinen koulutus'
  })
  await uusiOppijaPage.submitAndExpectSuccess()
}
