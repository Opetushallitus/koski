import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tutkintovaiheen lukukauden tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenLukukausi`
 */
export type DIAOppiaineenTutkintovaiheenLukukausi = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenLukukausi'
  tunniste: Koodistokoodiviite<'dialukukausi', '3' | '4' | '5' | '6'>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const DIAOppiaineenTutkintovaiheenLukukausi = (o: {
  tunniste: Koodistokoodiviite<'dialukukausi', '3' | '4' | '5' | '6'>
  laajuus?: LaajuusVuosiviikkotunneissa
}): DIAOppiaineenTutkintovaiheenLukukausi => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenLukukausi',
  ...o
})

DIAOppiaineenTutkintovaiheenLukukausi.className =
  'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenLukukausi' as const

export const isDIAOppiaineenTutkintovaiheenLukukausi = (
  a: any
): a is DIAOppiaineenTutkintovaiheenLukukausi =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineenTutkintovaiheenLukukausi'
