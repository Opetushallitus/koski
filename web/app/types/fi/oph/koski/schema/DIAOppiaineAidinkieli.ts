import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineÄidinkieli`
 */
export type DIAOppiaineÄidinkieli = {
  $class: 'fi.oph.koski.schema.DIAOppiaineÄidinkieli'
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'AI'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'oppiainediaaidinkieli', 'FI' | 'S2' | 'DE'>
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
}

export const DIAOppiaineÄidinkieli = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetdia', 'AI'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'oppiainediaaidinkieli', 'FI' | 'S2' | 'DE'>
  osaAlue?: Koodistokoodiviite<'diaosaalue', '1'>
}): DIAOppiaineÄidinkieli => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineÄidinkieli',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'oppiaineetdia'
  }),
  osaAlue: Koodistokoodiviite({ koodiarvo: '1', koodistoUri: 'diaosaalue' }),
  ...o
})

DIAOppiaineÄidinkieli.className =
  'fi.oph.koski.schema.DIAOppiaineÄidinkieli' as const

export const isDIAOppiaineÄidinkieli = (a: any): a is DIAOppiaineÄidinkieli =>
  a?.$class === 'fi.oph.koski.schema.DIAOppiaineÄidinkieli'
