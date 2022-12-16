import {
  NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa,
  isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa
} from './NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa'
import {
  PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus,
  isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus
} from './PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus'

/**
 * PerusopetukseenValmistavanOpetuksenOsasuoritus
 *
 * @see `fi.oph.koski.schema.PerusopetukseenValmistavanOpetuksenOsasuoritus`
 */
export type PerusopetukseenValmistavanOpetuksenOsasuoritus =
  | NuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa
  | PerusopetukseenValmistavanOpetuksenOppiaineenSuoritus

export const isPerusopetukseenValmistavanOpetuksenOsasuoritus = (
  a: any
): a is PerusopetukseenValmistavanOpetuksenOsasuoritus =>
  isNuortenPerusopetuksenOppiaineenSuoritusValmistavassaOpetuksessa(a) ||
  isPerusopetukseenValmistavanOpetuksenOppiaineenSuoritus(a)
