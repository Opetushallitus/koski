import {
  PreIBLukionModuulinSuoritusMuissaOpinnoissa2019,
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019
} from './PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
import {
  PreIBLukionModuulinSuoritusOppiaineissa2019,
  isPreIBLukionModuulinSuoritusOppiaineissa2019
} from './PreIBLukionModuulinSuoritusOppiaineissa2019'
import {
  PreIBLukionPaikallisenOpintojaksonSuoritus2019,
  isPreIBLukionPaikallisenOpintojaksonSuoritus2019
} from './PreIBLukionPaikallisenOpintojaksonSuoritus2019'

/**
 * PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019`
 */
export type PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 =
  | PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
  | PreIBLukionModuulinSuoritusOppiaineissa2019
  | PreIBLukionPaikallisenOpintojaksonSuoritus2019

export const isPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 = (
  a: any
): a is PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritus2019 =>
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
  isPreIBLukionModuulinSuoritusOppiaineissa2019(a) ||
  isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a)
