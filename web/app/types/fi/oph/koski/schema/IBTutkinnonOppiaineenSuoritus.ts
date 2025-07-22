import { IBDPCoreSuoritus, isIBDPCoreSuoritus } from './IBDPCoreSuoritus'
import {
  IBOppiaineenSuoritus,
  isIBOppiaineenSuoritus
} from './IBOppiaineenSuoritus'

/**
 * IBTutkinnonOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.IBTutkinnonOppiaineenSuoritus`
 */
export type IBTutkinnonOppiaineenSuoritus =
  | IBDPCoreSuoritus
  | IBOppiaineenSuoritus

export const isIBTutkinnonOppiaineenSuoritus = (
  a: any
): a is IBTutkinnonOppiaineenSuoritus =>
  isIBDPCoreSuoritus(a) || isIBOppiaineenSuoritus(a)
