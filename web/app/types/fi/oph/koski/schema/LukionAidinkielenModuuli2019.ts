import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionÄidinkielenModuuli2019`
 */
export type LukionÄidinkielenModuuli2019 = {
  $class: 'fi.oph.koski.schema.LukionÄidinkielenModuuli2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export const LukionÄidinkielenModuuli2019 = (o: {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}): LukionÄidinkielenModuuli2019 => ({
  $class: 'fi.oph.koski.schema.LukionÄidinkielenModuuli2019',
  ...o
})

LukionÄidinkielenModuuli2019.className =
  'fi.oph.koski.schema.LukionÄidinkielenModuuli2019' as const

export const isLukionÄidinkielenModuuli2019 = (
  a: any
): a is LukionÄidinkielenModuuli2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionÄidinkielenModuuli2019'
