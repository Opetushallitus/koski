import {
  SecondaryGradeArviointi,
  isSecondaryGradeArviointi
} from './SecondaryGradeArviointi'
import {
  SecondaryNumericalMarkArviointi,
  isSecondaryNumericalMarkArviointi
} from './SecondaryNumericalMarkArviointi'

/**
 * SecondaryLowerArviointi
 *
 * @see `fi.oph.koski.schema.SecondaryLowerArviointi`
 */
export type SecondaryLowerArviointi =
  | SecondaryGradeArviointi
  | SecondaryNumericalMarkArviointi

export const isSecondaryLowerArviointi = (
  a: any
): a is SecondaryLowerArviointi =>
  isSecondaryGradeArviointi(a) || isSecondaryNumericalMarkArviointi(a)
