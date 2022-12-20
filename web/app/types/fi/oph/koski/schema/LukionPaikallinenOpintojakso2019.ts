import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'
import { LocalizedString } from './LocalizedString'

/**
 * Paikallisen lukion/IB-lukion opintojakson tunnistetiedot 2019
 *
 * @see `fi.oph.koski.schema.LukionPaikallinenOpintojakso2019`
 */
export type LukionPaikallinenOpintojakso2019 = {
  $class: 'fi.oph.koski.schema.LukionPaikallinenOpintojakso2019'
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
  kuvaus: LocalizedString
  pakollinen: boolean
}

export const LukionPaikallinenOpintojakso2019 = (o: {
  tunniste: PaikallinenKoodi
  laajuus: LaajuusOpintopisteissä
  kuvaus: LocalizedString
  pakollinen: boolean
}): LukionPaikallinenOpintojakso2019 => ({
  $class: 'fi.oph.koski.schema.LukionPaikallinenOpintojakso2019',
  ...o
})

export const isLukionPaikallinenOpintojakso2019 = (
  a: any
): a is LukionPaikallinenOpintojakso2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionPaikallinenOpintojakso2019'
