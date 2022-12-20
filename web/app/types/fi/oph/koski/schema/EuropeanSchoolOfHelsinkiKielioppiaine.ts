import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * EuropeanSchoolOfHelsinkiKielioppiaine
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaine`
 */
export type EuropeanSchoolOfHelsinkiKielioppiaine = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaine'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', string>
}

export const EuropeanSchoolOfHelsinkiKielioppiaine = (o: {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkikielioppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
  kieli: Koodistokoodiviite<'kieli', string>
}): EuropeanSchoolOfHelsinkiKielioppiaine => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaine',
  ...o
})

export const isEuropeanSchoolOfHelsinkiKielioppiaine = (
  a: any
): a is EuropeanSchoolOfHelsinkiKielioppiaine =>
  a?.$class === 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiKielioppiaine'
