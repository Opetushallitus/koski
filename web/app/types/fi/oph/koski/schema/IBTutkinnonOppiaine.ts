import { IBDBCoreSuoritus, isIBDBCoreSuoritus } from './IBDBCoreSuoritus'
import {
  IBOppiaineenSuoritus,
  isIBOppiaineenSuoritus
} from './IBOppiaineenSuoritus'

/**
 * IBTutkinnonOppiaine
 *
 * @see `fi.oph.koski.schema.IBTutkinnonOppiaine`
 */
export type IBTutkinnonOppiaine = IBDBCoreSuoritus | IBOppiaineenSuoritus

export const isIBTutkinnonOppiaine = (a: any): a is IBTutkinnonOppiaine =>
  isIBDBCoreSuoritus(a) || isIBOppiaineenSuoritus(a)
