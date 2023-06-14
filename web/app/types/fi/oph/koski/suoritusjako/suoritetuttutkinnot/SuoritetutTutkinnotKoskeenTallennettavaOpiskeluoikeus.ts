import {
  SuoritetutTutkinnotAmmatillinenOpiskeluoikeus,
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus
} from './SuoritetutTutkinnotAmmatillinenOpiskeluoikeus'
import {
  SuoritetutTutkinnotDIAOpiskeluoikeus,
  isSuoritetutTutkinnotDIAOpiskeluoikeus
} from './SuoritetutTutkinnotDIAOpiskeluoikeus'
import {
  SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus,
  isSuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
} from './SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus'

/**
 * SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus`
 */
export type SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus =
  | SuoritetutTutkinnotAmmatillinenOpiskeluoikeus
  | SuoritetutTutkinnotDIAOpiskeluoikeus
  | SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus

export const isSuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotKoskeenTallennettavaOpiskeluoikeus =>
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotDIAOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus(a)
