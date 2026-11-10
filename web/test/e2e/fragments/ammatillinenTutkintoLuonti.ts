import { Page } from '@playwright/test'
import { KoskiUusiOppijaPage } from '../pages/oppija/KoskiUusiOppijaPage'

export const STADIN_AMMATTIOPISTO = {
  haku: 'Stadin',
  oid: '1.2.246.562.10.52251087186'
}

export const AUTOALAN_PERUSTUTKINTO = {
  haku: 'auto',
  avain: '351301_39/011/2014',
  suoritustapa: 'ops'
}

export type AmmatillinenTutkintoLomake = {
  oppilaitos?: { haku: string; oid: string }
  tutkinto?: { haku: string; avain: string; suoritustapa?: string }
  suoritustyyppi?: string
  suorituskieli?: string
  aloituspäivä?: string
  rahoitus?: string
  maksuton?: 'eiOvlPiirissä' | 'maksuton' | 'maksullinen'
}

/**
 * Täyttää uuden opiskeluoikeuden lomakkeen ammatillisen tutkinnon tiedoilla.
 * Kentät tulevat näkyviin vasta edellisten valintojen jälkeen.
 */
export const täytäAmmatillinenTutkinto = async (
  page: Page,
  uusiOppijaPage: KoskiUusiOppijaPage,
  {
    oppilaitos = STADIN_AMMATTIOPISTO,
    tutkinto = AUTOALAN_PERUSTUTKINTO,
    suoritustyyppi = 'ammatillinentutkinto',
    suorituskieli,
    aloituspäivä = '1.1.2018',
    rahoitus,
    maksuton
  }: AmmatillinenTutkintoLomake = {}
) => {
  const kenttä = (id: string) =>
    page.getByTestId(`uusiOpiskeluoikeus.modal.${id}`)
  const { controls } = uusiOppijaPage

  await kenttä('oppilaitos.input').fill(oppilaitos.haku)
  await kenttä(`oppilaitos.options.${oppilaitos.oid}.item`).click()
  await controls.opiskeluoikeus.set(
    'opiskeluoikeudentyyppi_ammatillinenkoulutus'
  )
  await controls.suoritustyyppi.set(`suorituksentyyppi_${suoritustyyppi}`)

  await kenttä('tutkinto.input').fill(tutkinto.haku)
  await kenttä(`tutkinto.options.${tutkinto.avain}.item`).click()
  if (tutkinto.suoritustapa) {
    await kenttä('suoritustapa.input').click()
    await kenttä(
      `suoritustapa.options.ammatillisentutkinnonsuoritustapa_${tutkinto.suoritustapa}.item`
    ).click()
  }

  if (suorituskieli) await controls.suorituskieli.set(suorituskieli)
  await controls.aloituspäivä.set(aloituspäivä)
  if (rahoitus) await controls.opintojenRahoitus.setByLabel(rahoitus)
  if (maksuton) await controls.maksuton.set(maksuton)
}

/**
 * Luo henkilöpalvelusta löytyvälle henkilölle opiskeluoikeuden lomakkeen kautta.
 */
export const luoAmmatillinenTutkinto = async (
  page: Page,
  uusiOppijaPage: KoskiUusiOppijaPage,
  hetu: string,
  lomake?: AmmatillinenTutkintoLomake
) => {
  await uusiOppijaPage.goTo(hetu)
  await täytäAmmatillinenTutkinto(page, uusiOppijaPage, lomake)
  await uusiOppijaPage.submitAndExpectSuccess()
  await page.getByTestId('oo.0.opiskeluoikeus.edit').waitFor()
}
