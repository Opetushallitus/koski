import {
  LukionMatematiikka2015,
  isLukionMatematiikka2015
} from './LukionMatematiikka2015'
import {
  LukionMuuValtakunnallinenOppiaine2015,
  isLukionMuuValtakunnallinenOppiaine2015
} from './LukionMuuValtakunnallinenOppiaine2015'
import { LukionUskonto2015, isLukionUskonto2015 } from './LukionUskonto2015'
import {
  LukionÄidinkieliJaKirjallisuus2015,
  isLukionÄidinkieliJaKirjallisuus2015
} from './LukionAidinkieliJaKirjallisuus2015'
import {
  PaikallinenLukionOppiaine2015,
  isPaikallinenLukionOppiaine2015
} from './PaikallinenLukionOppiaine2015'
import {
  VierasTaiToinenKotimainenKieli2015,
  isVierasTaiToinenKotimainenKieli2015
} from './VierasTaiToinenKotimainenKieli2015'

/**
 * LukionOppiaine2015
 *
 * @see `fi.oph.koski.schema.LukionOppiaine2015`
 */
export type LukionOppiaine2015 =
  | LukionMatematiikka2015
  | LukionMuuValtakunnallinenOppiaine2015
  | LukionUskonto2015
  | LukionÄidinkieliJaKirjallisuus2015
  | PaikallinenLukionOppiaine2015
  | VierasTaiToinenKotimainenKieli2015

export const isLukionOppiaine2015 = (a: any): a is LukionOppiaine2015 =>
  isLukionMatematiikka2015(a) ||
  isLukionMuuValtakunnallinenOppiaine2015(a) ||
  isLukionUskonto2015(a) ||
  isLukionÄidinkieliJaKirjallisuus2015(a) ||
  isPaikallinenLukionOppiaine2015(a) ||
  isVierasTaiToinenKotimainenKieli2015(a)
