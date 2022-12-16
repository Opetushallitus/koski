import {
  MuidenLukioOpintojenSuoritus2015,
  isMuidenLukioOpintojenSuoritus2015
} from './MuidenLukioOpintojenSuoritus2015'
import {
  PreIBOppiaineenSuoritus2015,
  isPreIBOppiaineenSuoritus2015
} from './PreIBOppiaineenSuoritus2015'

/**
 * PreIBSuorituksenOsasuoritus2015
 *
 * @see `fi.oph.koski.schema.PreIBSuorituksenOsasuoritus2015`
 */
export type PreIBSuorituksenOsasuoritus2015 =
  | MuidenLukioOpintojenSuoritus2015
  | PreIBOppiaineenSuoritus2015

export const isPreIBSuorituksenOsasuoritus2015 = (
  a: any
): a is PreIBSuorituksenOsasuoritus2015 =>
  isMuidenLukioOpintojenSuoritus2015(a) || isPreIBOppiaineenSuoritus2015(a)
