import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * LukioonValmistavaÄidinkieliJaKirjallisuus
 *
 * @see `fi.oph.koski.schema.LukioonValmistavaÄidinkieliJaKirjallisuus`
 */
export type LukioonValmistavaÄidinkieliJaKirjallisuus = {
  $class: 'fi.oph.koski.schema.LukioonValmistavaÄidinkieliJaKirjallisuus'
  tunniste: Koodistokoodiviite<'oppiaineetluva', 'LVAIK'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', 'AI7' | 'AI8'>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export const LukioonValmistavaÄidinkieliJaKirjallisuus = (o: {
  tunniste?: Koodistokoodiviite<'oppiaineetluva', 'LVAIK'>
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', 'AI7' | 'AI8'>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}): LukioonValmistavaÄidinkieliJaKirjallisuus => ({
  $class: 'fi.oph.koski.schema.LukioonValmistavaÄidinkieliJaKirjallisuus',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'LVAIK',
    koodistoUri: 'oppiaineetluva'
  }),
  ...o
})

LukioonValmistavaÄidinkieliJaKirjallisuus.className =
  'fi.oph.koski.schema.LukioonValmistavaÄidinkieliJaKirjallisuus' as const

export const isLukioonValmistavaÄidinkieliJaKirjallisuus = (
  a: any
): a is LukioonValmistavaÄidinkieliJaKirjallisuus =>
  a?.$class === 'fi.oph.koski.schema.LukioonValmistavaÄidinkieliJaKirjallisuus'
