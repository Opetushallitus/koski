import {
  LukionPaikallinenOpintojakso2019,
  isLukionPaikallinenOpintojakso2019
} from './LukionPaikallinenOpintojakso2019'

/**
 * PreIBPaikallinenOpintojakso2019
 *
 * @see `fi.oph.koski.schema.PreIBPaikallinenOpintojakso2019`
 */
export type PreIBPaikallinenOpintojakso2019 = LukionPaikallinenOpintojakso2019

export const isPreIBPaikallinenOpintojakso2019 = (
  a: any
): a is PreIBPaikallinenOpintojakso2019 => isLukionPaikallinenOpintojakso2019(a)
