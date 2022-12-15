import {
  LukionModuulinSuoritusOppiaineissa2019,
  isLukionModuulinSuoritusOppiaineissa2019
} from './LukionModuulinSuoritusOppiaineissa2019'
import {
  LukionPaikallisenOpintojaksonSuoritus2019,
  isLukionPaikallisenOpintojaksonSuoritus2019
} from './LukionPaikallisenOpintojaksonSuoritus2019'

/**
 * LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019
 *
 * @see `fi.oph.koski.schema.LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019`
 */
export type LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  | LukionModuulinSuoritusOppiaineissa2019
  | LukionPaikallisenOpintojaksonSuoritus2019

export const isLukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =
  (
    a: any
  ): a is LukionModuulinTaiPaikallisenOpintojaksonSuoritusOppiaineissa2019 =>
    isLukionModuulinSuoritusOppiaineissa2019(a) ||
    isLukionPaikallisenOpintojaksonSuoritus2019(a)
