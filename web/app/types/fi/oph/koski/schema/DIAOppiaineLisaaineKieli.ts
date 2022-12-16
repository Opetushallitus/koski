import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineLisäaineKieli`
 */
export type DIAOppiaineLisäaineKieli = {
  $class: 'fi.oph.koski.schema.DIAOppiaineLisäaineKieli'
  tunniste: Koodistokoodiviite<'oppiaineetdia', 'B2'>
  laajuus?: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kielivalikoima', 'LA'>
}

export const DIAOppiaineLisäaineKieli = (
  o: {
    tunniste?: Koodistokoodiviite<'oppiaineetdia', 'B2'>
    laajuus?: LaajuusVuosiviikkotunneissa
    kieli?: Koodistokoodiviite<'kielivalikoima', 'LA'>
  } = {}
): DIAOppiaineLisäaineKieli => ({
  $class: 'fi.oph.koski.schema.DIAOppiaineLisäaineKieli',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'B2',
    koodistoUri: 'oppiaineetdia'
  }),
  kieli: Koodistokoodiviite({ koodiarvo: 'LA', koodistoUri: 'kielivalikoima' }),
  ...o
})

export const isDIAOppiaineLisäaineKieli = (
  a: any
): a is DIAOppiaineLisäaineKieli => a?.$class === 'DIAOppiaineLisäaineKieli'
