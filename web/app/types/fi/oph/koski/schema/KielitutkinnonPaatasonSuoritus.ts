import {
  ValtionhallinnonKielitutkinnonSuoritus,
  isValtionhallinnonKielitutkinnonSuoritus
} from './ValtionhallinnonKielitutkinnonSuoritus'
import {
  YleisenKielitutkinnonSuoritus,
  isYleisenKielitutkinnonSuoritus
} from './YleisenKielitutkinnonSuoritus'

/**
 * KielitutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.KielitutkinnonPäätasonSuoritus`
 */
export type KielitutkinnonPäätasonSuoritus =
  | ValtionhallinnonKielitutkinnonSuoritus
  | YleisenKielitutkinnonSuoritus

export const isKielitutkinnonPäätasonSuoritus = (
  a: any
): a is KielitutkinnonPäätasonSuoritus =>
  isValtionhallinnonKielitutkinnonSuoritus(a) ||
  isYleisenKielitutkinnonSuoritus(a)
