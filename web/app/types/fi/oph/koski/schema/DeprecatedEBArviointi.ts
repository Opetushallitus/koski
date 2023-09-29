import {
  DeprecatedEBTutkintoFinalMarkArviointi,
  isDeprecatedEBTutkintoFinalMarkArviointi
} from './DeprecatedEBTutkintoFinalMarkArviointi'
import {
  DeprecatedEBTutkintoPreliminaryMarkArviointi,
  isDeprecatedEBTutkintoPreliminaryMarkArviointi
} from './DeprecatedEBTutkintoPreliminaryMarkArviointi'

/**
 * DeprecatedEBArviointi
 *
 * @see `fi.oph.koski.schema.DeprecatedEBArviointi`
 */
export type DeprecatedEBArviointi =
  | DeprecatedEBTutkintoFinalMarkArviointi
  | DeprecatedEBTutkintoPreliminaryMarkArviointi

export const isDeprecatedEBArviointi = (a: any): a is DeprecatedEBArviointi =>
  isDeprecatedEBTutkintoFinalMarkArviointi(a) ||
  isDeprecatedEBTutkintoPreliminaryMarkArviointi(a)
