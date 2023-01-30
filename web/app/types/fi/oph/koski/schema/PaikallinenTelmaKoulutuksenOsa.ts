import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Työhön ja itsenäiseen elämään valmentavan koulutuksen osan tunnistiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenTelmaKoulutuksenOsa`
 */
export type PaikallinenTelmaKoulutuksenOsa = {
  $class: 'fi.oph.koski.schema.PaikallinenTelmaKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export const PaikallinenTelmaKoulutuksenOsa = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}): PaikallinenTelmaKoulutuksenOsa => ({
  $class: 'fi.oph.koski.schema.PaikallinenTelmaKoulutuksenOsa',
  ...o
})

PaikallinenTelmaKoulutuksenOsa.className =
  'fi.oph.koski.schema.PaikallinenTelmaKoulutuksenOsa' as const

export const isPaikallinenTelmaKoulutuksenOsa = (
  a: any
): a is PaikallinenTelmaKoulutuksenOsa =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenTelmaKoulutuksenOsa'
