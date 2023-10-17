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
  | SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus
  | SuoritetutTutkinnotKorkeakoulunOpiskeluoikeus
  | SuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus

export const isSuoritetutTutkinnotOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotOpiskeluoikeus =>
  isSuoritetutTutkinnotAmmatillinenOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotDIAOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotEBTutkinnonOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotKorkeakoulunOpiskeluoikeus(a) ||
  isSuoritetutTutkinnotYlioppilastutkinnonOpiskeluoikeus(a)
