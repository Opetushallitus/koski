import {
  LukionOppiaineenPreIBSuoritus2019,
  isLukionOppiaineenPreIBSuoritus2019
} from './LukionOppiaineenPreIBSuoritus2019'
import {
  MuidenLukioOpintojenPreIBSuoritus2019,
  isMuidenLukioOpintojenPreIBSuoritus2019
} from './MuidenLukioOpintojenPreIBSuoritus2019'

/**
 * PreIBSuorituksenOsasuoritus2019
 *
 * @see `fi.oph.koski.schema.PreIBSuorituksenOsasuoritus2019`
 */
export type PreIBSuorituksenOsasuoritus2019 =
  | LukionOppiaineenPreIBSuoritus2019
  | MuidenLukioOpintojenPreIBSuoritus2019

export const isPreIBSuorituksenOsasuoritus2019 = (
  a: any
): a is PreIBSuorituksenOsasuoritus2019 =>
  isLukionOppiaineenPreIBSuoritus2019(a) ||
  isMuidenLukioOpintojenPreIBSuoritus2019(a)
