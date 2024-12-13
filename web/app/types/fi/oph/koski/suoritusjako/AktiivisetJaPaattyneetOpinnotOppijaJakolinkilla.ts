import { Jakolinkki } from './Jakolinkki'
import { SuoritusjakoHenkilö } from './SuoritusjakoHenkilo'
import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeus } from '../aktiivisetjapaattyneetopinnot/AktiivisetJaPaattyneetOpinnotOpiskeluoikeus'

/**
 * AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä
 *
 * @see `fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä`
 */
export type AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä = {
  $class: 'fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä'
  jakolinkki?: Jakolinkki
  henkilö: SuoritusjakoHenkilö
  opiskeluoikeudet: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeus>
}

export const AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä = (o: {
  jakolinkki?: Jakolinkki
  henkilö: SuoritusjakoHenkilö
  opiskeluoikeudet?: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeus>
}): AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä => ({
  $class:
    'fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä',
  opiskeluoikeudet: [],
  ...o
})

AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä.className =
  'fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä' as const

export const isAktiivisetJaPäättyneetOpinnotOppijaJakolinkillä = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.AktiivisetJaPäättyneetOpinnotOppijaJakolinkillä'
