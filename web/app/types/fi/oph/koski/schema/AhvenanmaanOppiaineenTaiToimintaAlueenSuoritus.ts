import {
  AhvenanmaanPerusopetuksenOppiaineenSuoritus,
  isAhvenanmaanPerusopetuksenOppiaineenSuoritus
} from './AhvenanmaanPerusopetuksenOppiaineenSuoritus'
import {
  AhvenanmaanPerusopetuksenToimintaAlueenSuoritus,
  isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus
} from './AhvenanmaanPerusopetuksenToimintaAlueenSuoritus'

/**
 * AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus
 *
 * @see `fi.oph.koski.schema.AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus`
 */
export type AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus =
  | AhvenanmaanPerusopetuksenOppiaineenSuoritus
  | AhvenanmaanPerusopetuksenToimintaAlueenSuoritus

export const isAhvenanmaanOppiaineenTaiToimintaAlueenSuoritus = (
  a: any
): a is AhvenanmaanOppiaineenTaiToimintaAlueenSuoritus =>
  isAhvenanmaanPerusopetuksenOppiaineenSuoritus(a) ||
  isAhvenanmaanPerusopetuksenToimintaAlueenSuoritus(a)
