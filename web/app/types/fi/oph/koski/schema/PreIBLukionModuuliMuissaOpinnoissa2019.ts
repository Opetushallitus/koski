import {
  LukionMuuModuuliMuissaOpinnoissa2019,
  isLukionMuuModuuliMuissaOpinnoissa2019
} from './LukionMuuModuuliMuissaOpinnoissa2019'
import {
  LukionVieraanKielenModuuliMuissaOpinnoissa2019,
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019
} from './LukionVieraanKielenModuuliMuissaOpinnoissa2019'

/**
 * PreIBLukionModuuliMuissaOpinnoissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuuliMuissaOpinnoissa2019`
 */
export type PreIBLukionModuuliMuissaOpinnoissa2019 =
  | LukionMuuModuuliMuissaOpinnoissa2019
  | LukionVieraanKielenModuuliMuissaOpinnoissa2019

export const isPreIBLukionModuuliMuissaOpinnoissa2019 = (
  a: any
): a is PreIBLukionModuuliMuissaOpinnoissa2019 =>
  isLukionMuuModuuliMuissaOpinnoissa2019(a) ||
  isLukionVieraanKielenModuuliMuissaOpinnoissa2019(a)
