import {
  NuortenPerusopetuksenOppiaineenOppimääränSuoritus,
  isNuortenPerusopetuksenOppiaineenOppimääränSuoritus
} from './NuortenPerusopetuksenOppiaineenOppimaaranSuoritus'
import {
  NuortenPerusopetuksenOppimääränSuoritus,
  isNuortenPerusopetuksenOppimääränSuoritus
} from './NuortenPerusopetuksenOppimaaranSuoritus'
import {
  PerusopetuksenVuosiluokanSuoritus,
  isPerusopetuksenVuosiluokanSuoritus
} from './PerusopetuksenVuosiluokanSuoritus'

/**
 * PerusopetuksenPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.PerusopetuksenPäätasonSuoritus`
 */
export type PerusopetuksenPäätasonSuoritus =
  | NuortenPerusopetuksenOppiaineenOppimääränSuoritus
  | NuortenPerusopetuksenOppimääränSuoritus
  | PerusopetuksenVuosiluokanSuoritus

export const isPerusopetuksenPäätasonSuoritus = (
  a: any
): a is PerusopetuksenPäätasonSuoritus =>
  isNuortenPerusopetuksenOppiaineenOppimääränSuoritus(a) ||
  isNuortenPerusopetuksenOppimääränSuoritus(a) ||
  isPerusopetuksenVuosiluokanSuoritus(a)
