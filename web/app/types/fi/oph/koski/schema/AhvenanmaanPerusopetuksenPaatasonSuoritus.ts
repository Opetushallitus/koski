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
  | AhvenanmaanPerusopetuksenOppimääränSuoritus
  | AhvenanmaanPerusopetuksenVuosiluokanSuoritus

export const isAhvenanmaanPerusopetuksenPäätasonSuoritus = (
  a: any
): a is AhvenanmaanPerusopetuksenPäätasonSuoritus =>
  isAhvenanmaanPerusopetuksenOppimääränSuoritus(a) ||
  isAhvenanmaanPerusopetuksenVuosiluokanSuoritus(a)
