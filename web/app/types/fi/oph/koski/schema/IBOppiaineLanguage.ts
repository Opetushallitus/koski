import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusTunneissa } from './LaajuusTunneissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBOppiaineLanguage`
 */
export type IBOppiaineLanguage = {
  $class: 'fi.oph.koski.schema.IBOppiaineLanguage'
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
}

export const IBOppiaineLanguage = (o: {
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB'>
}): IBOppiaineLanguage => ({
  $class: 'fi.oph.koski.schema.IBOppiaineLanguage',
  ...o
})

export const isIBOppiaineLanguage = (a: any): a is IBOppiaineLanguage =>
  a?.$class === 'fi.oph.koski.schema.IBOppiaineLanguage'
