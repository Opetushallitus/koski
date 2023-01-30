import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionUskonto2019`
 */
export type LukionUskonto2019 = {
  $class: 'fi.oph.koski.schema.LukionUskonto2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}

export const LukionUskonto2019 = (o: {
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'KT'>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
  uskonnonOppimäärä?: Koodistokoodiviite<'uskonnonoppimaara', string>
}): LukionUskonto2019 => ({
  $class: 'fi.oph.koski.schema.LukionUskonto2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'KT',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

LukionUskonto2019.className = 'fi.oph.koski.schema.LukionUskonto2019' as const

export const isLukionUskonto2019 = (a: any): a is LukionUskonto2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionUskonto2019'
