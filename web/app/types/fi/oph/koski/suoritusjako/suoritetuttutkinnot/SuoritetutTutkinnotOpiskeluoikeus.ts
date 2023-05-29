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
import {
  SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus,
  isSuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
} from './SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus'
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
  | SuoritetutTutkinnotDIAOpiskeluoikeus
  | SuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus
  | SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
  | SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus

export const isSuoritetutTutkinnotOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotOpiskeluoikeus =>
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotDIAOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotEuropeanSchoolOfHelsinkiOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotKorkeakoulunOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus(a)
