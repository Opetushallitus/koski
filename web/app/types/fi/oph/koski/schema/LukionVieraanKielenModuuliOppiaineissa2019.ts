import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionVieraanKielenModuuliOppiaineissa2019`
 */
export type LukionVieraanKielenModuuliOppiaineissa2019 = {
  $class: 'fi.oph.koski.schema.LukionVieraanKielenModuuliOppiaineissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima', string>
}

export const LukionVieraanKielenModuuliOppiaineissa2019 = (o: {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli?: Koodistokoodiviite<'kielivalikoima', string>
}): LukionVieraanKielenModuuliOppiaineissa2019 => ({
  $class: 'fi.oph.koski.schema.LukionVieraanKielenModuuliOppiaineissa2019',
  ...o
})

LukionVieraanKielenModuuliOppiaineissa2019.className =
  'fi.oph.koski.schema.LukionVieraanKielenModuuliOppiaineissa2019' as const

export const isLukionVieraanKielenModuuliOppiaineissa2019 = (
  a: any
): a is LukionVieraanKielenModuuliOppiaineissa2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionVieraanKielenModuuliOppiaineissa2019'
