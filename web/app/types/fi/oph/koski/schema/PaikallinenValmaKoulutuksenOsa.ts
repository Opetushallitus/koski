import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Ammatilliseen peruskoulutukseen valmentavan koulutuksen osan tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenValmaKoulutuksenOsa`
 */
export type PaikallinenValmaKoulutuksenOsa = {
  $class: 'fi.oph.koski.schema.PaikallinenValmaKoulutuksenOsa'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}

export const PaikallinenValmaKoulutuksenOsa = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  pakollinen: boolean
}): PaikallinenValmaKoulutuksenOsa => ({
  $class: 'fi.oph.koski.schema.PaikallinenValmaKoulutuksenOsa',
  ...o
})

export const isPaikallinenValmaKoulutuksenOsa = (
  a: any
): a is PaikallinenValmaKoulutuksenOsa =>
  a?.$class === 'fi.oph.koski.schema.PaikallinenValmaKoulutuksenOsa'
