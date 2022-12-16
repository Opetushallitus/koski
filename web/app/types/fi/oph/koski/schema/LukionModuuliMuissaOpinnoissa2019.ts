import {
  LukionMuuModuuliMuissaOpinnoissa2019,
  isLukionMuuModuuliMuissaOpinnoissa2019
} from './LukionMuuModuuliMuissaOpinnoissa2019'
import {
  LukionVieraanKielenModuuliMuissaOpinnoissa2019,
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019
} from './LukionVieraanKielenModuuliMuissaOpinnoissa2019'

/**
 * LukionModuuliMuissaOpinnoissa2019
 *
 * @see `fi.oph.koski.schema.LukionModuuliMuissaOpinnoissa2019`
 */
export type LukionModuuliMuissaOpinnoissa2019 =
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019

export const isLukionModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionModuuliMuissaOpinnoissa2019 =>
  isLukionMuuModuuliMuissaOpinnoissa2019(a) ||
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019(a)
