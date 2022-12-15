import {
  NumeerinenInternationalSchoolOppiaineenArviointi,
  isNumeerinenInternationalSchoolOppiaineenArviointi
} from './NumeerinenInternationalSchoolOppiaineenArviointi'
import {
  PassFailOppiaineenArviointi,
  isPassFailOppiaineenArviointi
} from './PassFailOppiaineenArviointi'

/**
 * MYPArviointi
 *
 * @see `fi.oph.koski.schema.MYPArviointi`
 */
export type MYPArviointi =
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export const isMYPArviointi = (a: any): a is MYPArviointi =>
  isNumeerinenInternationalSchoolOppiaineenArviointi(a) ||
  isPassFailOppiaineenArviointi(a)
