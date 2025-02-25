import { IBDBCoreSuoritus, isIBDBCoreSuoritus } from './IBDBCoreSuoritus'
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
  | IBDBCoreSuoritus
  | IBOppiaineenSuoritus

export const isIBTutkinnonOppiaineenSuoritus = (
  a: any
): a is IBTutkinnonOppiaineenSuoritus =>
  isIBDBCoreSuoritus(a) || isIBOppiaineenSuoritus(a)
