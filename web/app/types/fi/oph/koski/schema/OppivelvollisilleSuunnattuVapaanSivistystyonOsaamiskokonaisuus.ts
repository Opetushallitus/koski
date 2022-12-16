import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus`
 */
export type OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus = {
  $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus'
  tunniste: Koodistokoodiviite<'vstosaamiskokonaisuus', string>
  laajuus?: LaajuusOpintopisteissä
}

export const OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus =
  (o: {
    tunniste: Koodistokoodiviite<'vstosaamiskokonaisuus', string>
    laajuus?: LaajuusOpintopisteissä
  }): OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus',
    ...o
  })

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus =
  (
    a: any
  ): a is OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus =>
    a?.$class ===
    'OppivelvollisilleSuunnattuVapaanSivistystyönOsaamiskokonaisuus'
