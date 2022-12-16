import {
  NumeerinenPerusopetuksenOppiaineenArviointi,
  isNumeerinenPerusopetuksenOppiaineenArviointi
} from './NumeerinenPerusopetuksenOppiaineenArviointi'
import {
  SanallinenPerusopetuksenOppiaineenArviointi,
  isSanallinenPerusopetuksenOppiaineenArviointi
} from './SanallinenPerusopetuksenOppiaineenArviointi'

/**
 * PerusopetuksenOppiaineenArviointi
 *
 * @see `fi.oph.koski.schema.PerusopetuksenOppiaineenArviointi`
 */
export type PerusopetuksenOppiaineenArviointi =
  | NumeerinenPerusopetuksenOppiaineenArviointi
  | SanallinenPerusopetuksenOppiaineenArviointi

export const isPerusopetuksenOppiaineenArviointi = (
  a: any
): a is PerusopetuksenOppiaineenArviointi =>
  isNumeerinenPerusopetuksenOppiaineenArviointi(a) ||
  isSanallinenPerusopetuksenOppiaineenArviointi(a)
