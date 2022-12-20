import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena matematiikka
 *
 * @see `fi.oph.koski.schema.LukionMatematiikka2015`
 */
export type LukionMatematiikka2015 = {
  $class: 'fi.oph.koski.schema.LukionMatematiikka2015'
  pakollinen: boolean
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
}

export const LukionMatematiikka2015 = (o: {
  pakollinen: boolean
  oppimäärä: Koodistokoodiviite<'oppiainematematiikka', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'MA'>
}): LukionMatematiikka2015 => ({
  $class: 'fi.oph.koski.schema.LukionMatematiikka2015',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'MA',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isLukionMatematiikka2015 = (a: any): a is LukionMatematiikka2015 =>
  a?.$class === 'fi.oph.koski.schema.LukionMatematiikka2015'
