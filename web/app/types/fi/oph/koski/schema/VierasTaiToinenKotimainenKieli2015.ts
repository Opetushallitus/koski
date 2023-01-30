import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena vieras tai toinen kotimainen kieli
 *
 * @see `fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2015`
 */
export type VierasTaiToinenKotimainenKieli2015 = {
  $class: 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2015'
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}

export const VierasTaiToinenKotimainenKieli2015 = (o: {
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A1' | 'A2' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
}): VierasTaiToinenKotimainenKieli2015 => ({
  $class: 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2015',
  ...o
})

VierasTaiToinenKotimainenKieli2015.className =
  'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2015' as const

export const isVierasTaiToinenKotimainenKieli2015 = (
  a: any
): a is VierasTaiToinenKotimainenKieli2015 =>
  a?.$class === 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2015'
