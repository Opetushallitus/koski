import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'
import { LukionOmanÄidinkielenOpintojenOsasuoritus } from './LukionOmanAidinkielenOpintojenOsasuoritus'

/**
 * LukionOmanÄidinkielenOpinnot
 *
 * @see `fi.oph.koski.schema.LukionOmanÄidinkielenOpinnot`
 */
export type LukionOmanÄidinkielenOpinnot = {
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnot'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus: LaajuusOpintopisteissä
  arviointipäivä?: string
  osasuoritukset?: Array<LukionOmanÄidinkielenOpintojenOsasuoritus>
}

export const LukionOmanÄidinkielenOpinnot = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus: LaajuusOpintopisteissä
  arviointipäivä?: string
  osasuoritukset?: Array<LukionOmanÄidinkielenOpintojenOsasuoritus>
}): LukionOmanÄidinkielenOpinnot => ({
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnot',
  ...o
})

LukionOmanÄidinkielenOpinnot.className =
  'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnot' as const

export const isLukionOmanÄidinkielenOpinnot = (
  a: any
): a is LukionOmanÄidinkielenOpinnot =>
  a?.$class === 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinnot'
