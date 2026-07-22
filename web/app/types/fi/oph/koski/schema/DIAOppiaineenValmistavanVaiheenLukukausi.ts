import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiOpintopisteissä } from './LaajuusVuosiviikkotunneissaTaiOpintopisteissa'

/**
 * DIA-oppiaineen valmistavan vaiheen lukukauden tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukausi`
 */
export type DIAOppiaineenValmistavanVaiheenLukukausi = {
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukausi'
  tunniste: Koodistokoodiviite<'dialukukausi', '1' | '2'>
  laajuus?: LaajuusVuosiviikkotunneissaTaiOpintopisteissä
}

export const DIAOppiaineenValmistavanVaiheenLukukausi = (o: {
  tunniste: Koodistokoodiviite<'dialukukausi', '1' | '2'>
  laajuus?: LaajuusVuosiviikkotunneissaTaiOpintopisteissä
}): DIAOppiaineenValmistavanVaiheenLukukausi => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukausi',
  ...o
})

DIAOppiaineenValmistavanVaiheenLukukausi.className =
  'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukausi' as const

export const isDIAOppiaineenValmistavanVaiheenLukukausi = (
  a: any
): a is DIAOppiaineenValmistavanVaiheenLukukausi =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineenValmistavanVaiheenLukukausi'
