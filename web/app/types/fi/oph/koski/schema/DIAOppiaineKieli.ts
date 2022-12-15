import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineKieli`
 */
export type DIAOppiaineKieli = {
  $class: 'fi.oph.koski.schema.DIAOppiaineKieli'
  pakollinen: boolean
  osaAlue: Koodistokoodiviite<'diaosaalue', '1'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FR' | 'SV' | 'RU'>
  laajuus?: LaajuusVuosiviikkotunneissa
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'A' | 'B1' | 'B3'>
}

export const DIAOppiaineKieli = (o: {
  pakollinen: boolean
  osaAlue?: Koodistokoodiviite<'diaosaalue', '1'>
  kieli: Koodistokoodiviite<'kielivalikoima', 'EN' | 'FR' | 'SV' | 'RU'>
  laajuus?: LaajuusVuosiviikkotunneissa
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'A' | 'B1' | 'B3'>
}): DIAOppiaineKieli => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineKieli',
  osaAlue: Koodistokoodiviite({ koodiarvo: '1', koodistoUri: 'diaosaalue' }),
  ...o
})

export const isDIAOppiaineKieli = (a: any): a is DIAOppiaineKieli =>
  a?.$class === 'DIAOppiaineKieli'
