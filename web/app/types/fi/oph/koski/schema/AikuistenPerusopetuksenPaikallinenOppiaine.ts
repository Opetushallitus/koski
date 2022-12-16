import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'
import { LocalizedString } from './LocalizedString'
import { PaikallinenKoodi } from './PaikallinenKoodi'

/**
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenPaikallinenOppiaine`
 */
export type AikuistenPerusopetuksenPaikallinenOppiaine = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenPaikallinenOppiaine'
  pakollinen: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}

export const AikuistenPerusopetuksenPaikallinenOppiaine = (o: {
  pakollinen?: boolean
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: PaikallinenKoodi
}): AikuistenPerusopetuksenPaikallinenOppiaine => ({
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenPaikallinenOppiaine',
  pakollinen: false,
  ...o
})

export const isAikuistenPerusopetuksenPaikallinenOppiaine = (
  a: any
): a is AikuistenPerusopetuksenPaikallinenOppiaine =>
  a?.$class === 'AikuistenPerusopetuksenPaikallinenOppiaine'
