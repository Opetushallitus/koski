import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina
 *
 * @see `fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina`
 */
export type OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = {
  $class: 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  arviointipäivä?: string
}

export const OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusVuosiviikkotunneissa
  arviointipäivä?: string
}): OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina => ({
  $class:
    'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina',
  ...o
})

export const isOmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina =>
  a?.$class ===
  'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusVuosiviikkotunteina'
