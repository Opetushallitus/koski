import {
  AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
} from './AhvenanmaanAikuistenPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenOppimääränSuoritus,
  isAhvenanmaanPerusopetuksenOppimääränSuoritus
} from './AhvenanmaanPerusopetuksenOppimaaranSuoritus'
import {
  AhvenanmaanPerusopetuksenVuosiluokanSuoritus,
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus
} from './AhvenanmaanPerusopetuksenVuosiluokanSuoritus'

/**
 * AhvenanmaanPerusopetuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenPäätasonSuoritus`
 */
export type AhvenanmaanPerusopetuksenPäätasonSuoritus =
  | AhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus
  | AhvenanmaanPerusopetuksenOppimääränSuoritus
  | AhvenanmaanPerusopetuksenVuosiluokanSuoritus

export const isAhvenanmaanPerusopetuksenPäätasonSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenPäätasonSuoritus =>
  isAhvenanmaanAikuistenPerusopetuksenOppimääränSuoritus(a) ||
  isAhvenanmaanPerusopetuksenOppimääränSuoritus(a) ||
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(a)
