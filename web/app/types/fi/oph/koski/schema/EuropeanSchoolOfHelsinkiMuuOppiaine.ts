import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * EuropeanSchoolOfHelsinkiMuuOppiaine
 *
 * @see `fi.oph.koski.schema.EuropeanSchoolOfHelsinkiMuuOppiaine`
 */
export type EuropeanSchoolOfHelsinkiMuuOppiaine = {
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiMuuOppiaine'
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkimuuoppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
}

export const EuropeanSchoolOfHelsinkiMuuOppiaine = (o: {
  tunniste: Koodistokoodiviite<'europeanschoolofhelsinkimuuoppiaine', string>
  laajuus: LaajuusVuosiviikkotunneissa
}): EuropeanSchoolOfHelsinkiMuuOppiaine => ({
  $class: 'fi.oph.koski.schema.EuropeanSchoolOfHelsinkiMuuOppiaine',
  ...o
})

export const isEuropeanSchoolOfHelsinkiMuuOppiaine = (
  a: any
): a is EuropeanSchoolOfHelsinkiMuuOppiaine =>
  a?.$class === 'EuropeanSchoolOfHelsinkiMuuOppiaine'
