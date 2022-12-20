import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Opetussuunnitelmaan kuuluvan tutkinnon osan tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuValtakunnallinenTutkinnonOsa`
 */
export type MuuValtakunnallinenTutkinnonOsa = {
  $class: 'fi.oph.koski.schema.MuuValtakunnallinenTutkinnonOsa'
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
  kuvaus?: LocalizedString
}

export const MuuValtakunnallinenTutkinnonOsa = (o: {
  tunniste: Koodistokoodiviite<'tutkinnonosat', string>
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
  kuvaus?: LocalizedString
}): MuuValtakunnallinenTutkinnonOsa => ({
  $class: 'fi.oph.koski.schema.MuuValtakunnallinenTutkinnonOsa',
  ...o
})

export const isMuuValtakunnallinenTutkinnonOsa = (
  a: any
): a is MuuValtakunnallinenTutkinnonOsa =>
  a?.$class === 'fi.oph.koski.schema.MuuValtakunnallinenTutkinnonOsa'
