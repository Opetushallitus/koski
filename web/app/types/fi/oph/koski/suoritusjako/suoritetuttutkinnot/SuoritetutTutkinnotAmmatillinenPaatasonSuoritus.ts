import {
  SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus,
  isSuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
} from './SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus'
import {
  SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus,
  isSuoritetutTutkinnotAmmatillisenTutkinnonSuoritus
} from './SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus'
import {
  SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus,
  isSuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus
} from './SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus'

/**
 * SuoritetutTutkinnotAmmatillinenPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillinenPäätasonSuoritus`
 */
export type SuoritetutTutkinnotAmmatillinenPäätasonSuoritus =
  | SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
  | SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus
  | SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus

export const isSuoritetutTutkinnotAmmatillinenPäätasonSuoritus = (
  a: any
): a is SuoritetutTutkinnotAmmatillinenPäätasonSuoritus =>
  isSuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isSuoritetutTutkinnotAmmatillisenTutkinnonSuoritus(a) ||
  isSuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus(a)
