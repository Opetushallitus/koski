import { OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus } from './OppivelvollisilleSuunnattuVapaanSivistystyonOsaamiskokonaisuus'
import { OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus } from './OppivelvollisilleSuunnatunVapaanSivistystyonOpintokokonaisuudenSuoritus'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus`
 */
export type OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  {
    $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus'
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus
    osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus>
    tyyppi: Koodistokoodiviite<'suorituksentyyppi', 'vstosaamiskokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }

export const OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  (o: {
    koulutusmoduuli: OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus
    osasuoritukset?: Array<OppivelvollisilleSuunnatunVapaanSivistystyönOpintokokonaisuudenSuoritus>
    tyyppi?: Koodistokoodiviite<'suorituksentyyppi', 'vstosaamiskokonaisuus'>
    tila?: Koodistokoodiviite<'suorituksentila', string>
  }): OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus',
    tyyppi: Koodistokoodiviite({
      koodiarvo: 'vstosaamiskokonaisuus',
      koodistoUri: 'suorituksentyyppi'
    }),
    ...o
  })

export const isOppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =
  (
    a: any
  ): a is OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus =>
    a?.$class ===
    'fi.oph.koski.schema.OppivelvollisilleSuunnatunVapaanSivistystyönOsaamiskokonaisuudenSuoritus'
