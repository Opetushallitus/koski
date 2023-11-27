import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * OmanÄidinkielenOpinnotLaajuusKursseina
 *
 * @see `fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusKursseina`
 */
export type OmanÄidinkielenOpinnotLaajuusKursseina = {
  $class: 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusKursseina'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusKursseissa
  arviointipäivä?: string
}

export const OmanÄidinkielenOpinnotLaajuusKursseina = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    'O' | 'S' | 'H' | '4' | '5' | '6' | '7' | '8' | '9' | '10'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  hyväksytty?: boolean
  laajuus?: LaajuusKursseissa
  arviointipäivä?: string
}): OmanÄidinkielenOpinnotLaajuusKursseina => ({
  $class: 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusKursseina',
  ...o
})

OmanÄidinkielenOpinnotLaajuusKursseina.className =
  'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusKursseina' as const

export const isOmanÄidinkielenOpinnotLaajuusKursseina = (
  a: any
): a is OmanÄidinkielenOpinnotLaajuusKursseina =>
  a?.$class === 'fi.oph.koski.schema.OmanÄidinkielenOpinnotLaajuusKursseina'
