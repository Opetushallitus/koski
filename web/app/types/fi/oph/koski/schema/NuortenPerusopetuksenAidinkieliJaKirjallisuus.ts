import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Oppiaineena äidinkieli ja kirjallisuus
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenÄidinkieliJaKirjallisuus`
 */
export type NuortenPerusopetuksenÄidinkieliJaKirjallisuus = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenÄidinkieliJaKirjallisuus'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}

export const NuortenPerusopetuksenÄidinkieliJaKirjallisuus = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'oppiaineaidinkielijakirjallisuus', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste?: Koodistokoodiviite<'koskioppiaineetyleissivistava', 'AI'>
}): NuortenPerusopetuksenÄidinkieliJaKirjallisuus => ({
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenÄidinkieliJaKirjallisuus',
  tunniste: Koodistokoodiviite({
    koodiarvo: 'AI',
    koodistoUri: 'koskioppiaineetyleissivistava'
  }),
  ...o
})

NuortenPerusopetuksenÄidinkieliJaKirjallisuus.className =
  'fi.oph.koski.schema.NuortenPerusopetuksenÄidinkieliJaKirjallisuus' as const

export const isNuortenPerusopetuksenÄidinkieliJaKirjallisuus = (
  a: any
): a is NuortenPerusopetuksenÄidinkieliJaKirjallisuus =>
  a?.$class ===
  'fi.oph.koski.schema.NuortenPerusopetuksenÄidinkieliJaKirjallisuus'
