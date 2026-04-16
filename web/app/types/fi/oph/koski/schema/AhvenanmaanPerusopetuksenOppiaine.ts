import {
  AhvenanmaanPerusopetuksenMuuOppiaine,
  isAhvenanmaanPerusopetuksenMuuOppiaine
} from './AhvenanmaanPerusopetuksenMuuOppiaine'
import {
  AhvenanmaanPerusopetuksenPaikallinenOppiaine,
  isAhvenanmaanPerusopetuksenPaikallinenOppiaine
} from './AhvenanmaanPerusopetuksenPaikallinenOppiaine'
import {
  AhvenanmaanPerusopetuksenVierasKieli,
  isAhvenanmaanPerusopetuksenVierasKieli
} from './AhvenanmaanPerusopetuksenVierasKieli'

/**
 * AhvenanmaanPerusopetuksenOppiaine
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaine`
 */
export type AhvenanmaanPerusopetuksenOppiaine =
  | AhvenanmaanPerusopetuksenMuuOppiaine
  | AhvenanmaanPerusopetuksenPaikallinenOppiaine
  | AhvenanmaanPerusopetuksenVierasKieli

export const isAhvenanmaanPerusopetuksenOppiaine = (
  a: any
): a is AhvenanmaanPerusopetuksenOppiaine =>
  isAhvenanmaanPerusopetuksenMuuOppiaine(a) ||
  isAhvenanmaanPerusopetuksenPaikallinenOppiaine(a) ||
  isAhvenanmaanPerusopetuksenVierasKieli(a)
