import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Valtakunnallisen lukion/IB-lukion moduulin tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionVieraanKielenModuuliMuissaOpinnoissa2019`
 */
export type LukionVieraanKielenModuuliMuissaOpinnoissa2019 = {
  $class: 'fi.oph.koski.schema.LukionVieraanKielenModuuliMuissaOpinnoissa2019'
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}

export const LukionVieraanKielenModuuliMuissaOpinnoissa2019 = (o: {
  tunniste: Koodistokoodiviite<'moduulikoodistolops2021', string>
  laajuus: LaajuusOpintopisteissä
  pakollinen: boolean
  kieli: Koodistokoodiviite<'kielivalikoima', string>
}): LukionVieraanKielenModuuliMuissaOpinnoissa2019 => ({
  $class: 'fi.oph.koski.schema.LukionVieraanKielenModuuliMuissaOpinnoissa2019',
  ...o
})

export const isLukionVieraanKielenModuuliMuissaOpinnoissa2019 = (
  a: any
): a is LukionVieraanKielenModuuliMuissaOpinnoissa2019 =>
  a?.$class ===
  'fi.oph.koski.schema.LukionVieraanKielenModuuliMuissaOpinnoissa2019'
