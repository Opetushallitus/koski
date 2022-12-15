import {
  PreIBLukionModuulinSuoritusOppiaineissa2019,
  isPreIBLukionModuulinSuoritusOppiaineissa2019
} from './PreIBLukionModuulinSuoritusOppiaineissa2019'
import {
  PreIBLukionPaikallisenOpintojaksonSuoritus2019,
  isPreIBLukionPaikallisenOpintojaksonSuoritus2019
} from './PreIBLukionPaikallisenOpintojaksonSuoritus2019'

/**
 * PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019`
 */
export type PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =

    | PreIBLukionModuulinSuoritusOppiaineissa2019
    | PreIBLukionPaikallisenOpintojaksonSuoritus2019

export const isPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  (
    a: any
  ): a is PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =>
    isPreIBLukionModuulinSuoritusOppiaineissa2019(a) ||
    isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a)
