import { OidHenkilö } from '../schema/OidHenkilo'
import { OpiskeluoikeusVersio } from './OpiskeluoikeusVersio'

/**
 * HenkilönOpiskeluoikeusVersiot
 *
 * @see `fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot`
 */
export type HenkilönOpiskeluoikeusVersiot = {
  $class: 'fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot'
  henkilö: OidHenkilö
  opiskeluoikeudet: Array<OpiskeluoikeusVersio>
}

export const HenkilönOpiskeluoikeusVersiot = (o: {
  henkilö: OidHenkilö
  opiskeluoikeudet?: Array<OpiskeluoikeusVersio>
}): HenkilönOpiskeluoikeusVersiot => ({
  $class: 'fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot',
  opiskeluoikeudet: [],
  ...o
})

export const isHenkilönOpiskeluoikeusVersiot = (
  a: any
): a is HenkilönOpiskeluoikeusVersiot =>
  a?.$class === 'fi.oph.koski.oppija.HenkilönOpiskeluoikeusVersiot'
