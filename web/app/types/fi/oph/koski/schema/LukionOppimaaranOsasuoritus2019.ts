import {
  LukionOppiaineenSuoritus2019,
  isLukionOppiaineenSuoritus2019
} from './LukionOppiaineenSuoritus2019'
import {
  MuidenLukioOpintojenSuoritus2019,
  isMuidenLukioOpintojenSuoritus2019
} from './MuidenLukioOpintojenSuoritus2019'

/**
 * LukionOppimääränOsasuoritus2019
 *
 * @see `fi.oph.koski.schema.LukionOppimääränOsasuoritus2019`
 */
export type LukionOppimääränOsasuoritus2019 =
  | LukionOppiaineenSuoritus2019
  | MuidenLukioOpintojenSuoritus2019

export const isLukionOppimääränOsasuoritus2019 = (
  a: any
): a is LukionOppimääränOsasuoritus2019 =>
  isLukionOppiaineenSuoritus2019(a) || isMuidenLukioOpintojenSuoritus2019(a)
