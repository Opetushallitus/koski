import {
  DIATutkinnonSuoritus,
  isDIATutkinnonSuoritus
} from './DIATutkinnonSuoritus'
import {
  DIAValmistavanVaiheenSuoritus,
  isDIAValmistavanVaiheenSuoritus
} from './DIAValmistavanVaiheenSuoritus'

/**
 * DIAPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.DIAPäätasonSuoritus`
 */
export type DIAPäätasonSuoritus =
  | DIATutkinnonSuoritus
  | DIAValmistavanVaiheenSuoritus

export const isDIAPäätasonSuoritus = (a: any): a is DIAPäätasonSuoritus =>
  isDIATutkinnonSuoritus(a) || isDIAValmistavanVaiheenSuoritus(a)
