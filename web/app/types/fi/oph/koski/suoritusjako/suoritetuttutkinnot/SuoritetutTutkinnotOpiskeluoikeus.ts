import {
  SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus
} from './SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
import {
  SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus,
  isSuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus
} from './SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus'

/**
 * SuoritetutTutkinnotOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotOpiskeluoikeus`
 */
export type SuoritetutTutkinnotOpiskeluoikeus =
  | SuoritetutTutkinnotAmmatillinenOpiskeluoikeus
  | SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus

export const isSuoritetutTutkinnotOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotOpiskeluoikeus =>
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus(a)
