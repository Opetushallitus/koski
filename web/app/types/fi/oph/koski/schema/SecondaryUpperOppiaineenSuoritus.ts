import {
  SecondaryUpperOppiaineenSuoritusS6,
  isSecondaryUpperOppiaineenSuoritusS6
} from './SecondaryUpperOppiaineenSuoritusS6'
import {
  SecondaryUpperOppiaineenSuoritusS7,
  isSecondaryUpperOppiaineenSuoritusS7
} from './SecondaryUpperOppiaineenSuoritusS7'

/**
 * SecondaryUpperOppiaineenSuoritus
 *
 * @see `fi.oph.koski.schema.SecondaryUpperOppiaineenSuoritus`
 */
export type SecondaryUpperOppiaineenSuoritus =
  | SecondaryUpperOppiaineenSuoritusS6
  | SecondaryUpperOppiaineenSuoritusS7

export const isSecondaryUpperOppiaineenSuoritus = (
  a: any
): a is SecondaryUpperOppiaineenSuoritus =>
  isSecondaryUpperOppiaineenSuoritusS6(a) ||
  isSecondaryUpperOppiaineenSuoritusS7(a)
