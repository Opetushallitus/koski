import {
  ValmaKoulutuksenOsanSuoritus,
  isValmaKoulutuksenOsanSuoritus
} from './ValmaKoulutuksenOsanSuoritus'
import {
  YhteisenTutkinnonOsanOsaAlueenSuoritus,
  isYhteisenTutkinnonOsanOsaAlueenSuoritus
} from './YhteisenTutkinnonOsanOsaAlueenSuoritus'

/**
 * ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus
 *
 * @see `fi.oph.koski.schema.ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus`
 */
export type ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus =
  | ValmaKoulutuksenOsanSuoritus
  | YhteisenTutkinnonOsanOsaAlueenSuoritus

export const isValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus = (
  a: any
): a is ValmaKoulutuksenOsanTaiOsanOsaAlueenSuoritus =>
  isValmaKoulutuksenOsanSuoritus(a) ||
  isYhteisenTutkinnonOsanOsaAlueenSuoritus(a)
