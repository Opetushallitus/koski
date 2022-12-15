import {
  NuortenPerusopetuksenOppiaineenSuoritus,
  isNuortenPerusopetuksenOppiaineenSuoritus
} from './NuortenPerusopetuksenOppiaineenSuoritus'
import {
  PerusopetuksenToiminta_AlueenSuoritus,
  isPerusopetuksenToiminta_AlueenSuoritus
} from './PerusopetuksenToimintaAlueenSuoritus'

/**
 * OppiaineenTaiToiminta_AlueenSuoritus
 *
 * @see `fi.oph.koski.schema.OppiaineenTaiToiminta_AlueenSuoritus`
 */
export type OppiaineenTaiToiminta_AlueenSuoritus =
  | NuortenPerusopetuksenOppiaineenSuoritus
  | PerusopetuksenToiminta_AlueenSuoritus

export const isOppiaineenTaiToiminta_AlueenSuoritus = (
  a: any
): a is OppiaineenTaiToiminta_AlueenSuoritus =>
  isNuortenPerusopetuksenOppiaineenSuoritus(a) ||
  isPerusopetuksenToiminta_AlueenSuoritus(a)
