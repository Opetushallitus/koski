import { DIANäyttötutkinto, isDIANäyttötutkinto } from './DIANayttotutkinto'
import {
  DIAOppiaineenTutkintovaiheenLukukausi,
  isDIAOppiaineenTutkintovaiheenLukukausi
} from './DIAOppiaineenTutkintovaiheenLukukausi'
import { DIAPäättökoe, isDIAPäättökoe } from './DIAPaattokoe'

/**
 * DIAOppiaineenTutkintovaiheenOsasuoritus
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenOsasuoritus`
 */
export type DIAOppiaineenTutkintovaiheenOsasuoritus =
  | DIANäyttötutkinto
  | DIAOppiaineenTutkintovaiheenLukukausi
  | DIAPäättökoe

export const isDIAOppiaineenTutkintovaiheenOsasuoritus = (
  a: any
): a is DIAOppiaineenTutkintovaiheenOsasuoritus =>
  isDIANäyttötutkinto(a) ||
  isDIAOppiaineenTutkintovaiheenLukukausi(a) ||
  isDIAPäättökoe(a)
