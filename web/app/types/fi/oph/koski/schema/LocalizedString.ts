import { English, isEnglish } from './English'
import { Finnish, isFinnish } from './Finnish'
import { Swedish, isSwedish } from './Swedish'

/**
 * LocalizedString
 *
 * @see `fi.oph.koski.schema.LocalizedString`
 */
export type LocalizedString = English | Finnish | Swedish

export const isLocalizedString = (a: any): a is LocalizedString =>
  isEnglish(a) || isFinnish(a) || isSwedish(a)
