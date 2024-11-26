import { Jakolinkki } from './Jakolinkki'
import { SuoritusjakoHenkilö } from './SuoritusjakoHenkilo'
import { SuoritetutTutkinnotOpiskeluoikeus } from '../suoritetuttutkinnot/SuoritetutTutkinnotOpiskeluoikeus'

/**
 * SuoritetutTutkinnotOppijaJakolinkillä
 *
 * @see `fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä`
 */
export type SuoritetutTutkinnotOppijaJakolinkillä = {
  $class: 'fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä'
  jakolinkki?: Jakolinkki
  henkilö: SuoritusjakoHenkilö
  opiskeluoikeudet: Array<SuoritetutTutkinnotOpiskeluoikeus>
}

export const SuoritetutTutkinnotOppijaJakolinkillä = (o: {
  jakolinkki?: Jakolinkki
  henkilö: SuoritusjakoHenkilö
  opiskeluoikeudet?: Array<SuoritetutTutkinnotOpiskeluoikeus>
}): SuoritetutTutkinnotOppijaJakolinkillä => ({
  $class: 'fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä',
  opiskeluoikeudet: [],
  ...o
})

SuoritetutTutkinnotOppijaJakolinkillä.className =
  'fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä' as const

export const isSuoritetutTutkinnotOppijaJakolinkillä = (
  a: any
): a is SuoritetutTutkinnotOppijaJakolinkillä =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.SuoritetutTutkinnotOppijaJakolinkillä'
