import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionMuuModuuliMuissaOpinnoissa2019`
 */
export type LukionMuuModuuliMuissaOpinnoissa2019 = {
  $class: 'fi.oph.koski.schema.LukionMuuModuuliMuissaOpinnoissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}

export const LukionMuuModuuliMuissaOpinnoissa2019 = (o: {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
}): LukionMuuModuuliMuissaOpinnoissa2019 => ({
  $class: 'fi.oph.koski.schema.LukionMuuModuuliMuissaOpinnoissa2019',
  ...o
})

LukionMuuModuuliMuissaOpinnoissa2019.className =
  'fi.oph.koski.schema.LukionMuuModuuliMuissaOpinnoissa2019' as const

export const isLukionMuuModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionMuuModuuliMuissaOpinnoissa2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionMuuModuuliMuissaOpinnoissa2019'
