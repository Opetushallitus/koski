import {
  EBTutkintoFinalMarkArviointi,
  isEBTutkintoFinalMarkArviointi
} from './EBTutkintoFinalMarkArviointi'
import {
  EBTutkintoPreliminaryMarkArviointi,
  isEBTutkintoPreliminaryMarkArviointi
} from './EBTutkintoPreliminaryMarkArviointi'

/**
 * EBArviointi
 *
 * @see `fi.oph.koski.schema.EBArviointi`
 */
export type EBArviointi =
  | EBTutkintoFinalMarkArviointi
  | EBTutkintoPreliminaryMarkArviointi

export const isEBArviointi = (a: any): a is EBArviointi =>
  isEBTutkintoFinalMarkArviointi(a) || isEBTutkintoPreliminaryMarkArviointi(a)
