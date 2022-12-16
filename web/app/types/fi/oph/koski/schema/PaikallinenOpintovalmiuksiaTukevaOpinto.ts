import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * PaikallinenOpintovalmiuksiaTukevaOpinto
 *
 * @see `fi.oph.koski.schema.PaikallinenOpintovalmiuksiaTukevaOpinto`
 */
export type PaikallinenOpintovalmiuksiaTukevaOpinto = {
  $class: 'fi.oph.koski.schema.PaikallinenOpintovalmiuksiaTukevaOpinto'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}

export const PaikallinenOpintovalmiuksiaTukevaOpinto = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  laajuus?: LaajuusOsaamispisteissä
}): PaikallinenOpintovalmiuksiaTukevaOpinto => ({
  $class: 'fi.oph.koski.schema.PaikallinenOpintovalmiuksiaTukevaOpinto',
  ...o
})

export const isPaikallinenOpintovalmiuksiaTukevaOpinto = (
  a: any
): a is PaikallinenOpintovalmiuksiaTukevaOpinto =>
  a?.$class === 'PaikallinenOpintovalmiuksiaTukevaOpinto'
