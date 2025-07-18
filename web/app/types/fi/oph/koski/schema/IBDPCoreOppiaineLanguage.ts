import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaineLanguage`
 */
export type IBDPCoreOppiaineLanguage = {
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineLanguage'
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusOpintopisteissä
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
}

export const IBDPCoreOppiaineLanguage = (o: {
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusOpintopisteissä
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<'oppiaineetib', 'A' | 'A2' | 'B' | 'AB' | 'CLA'>
}): IBDPCoreOppiaineLanguage => ({
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineLanguage',
  ...o
})

IBDPCoreOppiaineLanguage.className =
  'fi.oph.koski.schema.IBDPCoreOppiaineLanguage' as const

export const isIBDPCoreOppiaineLanguage = (
  a: any
): a is IBDPCoreOppiaineLanguage =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreOppiaineLanguage'
