import {
  PreIBLukionModuulinSuoritusMuissaOpinnoissa2019,
  isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019
} from './PreIBLukionModuulinSuoritusMuissaOpinnoissa2019'
import {
  PreIBLukionPaikallisenOpintojaksonSuoritus2019,
  isPreIBLukionPaikallisenOpintojaksonSuoritus2019
} from './PreIBLukionPaikallisenOpintojaksonSuoritus2019'

/**
 * PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019
 *
 * @see `fi.oph.koski.schema.PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019`
 */
export type PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =

    | PreIBLukionModuulinSuoritusMuissaOpinnoissa2019
    | PreIBLukionPaikallisenOpintojaksonSuoritus2019

export const isPreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =
  (
    a: any
  ): a is PreIBLukionModuulinTaiPaikallisenOpintojaksonSuoritusMuissaOpinnoissa2019 =>
    isPreIBLukionModuulinSuoritusMuissaOpinnoissa2019(a) ||
    isPreIBLukionPaikallisenOpintojaksonSuoritus2019(a)
