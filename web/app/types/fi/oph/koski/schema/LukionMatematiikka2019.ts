import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena matematiikka
 *
 * @see `fi.oph.koski.schema.LukionMatematiikka2019`
 */
export type LukionMatematiikka2019 = {
  $class: 'fi.oph.koski.schema.LukionMatematiikka2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const LukionMatematiikka2019 = (o: {
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): LukionMatematiikka2019 => ({
  $class: 'fi.oph.koski.schema.LukionMatematiikka2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'MA',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isLukionMatematiikka2019 = (a: any): a is LukionMatematiikka2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionMatematiikka2019'
