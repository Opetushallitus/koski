import {
  MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus,
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
} from './MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus'
import {
  TutkinnonOsaaPienemmänKokonaisuudenSuoritus,
  isTutkinnonOsaaPienemmänKokonaisuudenSuoritus
} from './TutkinnonOsaaPienemmanKokonaisuudenSuoritus'
import {
  YhteisenTutkinnonOsanOsaAlueenSuoritus,
  isYhteisenTutkinnonOsanOsaAlueenSuoritus
} from './YhteisenTutkinnonOsanOsaAlueenSuoritus'

/**
 * MuuAmmatillinenOsasuoritus
 *
 * @see `fi.oph.koski.schema.MuuAmmatillinenOsasuoritus`
 */
export type MuuAmmatillinenOsasuoritus =
  | MuunAmmatillisenKoulutuksenOsasuorituksenSuoritus
  | TutkinnonOsaaPienemmänKokonaisuudenSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isMuuAmmatillinenOsasuoritus = (
  a: any
): a is MuuAmmatillinenOsasuoritus =>
  isMuunAmmatillisenKoulutuksenOsasuorituksenSuoritus(a) ||
  isTutkinnonOsaaPienemmänKokonaisuudenSuoritus(a) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
