import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 * Oppiaineena vieras tai toinen kotimainen kieli 2019
 *
 * @see `fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2019`
 */
export type VierasTaiToinenKotimainenKieli2019 = {
  $class: 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2019'
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const VierasTaiToinenKotimainenKieli2019 = (o: {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    'A' | 'B1' | 'B2' | 'B3' | 'AOM'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): VierasTaiToinenKotimainenKieli2019 => ({
  $class: 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2019',
  ...o
})

export const isVierasTaiToinenKotimainenKieli2019 = (
  a: any
): a is VierasTaiToinenKotimainenKieli2019 =>
  a?.$class === 'fi.oph.koski.schema.VierasTaiToinenKotimainenKieli2019'
