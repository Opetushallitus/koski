import {
  OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
} from './OppivelvollisilleSuunnatunVapaanSivistystyonOsaamiskokonaisuudenSuoritus'
import {
  OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus,
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus
} from './OppivelvollisilleSuunnatunVapaanSivistystyonValinnaistenSuuntautumisopintojenSuoritus'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus =
  | OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
  | OppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus = (
  a: any
): a is OppivelvollisilleSuunnatunVapaanSivistystyönOsasuoritus =>
  isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus(
    a
  ) ||
  isOppivelvollisilleSuunnatunVapaanSivistystyönValinnaistenSuuntautumisopintojenSuoritus(
    a
  )
