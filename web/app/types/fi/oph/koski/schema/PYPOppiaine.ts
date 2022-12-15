import {
  LanguageAcquisition,
  isLanguageAcquisition
} from './LanguageAcquisition'
import {
  LanguageAndLiterature,
  isLanguageAndLiterature
} from './LanguageAndLiterature'
import { PYPOppiaineMuu, isPYPOppiaineMuu } from './PYPOppiaineMuu'

/**
 * PYPOppiaine
 *
 * @see `fi.oph.koski.schema.PYPOppiaine`
 */
export type PYPOppiaine =
  | LanguageAcquisition
  | LanguageAndLiterature
  | PYPOppiaineMuu

export const isPYPOppiaine = (a: any): a is PYPOppiaine =>
  isLanguageAcquisition(a) || isLanguageAndLiterature(a) || isPYPOppiaineMuu(a)
