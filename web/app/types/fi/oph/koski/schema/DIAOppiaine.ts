import { DIAOppiaineKieli, isDIAOppiaineKieli } from './DIAOppiaineKieli'
import {
  DIAOppiaineLisäaine,
  isDIAOppiaineLisäaine
} from './DIAOppiaineLisaaine'
import {
  DIAOppiaineLisäaineKieli,
  isDIAOppiaineLisäaineKieli
} from './DIAOppiaineLisaaineKieli'
import { DIAOppiaineMuu, isDIAOppiaineMuu } from './DIAOppiaineMuu'
import {
  DIAOppiaineÄidinkieli,
  isDIAOppiaineÄidinkieli
} from './DIAOppiaineAidinkieli'

/**
 * DIAOppiaine
 *
 * @see `fi.oph.koski.schema.DIAOppiaine`
 */
export type DIAOppiaine =
  | DIAOppiaineKieli
  | DIAOppiaineLisäaine
  | DIAOppiaineLisäaineKieli
  | DIAOppiaineMuu
  | DIAOppiaineÄidinkieli

export const isDIAOppiaine = (a: any): a is DIAOppiaine =>
  isDIAOppiaineKieli(a) ||
  isDIAOppiaineLisäaine(a) ||
  isDIAOppiaineLisäaineKieli(a) ||
  isDIAOppiaineMuu(a) ||
  isDIAOppiaineÄidinkieli(a)
