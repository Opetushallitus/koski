import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla
 *
 * @see `fi.oph.koski.schema.AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla`
 */
export type AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla = {
  $class: 'fi.oph.koski.schema.AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla'
  tunniste: Koodistokoodiviite<
    'ammatillisenoppiaineet',
    'VVTK' | 'VVAI' | 'VVAI22' | 'VVVK'
  >
  kieli: Koodistokoodiviite<'kielivalikoima', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla =
  (o: {
    tunniste: Koodistokoodiviite<
      'ammatillisenoppiaineet',
      'VVTK' | 'VVAI' | 'VVAI22' | 'VVVK'
    >
    kieli: Koodistokoodiviite<'kielivalikoima', string>
    pakollinen: boolean
    laajuus?: LaajuusOsaamispisteissä
  }): AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla => ({
    $class:
      'fi.oph.koski.schema.AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla',
    ...o
  })

export const isAmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla = (
  a: any
): a is AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla =>
  a?.$class === 'AmmatillisenTutkinnonViestintäJaVuorovaikutusKielivalinnalla'
