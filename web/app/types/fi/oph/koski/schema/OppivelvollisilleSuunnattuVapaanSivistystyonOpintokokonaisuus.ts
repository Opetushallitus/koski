import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus
 *
 * @see `fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus`
 */
export type OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = {
  $class: 'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus: LaajuusOpintopisteissä
}

export const OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus =
  (o: {
    tunniste: PaikallinenKoodi
    kuvaus: LocalizedString
    laajuus: LaajuusOpintopisteissä
  }): OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus => ({
    $class:
      'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus',
    ...o
  })

OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus.className =
  'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus' as const

export const isOppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus = (
  a: any
): a is OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus =>
  a?.$class ===
  'fi.oph.koski.schema.OppivelvollisilleSuunnattuVapaanSivistystyönOpintokokonaisuus'
