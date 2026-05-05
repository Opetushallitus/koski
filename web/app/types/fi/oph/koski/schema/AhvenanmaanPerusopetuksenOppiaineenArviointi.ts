import {
  NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi,
  isNumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi
} from './NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi'
import {
  SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi,
  isSanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi
} from './SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi'

/**
 * AhvenanmaanPerusopetuksenOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.AhvenanmaanPerusopetuksenOppiaineenArviointi`
 */
export type AhvenanmaanPerusopetuksenOppiaineenArviointi =
  | NumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi
  | SanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi

export const isAhvenanmaanPerusopetuksenOppiaineenArviointi = (
  a: any
): a is AhvenanmaanPerusopetuksenOppiaineenArviointi =>
  isNumeerinenAhvenanmaanPerusopetuksenOppiaineenArviointi(a) ||
  isSanallinenAhvenanmaanPerusopetuksenOppiaineenArviointi(a)
