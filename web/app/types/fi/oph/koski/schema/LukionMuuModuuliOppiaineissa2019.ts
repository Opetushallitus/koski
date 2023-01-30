import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionMuuModuuliOppiaineissa2019`
 */
export type LukionMuuModuuliOppiaineissa2019 = {
  $class: 'fi.oph.koski.schema.LukionMuuModuuliOppiaineissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export const LukionMuuModuuliOppiaineissa2019 = (o: {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}): LukionMuuModuuliOppiaineissa2019 => ({
  $class: 'fi.oph.koski.schema.LukionMuuModuuliOppiaineissa2019',
  ...o
})

LukionMuuModuuliOppiaineissa2019.className =
  'fi.oph.koski.schema.LukionMuuModuuliOppiaineissa2019' as const

export const isLukionMuuModuuliOppiaineissa2019 = (
  a: any
): a is LukionMuuModuuliOppiaineissa2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionMuuModuuliOppiaineissa2019'
