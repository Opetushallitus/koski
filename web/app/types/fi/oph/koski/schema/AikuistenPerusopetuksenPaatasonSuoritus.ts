import {
  AikuistenPerusopetuksenAlkuvaiheenSuoritus,
  isAikuistenPerusopetuksenAlkuvaiheenSuoritus
} from './AikuistenPerusopetuksenAlkuvaiheenSuoritus'
import {
  AikuistenPerusopetuksenOppiaineenOppimääränSuoritus,
  isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus
} from './AikuistenPerusopetuksenOppiaineenOppimaaranSuoritus'
import {
  AikuistenPerusopetuksenOppimääränSuoritus,
  isAikuistenPerusopetuksenOppimääränSuoritus
} from './AikuistenPerusopetuksenOppimaaranSuoritus'

/**
 * AikuistenPerusopetuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenPäätasonSuoritus`
 */
export type AikuistenPerusopetuksenPäätasonSuoritus =
  | AikuistenPerusopetuksenAlkuvaiheenSuoritus
  | AikuistenPerusopetuksenOppiaineenOppimääränSuoritus
  | AikuistenPerusopetuksenOppimääränSuoritus

export const isAikuistenPerusopetuksenPäätasonSuoritus = (
  a: any
): a is AikuistenPerusopetuksenPäätasonSuoritus =>
  isAikuistenPerusopetuksenAlkuvaiheenSuoritus(a) ||
  isAikuistenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isAikuistenPerusopetuksenOppimääränSuoritus(a)
