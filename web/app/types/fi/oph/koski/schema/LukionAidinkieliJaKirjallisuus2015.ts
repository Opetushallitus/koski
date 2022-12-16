import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena äidinkieli ja kirjallisuus
 *
 * @see `fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2015`
 */
export type LukionÄidinkieliJaKirjallisuus2015 = {
  $class: 'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2015'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export const LukionÄidinkieliJaKirjallisuus2015 = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}): LukionÄidinkieliJaKirjallisuus2015 => ({
  $class: 'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2015',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isLukionÄidinkieliJaKirjallisuus2015 = (
  a: any
): a is LukionÄidinkieliJaKirjallisuus2015 =>
  a?.$class === 'LukionÄidinkieliJaKirjallisuus2015'
