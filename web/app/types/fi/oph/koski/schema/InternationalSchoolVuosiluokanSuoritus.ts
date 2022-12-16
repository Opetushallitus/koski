import {
  DiplomaVuosiluokanSuoritus,
  isDiplomaVuosiluokanSuoritus
} from './DiplomaVuosiluokanSuoritus'
import {
  MYPVuosiluokanSuoritus,
  isMYPVuosiluokanSuoritus
} from './MYPVuosiluokanSuoritus'
import {
  PYPVuosiluokanSuoritus,
  isPYPVuosiluokanSuoritus
} from './PYPVuosiluokanSuoritus'

/**
 * InternationalSchoolVuosiluokanSuoritus
 *
 * @see `fi.oph.koski.schema.InternationalSchoolVuosiluokanSuoritus`
 */
export type InternationalSchoolVuosiluokanSuoritus =
  | DiplomaVuosiluokanSuoritus
  | MYPVuosiluokanSuoritus
  | PYPVuosiluokanSuoritus

export const isInternationalSchoolVuosiluokanSuoritus = (
  a: any
): a is InternationalSchoolVuosiluokanSuoritus =>
  isDiplomaVuosiluokanSuoritus(a) ||
  isMYPVuosiluokanSuoritus(a) ||
  isPYPVuosiluokanSuoritus(a)
