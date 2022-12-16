import {
  NumeerinenLukionArviointi,
  isNumeerinenLukionArviointi
} from './NumeerinenLukionArviointi'
import {
  SanallinenLukionArviointi,
  isSanallinenLukionArviointi
} from './SanallinenLukionArviointi'

/**
 * LukionArviointi
 *
 * @see `fi.oph.koski.schema.LukionArviointi`
 */
export type LukionArviointi =
  | NumeerinenLukionArviointi
  | SanallinenLukionArviointi

export const isLukionArviointi = (a: any): a is LukionArviointi =>
  isNumeerinenLukionArviointi(a) || isSanallinenLukionArviointi(a)
