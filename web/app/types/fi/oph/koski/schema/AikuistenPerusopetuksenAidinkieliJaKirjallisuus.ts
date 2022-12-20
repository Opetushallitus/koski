import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * Oppiaineena äidinkieli ja kirjallisuus
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenÄidinkieliJaKirjallisuus`
 */
export type AikuistenPerusopetuksenÄidinkieliJaKirjallisuus = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenÄidinkieliJaKirjallisuus'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export const AikuistenPerusopetuksenÄidinkieliJaKirjallisuus = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}): AikuistenPerusopetuksenÄidinkieliJaKirjallisuus => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenÄidinkieliJaKirjallisuus',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

export const isAikuistenPerusopetuksenÄidinkieliJaKirjallisuus = (
  a: any
): a is AikuistenPerusopetuksenÄidinkieliJaKirjallisuus =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenÄidinkieliJaKirjallisuus'
