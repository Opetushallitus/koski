import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Paikallisen tutkinnon osan osa-alueen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue`
 */
export type PaikallinenAmmatillisenTutkinnonOsanOsaAlue = {
  $class: 'fi.oph.koski.schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}

export const PaikallinenAmmatillisenTutkinnonOsanOsaAlue = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOsaamispisteissä
}): PaikallinenAmmatillisenTutkinnonOsanOsaAlue => ({
  $class: 'fi.oph.koski.schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue',
  ...o
})

export const isPaikallinenAmmatillisenTutkinnonOsanOsaAlue = (
  a: any
): a is PaikallinenAmmatillisenTutkinnonOsanOsaAlue =>
  a?.$class ===
  'fi.oph.koski.schema.PaikallinenAmmatillisenTutkinnonOsanOsaAlue'
