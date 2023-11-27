import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * OmanÄidinkielenOpinnotLaajuusOpintopisteinä
 *
 * @see `fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusOpintopisteinä`
 */
export type OmanÄidinkielenOpinnotLaajuusOpintopisteinä = {
  $class: 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusOpintopisteinä'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus: LaajuusOpintopisteissä
  arviointipäivä?: string
}

export const OmanÄidinkielenOpinnotLaajuusOpintopisteinä = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus: LaajuusOpintopisteissä
  arviointipäivä?: string
}): OmanÄidinkielenOpinnotLaajuusOpintopisteinä => ({
  $class: 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusOpintopisteinä',
  ...o
})

OmanÄidinkielenOpinnotLaajuusOpintopisteinä.className =
  'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusOpintopisteinä' as const

export const isOmanÄidinkielenOpinnotLaajuusOpintopisteinä = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusOpintopisteinä =>
  a?.$class ===
  'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusOpintopisteinä'
