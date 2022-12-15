import {
  LukionMatematiikka2019,
  isLukionMatematiikka2019
} from './LukionMatematiikka2019'
import {
  LukionMuuValtakunnallinenOppiaine2019,
  isLukionMuuValtakunnallinenOppiaine2019
} from './LukionMuuValtakunnallinenOppiaine2019'
import { LukionUskonto2019, isLukionUskonto2019 } from './LukionUskonto2019'
import {
  LukionÄidinkieliJaKirjallisuus2019,
  isLukionÄidinkieliJaKirjallisuus2019
} from './LukionAidinkieliJaKirjallisuus2019'
import {
  PaikallinenLukionOppiaine2019,
  isPaikallinenLukionOppiaine2019
} from './PaikallinenLukionOppiaine2019'
import {
  VierasTaiToinenKotimainenKieli2019,
  isVierasTaiToinenKotimainenKieli2019
} from './VierasTaiToinenKotimainenKieli2019'

/**
 * PreIBLukionOppiaine2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionOppiaine2019`
 */
export type PreIBLukionOppiaine2019 =
  | LukionMatematiikka2019
  | LukionMuuValtakunnallinenOppiaine2019
  | LukionUskonto2019
  | LukionÄidinkieliJaKirjallisuus2019
  | PaikallinenLukionOppiaine2019
  | VierasTaiToinenKotimainenKieli2019

export const isPreIBLukionOppiaine2019 = (
  a: any
): a is PreIBLukionOppiaine2019 =>
  isLukionMatematiikka2019(a) ||
  isLukionMuuValtakunnallinenOppiaine2019(a) ||
  isLukionUskonto2019(a) ||
  isLukionÄidinkieliJaKirjallisuus2019(a) ||
  isPaikallinenLukionOppiaine2019(a) ||
  isVierasTaiToinenKotimainenKieli2019(a)
