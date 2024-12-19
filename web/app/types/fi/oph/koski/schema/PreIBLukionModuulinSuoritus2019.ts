import {
  PreIBLukionModuulinSuoritusMuissaOpinnoissa2019,
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019
} from './PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
import {
  PreIBLukionModuulinSuoritusOppiaineissa2019,
  isPreIBLukionModuulinSuoritusOppiaineissa2019
} from './PreIBLukionModuulinSuoritusOppiaineissa2019'

/**
 * PreIBLukionModuulinSuoritus2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinSuoritus2019`
 */
export type PreIBLukionModuulinSuoritus2019 =
  | PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
  | PreIBLukionModuulinSuoritusOppiaineissa2019

export const isPreIBLukionModuulinSuoritus2019 = (
  a: any
): a is PreIBLukionModuulinSuoritus2019 =>
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
  isPreIBLukionModuulinSuoritusOppiaineissa2019(a)
