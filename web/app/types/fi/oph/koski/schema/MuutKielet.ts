import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * MuutKielet
 *
 * @see `fi.oph.koski.schema.MuutKielet`
 */
export type MuutKielet = {
  $class: 'fi.oph.koski.schema.MuutKielet'
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMUUTK' | 'LVAK' | 'LVMAI' | 'LVPOAK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}

export const MuutKielet = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetluva',
    'LVMUUTK' | 'LVAK' | 'LVMAI' | 'LVPOAK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
}): MuutKielet => ({ $class: 'fi.oph.koski.schema.MuutKielet', ...o })

export const isMuutKielet = (a: any): a is MuutKielet =>
  a?.$class === 'MuutKielet'
