import {
  NumeerinenLukionOppiaineenArviointi2019,
  isNumeerinenLukionOppiaineenArviointi2019
} from './NumeerinenLukionOppiaineenArviointi2019'
import {
  SanallinenLukionOppiaineenArviointi2019,
  isSanallinenLukionOppiaineenArviointi2019
} from './SanallinenLukionOppiaineenArviointi2019'

/**
 * LukionOppiaineenArviointi2019
 *
 * @see `fi.oph.koski.schema.LukionOppiaineenArviointi2019`
 */
export type LukionOppiaineenArviointi2019 =
  | NumeerinenLukionOppiaineenArviointi2019
  | SanallinenLukionOppiaineenArviointi2019

export const isLukionOppiaineenArviointi2019 = (
  a: any
): a is LukionOppiaineenArviointi2019 =>
  isNumeerinenLukionOppiaineenArviointi2019(a) ||
  isSanallinenLukionOppiaineenArviointi2019(a)
