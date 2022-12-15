import {
  LanguageAcquisition,
  isLanguageAcquisition
} from './LanguageAcquisition'
import {
  LanguageAndLiterature,
  isLanguageAndLiterature
} from './LanguageAndLiterature'
import { MYPOppiaineMuu, isMYPOppiaineMuu } from './MYPOppiaineMuu'

/**
 * MYPOppiaine
 *
 * @see `fi.oph.koski.schema.MYPOppiaine`
 */
export type MYPOppiaine =
  | LanguageAcquisition
  | LanguageAndLiterature
  | MYPOppiaineMuu

export const isMYPOppiaine = (a: any): a is MYPOppiaine =>
  isLanguageAcquisition(a) || isLanguageAndLiterature(a) || isMYPOppiaineMuu(a)
