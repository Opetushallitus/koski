import {
  IBDPCoreOppiaineLanguage,
  isIBDPCoreOppiaineLanguage
} from './IBDPCoreOppiaineLanguage'
import {
  IBDPCoreOppiaineMuu,
  isIBDPCoreOppiaineMuu
} from './IBDPCoreOppiaineMuu'

/**
 * IBDPCoreAineRyhmäOppiaine
 *
 * @see `fi.oph.koski.schema.IBDPCoreAineRyhmäOppiaine`
 */
export type IBDPCoreAineRyhmäOppiaine =
  | IBDPCoreOppiaineLanguage
  | IBDPCoreOppiaineMuu

export const isIBDPCoreAineRyhmäOppiaine = (
  a: any
): a is IBDPCoreAineRyhmäOppiaine =>
  isIBDPCoreOppiaineLanguage(a) || isIBDPCoreOppiaineMuu(a)
