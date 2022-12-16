import {
  LukionOppiaineenOppimääränSuoritus2015,
  isLukionOppiaineenOppimääränSuoritus2015
} from './LukionOppiaineenOppimaaranSuoritus2015'
import {
  LukionOppiaineidenOppimäärienSuoritus2019,
  isLukionOppiaineidenOppimäärienSuoritus2019
} from './LukionOppiaineidenOppimaarienSuoritus2019'
import {
  LukionOppimääränSuoritus2015,
  isLukionOppimääränSuoritus2015
} from './LukionOppimaaranSuoritus2015'
import {
  LukionOppimääränSuoritus2019,
  isLukionOppimääränSuoritus2019
} from './LukionOppimaaranSuoritus2019'

/**
 * LukionPäätasonSuoritus
 *
 * @see `fi.oph.koski.schema.LukionPäätasonSuoritus`
 */
export type LukionPäätasonSuoritus =
  | LukionOppiaineenOppimääränSuoritus2015
  | LukionOppiaineidenOppimäärienSuoritus2019
  | LukionOppimääränSuoritus2015
  | LukionOppimääränSuoritus2019

export const isLukionPäätasonSuoritus = (a: any): a is LukionPäätasonSuoritus =>
  isLukionOppiaineenOppimääränSuoritus2015(a) ||
  isLukionOppiaineidenOppimäärienSuoritus2019(a) ||
  isLukionOppimääränSuoritus2015(a) ||
  isLukionOppimääränSuoritus2019(a)
