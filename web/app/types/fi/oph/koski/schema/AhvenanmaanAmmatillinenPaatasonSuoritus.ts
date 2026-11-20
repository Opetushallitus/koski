import {
  AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus,
  isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus
} from './AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus'
import {
  AhvenanmaanAmmatillisenTutkinnonSuoritus,
  isAhvenanmaanAmmatillisenTutkinnonSuoritus
} from './AhvenanmaanAmmatillisenTutkinnonSuoritus'

/**
 * AhvenanmaanAmmatillinenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.AhvenanmaanAmmatillinenPäätasonSuoritus`
 */
export type AhvenanmaanAmmatillinenPäätasonSuoritus =
  | AhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus
  | AhvenanmaanAmmatillisenTutkinnonSuoritus

export const isAhvenanmaanAmmatillinenPäätasonSuoritus = (
  a: any
): a is AhvenanmaanAmmatillinenPäätasonSuoritus =>
  isAhvenanmaanAmmatillisenTutkinnonOsittainenSuoritus(a) ||
  isAhvenanmaanAmmatillisenTutkinnonSuoritus(a)
