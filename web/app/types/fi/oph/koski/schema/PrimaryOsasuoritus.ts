import {
  PrimaryLapsiOppimisalueenSuoritus,
  isPrimaryLapsiOppimisalueenSuoritus
} from './PrimaryLapsiOppimisalueenSuoritus'
import {
  PrimaryOppimisalueenSuoritus,
  isPrimaryOppimisalueenSuoritus
} from './PrimaryOppimisalueenSuoritus'

/**
 * PrimaryOsasuoritus
 *
 * @see `fi.oph.koski.schema.PrimaryOsasuoritus`
 */
export type PrimaryOsasuoritus =
  | PrimaryLapsiOppimisalueenSuoritus
  | PrimaryOppimisalueenSuoritus

export const isPrimaryOsasuoritus = (a: any): a is PrimaryOsasuoritus =>
  isPrimaryLapsiOppimisalueenSuoritus(a) || isPrimaryOppimisalueenSuoritus(a)
