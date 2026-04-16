import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
 *
 * @see `fi.oph.koski.schema.AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina`
 */
export type AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = {
  $class: 'fi.oph.koski.schema.AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  arviointipäivä?: string
  kieli: Koodistokoodiviite<'kieli', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}

export const AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = (o: {
  arvosana: Koodistokoodiviite<'arviointiasteikkoyleissivistava', string>
  arviointipäivä?: string
  kieli: Koodistokoodiviite<'kieli', string>
  laajuus?: LaajuusVuosiviikkotunneissa
}): AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina => ({
  $class:
    'fi.oph.koski.schema.AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina',
  ...o
})

AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina.className =
  'fi.oph.koski.schema.AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina' as const

export const isAhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = (
  a: any
): a is AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina =>
  a?.$class ===
  'fi.oph.koski.schema.AhvenanmaanOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'
