import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * PaikallinenLukionOpinto
 *
 * @see `fi.oph.koski.schema.PaikallinenLukionOpinto`
 */
export type PaikallinenLukionOpinto = {
  $class: 'fi.oph.koski.schema.PaikallinenLukionOpinto'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}

export const PaikallinenLukionOpinto = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
  perusteenDiaarinumero: string
}): PaikallinenLukionOpinto => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukionOpinto',
  ...o
})

export const isPaikallinenLukionOpinto = (
  a: any
): a is PaikallinenLukionOpinto => a?.$class === 'PaikallinenLukionOpinto'
