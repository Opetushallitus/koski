import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena äidinkieli ja kirjallisuus
 *
 * @see `fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2019`
 */
export type LukionÄidinkieliJaKirjallisuus2019 = {
  $class: 'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2019'
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const LukionÄidinkieliJaKirjallisuus2019 = (o: {
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): LukionÄidinkieliJaKirjallisuus2019 => ({
  $class: 'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2019',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

LukionÄidinkieliJaKirjallisuus2019.className =
  'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2019' as const

export const isLukionÄidinkieliJaKirjallisuus2019 = (
  a: any
): a is LukionÄidinkieliJaKirjallisuus2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionÄidinkieliJaKirjallisuus2019'
