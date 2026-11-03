import {
  IBDiplomanCoreSuoritus,
  isIBDiplomanCoreSuoritus
} from './IBDiplomanCoreSuoritus'
import {
  IBDiplomanOppiaineenSuoritus,
  isIBDiplomanOppiaineenSuoritus
} from './IBDiplomanOppiaineenSuoritus'

/**
 * IBDiplomanOsasuoritus
 *
 * @see `fi.oph.koski.schema.IBDiplomanOsasuoritus`
 */
export type IBDiplomanOsasuoritus =
  IBDiplomanCoreSuoritus | IBDiplomanOppiaineenSuoritus

export const isIBDiplomanOsasuoritus = (a: any): a is IBDiplomanOsasuoritus =>
  isIBDiplomanCoreSuoritus(a) || isIBDiplomanOppiaineenSuoritus(a)
