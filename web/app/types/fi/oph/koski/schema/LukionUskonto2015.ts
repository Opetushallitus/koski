import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionUskonto2015`
 */
export type LukionUskonto2015 = {
  $class: 'fi.oph.koski.schema.LukionUskonto2015'
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}

export const LukionUskonto2015 = (o: {
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
}): LukionUskonto2015 => ({
  $class: 'fi.oph.koski.schema.LukionUskonto2015',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isLukionUskonto2015 = (a: any): a is LukionUskonto2015 =>
  a?.$class === 'LukionUskonto2015'
