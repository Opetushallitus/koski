import {
  PaikallinenLukionKurssi2015,
  isPaikallinenLukionKurssi2015
} from './PaikallinenLukionKurssi2015'
import {
  ValtakunnallinenLukionKurssi2015,
  isValtakunnallinenLukionKurssi2015
} from './ValtakunnallinenLukionKurssi2015'

/**
 * LukionKurssi2015
 *
 * @see `fi.oph.koski.schema.LukionKurssi2015`
 */
export type LukionKurssi2015 =
  | PaikallinenLukionKurssi2015
  | ValtakunnallinenLukionKurssi2015

export const isLukionKurssi2015 = (a: any): a is LukionKurssi2015 =>
  isPaikallinenLukionKurssi2015(a) || isValtakunnallinenLukionKurssi2015(a)
