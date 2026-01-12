import { LaajuusKursseissa } from './LaajuusKursseissa'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionUskonto2015`
 */
export type LukionUskonto2015 = {
  $class: 'fi.oph.koski.schema.LukionUskonto2015'
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export const LukionUskonto2015 = (o: {
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}): LukionUskonto2015 => ({
  $class: 'fi.oph.koski.schema.LukionUskonto2015',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

LukionUskonto2015.className = 'fi.oph.koski.schema.LukionUskonto2015' as const

export const isLukionUskonto2015 = (a: any): a is LukionUskonto2015 =>
  a?.$class === 'fi.oph.koski.schema.LukionUskonto2015'
