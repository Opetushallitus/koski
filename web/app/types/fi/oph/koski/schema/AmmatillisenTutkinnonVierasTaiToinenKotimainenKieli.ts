import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli`
 */
export type AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'VK' | 'TK1' | 'TK2'>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli = (o: {
  tunniste: Koodistokoodiviite<'ammatillisenoppiaineet', 'VK' | 'TK1' | 'TK2'>
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli => ({
  $class:
    'fi.oph.koski.schema.AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli',
  ...o
})

AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli.className =
  'fi.oph.koski.schema.AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli' as const

export const isAmmatillisenTutkinnonVierasTaiToinenKotimainenKieli = (
  a: any
): a is AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli =>
  a?.$class ===
  'fi.oph.koski.schema.AmmatillisenTutkinnonVierasTaiToinenKotimainenKieli'
