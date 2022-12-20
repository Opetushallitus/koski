import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissaTaiKursseissa } from './LaajuusVuosiviikkotunneissaTaiKursseissa'

/**
 * Oppiaineena vieras tai toinen kotimainen kieli
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli`
 */
export type AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  $class: 'fi.oph.koski.schema.AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3'
  >
}

export const AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissaTaiKursseissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3'
  >
}): AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli => ({
  $class:
    'fi.oph.koski.schema.AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli',
  ...o
})

export const isAikuistenPerusopetuksenVierasTaiToinenKotimainenKieli = (
  a: any
): a is AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli =>
  a?.$class ===
  'fi.oph.koski.schema.AikuistenPerusopetuksenVierasTaiToinenKotimainenKieli'
