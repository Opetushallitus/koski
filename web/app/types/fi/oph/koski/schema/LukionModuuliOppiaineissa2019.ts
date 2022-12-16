import {
  LukionMuuModuuliOppiaineissa2019,
  isLukionMuuModuuliOppiaineissa2019
} from './LukionMuuModuuliOppiaineissa2019'
import {
  LukionVieraanKielenModuuliOppiaineissa2019,
  isLukionVieraanKielenModuuliOppiaineissa2019
} from './LukionVieraanKielenModuuliOppiaineissa2019'

/**
 * LukionModuuliOppiaineissa2019
 *
 * @see `fi.oph.koski.schema.LukionModuuliOppiaineissa2019`
 */
export type LukionModuuliOppiaineissa2019 =
  | LukionMuuModuuliOppiaineissa2019
  | LukionVieraanKielenModuuliOppiaineissa2019

export const isLukionModuuliOppiaineissa2019 = (
  a: any
): a is LukionModuuliOppiaineissa2019 =>
  isLukionMuuModuuliOppiaineissa2019(a) ||
  isLukionVieraanKielenModuuliOppiaineissa2019(a)
