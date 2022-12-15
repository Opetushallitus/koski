import {
  AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli,
  isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
} from './AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'
import {
  AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla,
  isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
} from './AmmatillisenTutkinnonViestintaJaVuorovaikutusKielivalinnalla'
import {
  AmmatillisenTutkinnonÄidinkieli,
  isAmmatillisenTutkinnonÄidinkieli
} from './AmmatillisenTutkinnonAidinkieli'
import {
  PaikallinenAmmatillisenTutkinnonOsanOsaAlue,
  isPaikallinenAmmatillisenTutkinnonOsanOsaAlue
} from './PaikallinenAmmatillisenTutkinnonOsanOsaAlue'
import {
  ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue,
  isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue
} from './ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue'

/**
 * AmmatillisenTutkinnonOsanOsaAlue
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonOsanOsaAlue`
 */
export type AmmatillisenTutkinnonOsanOsaAlue =
  | AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
  | AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
  | AmmatillisenTutkinnonÄidinkieli
  | PaikallinenAmmatillisenTutkinnonOsanOsaAlue
  | ValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue

export const isAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is AmmatillisenTutkinnonOsanOsaAlue =>
  isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli(a) ||
  isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla(a) ||
  isAmmatillisenTutkinnonÄidinkieli(a) ||
  isPaikallinenAmmatillisenTutkinnonOsanOsaAlue(a) ||
  isValtakunnallinenAmmatillisenTutkinnonOsanOsaAlue(a)
