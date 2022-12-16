import {
  InternationalSchoolIBOppiaineenArviointi,
  isInternationalSchoolIBOppiaineenArviointi
} from './InternationalSchoolIBOppiaineenArviointi'
import {
  NumeerinenInternationalSchoolOppiaineenArviointi,
  isNumeerinenInternationalSchoolOppiaineenArviointi
} from './NumeerinenInternationalSchoolOppiaineenArviointi'
import {
  PassFailOppiaineenArviointi,
  isPassFailOppiaineenArviointi
} from './PassFailOppiaineenArviointi'

/**
 * DiplomaArviointi
 *
 * @see `fi.oph.koski.schema.DiplomaArviointi`
 */
export type DiplomaArviointi =
  | InternationalSchoolIBOppiaineenArviointi
  | NumeerinenInternationalSchoolOppiaineenArviointi
  | PassFailOppiaineenArviointi

export const isDiplomaArviointi = (a: any): a is DiplomaArviointi =>
  isInternationalSchoolIBOppiaineenArviointi(a) ||
  isNumeerinenInternationalSchoolOppiaineenArviointi(a) ||
  isPassFailOppiaineenArviointi(a)
