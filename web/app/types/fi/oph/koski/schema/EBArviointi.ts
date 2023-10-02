import {
  EBTutkintoFinalMarkArviointi,
  isEBTutkintoFinalMarkArviointi
} from './EBTutkintoFinalMarkArviointi'

/**
 * EBArviointi
 *
 * @see `fi.oph.koski.schema.EBArviointi`
 */
export type EBArviointi = EBTutkintoFinalMarkArviointi

export const isEBArviointi = (a: any): a is EBArviointi =>
  isEBTutkintoFinalMarkArviointi(a)
