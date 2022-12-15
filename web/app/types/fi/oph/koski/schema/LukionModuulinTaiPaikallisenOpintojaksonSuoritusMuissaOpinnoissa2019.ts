import {
  LukionModuulinSuoritusMuissaOpinnoissa2019,
  isLukionModuulinSuoritusMuissaOpinnoissa2019
} from './LukionModuulinSuoritusMuissaOpinnoissa2019'
import {
  LukionPaikallisenOpintojaksonSuoritus2019,
  isLukionPaikallisenOpintojaksonSuoritus2019
} from './LukionPaikallisenOpintojaksonSuoritus2019'

/**
 * LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019
 *
 * @see `fi.oph.koski.schema.LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019`
 */
export type LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =

    | LukionModuulinSuoritusMuissaOpinnoissa2019
    | LukionPaikallisenOpintojaksonSuoritus2019

export const isLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =
  (
    a: any
  ): a is LukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =>
    isLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
    isLukionPaikallisenOpintojaksonSuoritus2019(a)
