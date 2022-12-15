import { IBKurssi, isIBKurssi } from './IBKurssi'
import {
  PaikallinenLukionKurssi2015,
  isPaikallinenLukionKurssi2015
} from './PaikallinenLukionKurssi2015'
import {
  ValtakunnallinenLukionKurssi2015,
  isValtakunnallinenLukionKurssi2015
} from './ValtakunnallinenLukionKurssi2015'

/**
 * PreIBKurssi2015
 *
 * @see `fi.oph.koski.schema.PreIBKurssi2015`
 */
export type PreIBKurssi2015 =
  | IBKurssi
  | PaikallinenLukionKurssi2015
  | ValtakunnallinenLukionKurssi2015

export const isPreIBKurssi2015 = (a: any): a is PreIBKurssi2015 =>
  isIBKurssi(a) ||
  isPaikallinenLukionKurssi2015(a) ||
  isValtakunnallinenLukionKurssi2015(a)
