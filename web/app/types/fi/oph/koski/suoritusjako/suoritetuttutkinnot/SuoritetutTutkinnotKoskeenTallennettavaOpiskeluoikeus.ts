import {
  SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus
} from './SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
import {
  SuoritetutTutkinnotDIAOpiskeluoikeus,
  isSuoritetutTutkinnotDIAOpiskeluoikeus
} from './SuoritetutTutkinnotDIAOpiskeluoikeus'
import {
  SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus,
  isSuoritetutTutkinnotEBTutkinnonOpiskeluoikeus
} from './SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus'

/**
 * SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus`
 */
export type SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus =
  | SuoritetutTutkinnotAmmatillinenOpiskeluoikeus
  | SuoritetutTutkinnotDIAOpiskeluoikeus
  | SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus

export const isSuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus =>
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotDIAOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotEBTutkinnonOpiskeluoikeus(a)
