import {
  LukionOppiaineenSuoritus2015,
  isLukionOppiaineenSuoritus2015
} from './LukionOppiaineenSuoritus2015'
import {
  MuidenLukioOpintojenSuoritus2015,
  isMuidenLukioOpintojenSuoritus2015
} from './MuidenLukioOpintojenSuoritus2015'

/**
 * LukionOppimääränOsasuoritus2015
 *
 * @see `fi.oph.koski.schema.LukionOppimääränOsasuoritus2015`
 */
export type LukionOppimääränOsasuoritus2015 =
  | LukionOppiaineenSuoritus2015
  | MuidenLukioOpintojenSuoritus2015

export const isLukionOppimääränOsasuoritus2015 = (
  a: any
): a is LukionOppimääränOsasuoritus2015 =>
  isLukionOppiaineenSuoritus2015(a) || isMuidenLukioOpintojenSuoritus2015(a)
