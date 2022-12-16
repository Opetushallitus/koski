import {
  LukionMuuModuuliOppiaineissa2019,
  isLukionMuuModuuliOppiaineissa2019
} from './LukionMuuModuuliOppiaineissa2019'
import {
  LukionVieraanKielenModuuliOppiaineissa2019,
  isLukionVieraanKielenModuuliOppiaineissa2019
} from './LukionVieraanKielenModuuliOppiaineissa2019'

/**
 * PreIBLukionModuuliOppiaineissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuuliOppiaineissa2019`
 */
export type PreIBLukionModuuliOppiaineissa2019 =
  | LukionMuuModuuliOppiaineissa2019
  | LukionVieraanKielenModuuliOppiaineissa2019

export const isPreIBLukionModuuliOppiaineissa2019 = (
  a: any
): a is PreIBLukionModuuliOppiaineissa2019 =>
  isLukionMuuModuuliOppiaineissa2019(a) ||
  isLukionVieraanKielenModuuliOppiaineissa2019(a)
