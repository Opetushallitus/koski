import { Henkilo } from './Henkilo'
import { SuoritetutTutkinnotOpiskeluoikeus } from './SuoritetutTutkinnotOpiskeluoikeus'

/**
 * SuoritetutTutkinnotOppija
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija`
 */
export type SuoritetutTutkinnotOppija = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija'
  henkilö: Henkilo
  opiskeluoikeudet: Array<SuoritetutTutkinnotOpiskeluoikeus>
}

export const SuoritetutTutkinnotOppija = (o: {
  henkilö: Henkilo
  opiskeluoikeudet?: Array<SuoritetutTutkinnotOpiskeluoikeus>
}): SuoritetutTutkinnotOppija => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija',
  opiskeluoikeudet: [],
  ...o
})

SuoritetutTutkinnotOppija.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija' as const

export const isSuoritetutTutkinnotOppija = (
  a: any
): a is SuoritetutTutkinnotOppija =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOppija'
