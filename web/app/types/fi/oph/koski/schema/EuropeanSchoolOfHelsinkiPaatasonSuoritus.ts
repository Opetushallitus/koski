import {
  EBTutkinnonSuoritus,
  isEBTutkinnonSuoritus
} from './EBTutkinnonSuoritus'
import {
  NurseryVuosiluokanSuoritus,
  isNurseryVuosiluokanSuoritus
} from './NurseryVuosiluokanSuoritus'
import {
  PrimaryVuosiluokanSuoritus,
  isPrimaryVuosiluokanSuoritus
} from './PrimaryVuosiluokanSuoritus'
import {
  SecondaryLowerVuosiluokanSuoritus,
  isSecondaryLowerVuosiluokanSuoritus
} from './SecondaryLowerVuosiluokanSuoritus'
import {
  SecondaryUpperVuosiluokanSuoritus,
  isSecondaryUpperVuosiluokanSuoritus
} from './SecondaryUpperVuosiluokanSuoritus'

/**
 * EuropeanSchoolOfHelsinkiPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiPäätasonSuoritus`
 */
export type EuropeanSchoolOfHelsinkiPäätasonSuoritus =
  | EBTutkinnonSuoritus
  | NurseryVuosiluokanSuoritus
  | PrimaryVuosiluokanSuoritus
  | SecondaryLowerVuosiluokanSuoritus
  | SecondaryUpperVuosiluokanSuoritus

export const isEuropeanSchoolOfHelsinkiPäätasonSuoritus = (
  a: any
): a is EuropeanSchoolOfHelsinkiPäätasonSuoritus =>
  isEBTutkinnonSuoritus(a) ||
  isNurseryVuosiluokanSuoritus(a) ||
  isPrimaryVuosiluokanSuoritus(a) ||
  isSecondaryLowerVuosiluokanSuoritus(a) ||
  isSecondaryUpperVuosiluokanSuoritus(a)
