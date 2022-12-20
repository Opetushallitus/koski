import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Paikallisen tutkinnon osan tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenTutkinnonOsa`
 */
export type PaikallinenTutkinnonOsa = {
  $class: 'fi.oph.koski.schema.PaikallinenTutkinnonOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const PaikallinenTutkinnonOsa = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): PaikallinenTutkinnonOsa => ({
  $class: 'fi.oph.koski.schema.PaikallinenTutkinnonOsa',
  ...o
})

export const isPaikallinenTutkinnonOsa = (
  a: any
): a is PaikallinenTutkinnonOsa =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenTutkinnonOsa'
