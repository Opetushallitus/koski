import { IBOppiaineLanguage, isIBOppiaineLanguage } from './IBOppiaineLanguage'
import { IBOppiaineMuu, isIBOppiaineMuu } from './IBOppiaineMuu'

/**
 * IBAineRyhmäOppiaine
 *
 * @see `fi.oph.koski.schema.IBAineRyhmäOppiaine`
 */
export type IBAineRyhmäOppiaine = IBOppiaineLanguage | IBOppiaineMuu

export const isIBAineRyhmäOppiaine = (a: any): a is IBAineRyhmäOppiaine =>
  isIBOppiaineLanguage(a) || isIBOppiaineMuu(a)
