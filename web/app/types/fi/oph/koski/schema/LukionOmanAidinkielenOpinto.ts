import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionOmanÄidinkielenOpinto`
 */
export type LukionOmanÄidinkielenOpinto = {
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinto'
  tunniste: Koodistokoodiviite<
    'moduulikoodistolops2021',
    'OÄI1' | 'OÄI2' | 'OÄI3' | 'OÄI4' | 'OÄI5' | 'OÄI6' | 'OÄI7' | 'OÄI8'
  >
  laajuus: LaajuusOpintopisteissä
}

export const LukionOmanÄidinkielenOpinto = (o: {
  tunniste: Koodistokoodiviite<
    'moduulikoodistolops2021',
    'OÄI1' | 'OÄI2' | 'OÄI3' | 'OÄI4' | 'OÄI5' | 'OÄI6' | 'OÄI7' | 'OÄI8'
  >
  laajuus: LaajuusOpintopisteissä
}): LukionOmanÄidinkielenOpinto => ({
  $class: 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinto',
  ...o
})

LukionOmanÄidinkielenOpinto.className =
  'fi.oph.koski.schema.LukionOmanÄidinkielenOpinto' as const

export const isLukionOmanÄidinkielenOpinto = (
  a: any
): a is LukionOmanÄidinkielenOpinto =>
  a?.$class === 'fi.oph.koski.schema.LukionOmanÄidinkielenOpinto'
