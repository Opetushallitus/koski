import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * Oppiaineena vieras tai toinen kotimainen kieli
 * Perusopetuksen oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.NuortenPerusopetuksenVierasTaiToinenKotimainenKieli`
 */
export type NuortenPerusopetuksenVierasTaiToinenKotimainenKieli = {
  $class: 'fi.oph.koski.schema.NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}

export const NuortenPerusopetuksenVierasTaiToinenKotimainenKieli = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusVuosiviikkotunneissa
  kuvaus?: LocalizedString
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}): NuortenPerusopetuksenVierasTaiToinenKotimainenKieli => ({
  $class:
    'fi.oph.koski.schema.NuortenPerusopetuksenVierasTaiToinenKotimainenKieli',
  ...o
})

export const isNuortenPerusopetuksenVierasTaiToinenKotimainenKieli = (
  a: any
): a is NuortenPerusopetuksenVierasTaiToinenKotimainenKieli =>
  a?.$class === 'NuortenPerusopetuksenVierasTaiToinenKotimainenKieli'
