import {
  MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus,
  isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
} from './MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyonOpintojenSuoritus'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus
} from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'

/**
 * VapaanSivistystyönOpintokokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.VapaanSivistystyönOpintokokonaisuudenSuoritus`
 */
export type VapaanSivistystyönOpintokokonaisuudenSuoritus =
  | MuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus

export const isVapaanSivistystyönOpintokokonaisuudenSuoritus = (
  a: any
): a is VapaanSivistystyönOpintokokonaisuudenSuoritus =>
  isMuuallaSuoritettuOppivelvollisilleSuunnatunVapaanSivistystyönOpintojenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus(a)
