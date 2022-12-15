import {
  IBTutkinnonSuoritus,
  isIBTutkinnonSuoritus
} from './IBTutkinnonSuoritus'
import { PreIBSuoritus2015, isPreIBSuoritus2015 } from './PreIBSuoritus2015'
import { PreIBSuoritus2019, isPreIBSuoritus2019 } from './PreIBSuoritus2019'

/**
 * IBPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.IBPäätasonSuoritus`
 */
export type IBPäätasonSuoritus =
  | IBTutkinnonSuoritus
  | PreIBSuoritus2015
  | PreIBSuoritus2019

export const isIBPäätasonSuoritus = (a: any): a is IBPäätasonSuoritus =>
  isIBTutkinnonSuoritus(a) || isPreIBSuoritus2015(a) || isPreIBSuoritus2019(a)
