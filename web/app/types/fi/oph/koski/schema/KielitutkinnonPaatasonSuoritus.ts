import {
  YleisenKielitutkinnonSuoritus,
  isYleisenKielitutkinnonSuoritus
} from './YleisenKielitutkinnonSuoritus'

/**
 * KielitutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.KielitutkinnonPäätasonSuoritus`
 */
export type KielitutkinnonPäätasonSuoritus = YleisenKielitutkinnonSuoritus

export const isKielitutkinnonPäätasonSuoritus = (
  a: any
): a is KielitutkinnonPäätasonSuoritus => isYleisenKielitutkinnonSuoritus(a)
