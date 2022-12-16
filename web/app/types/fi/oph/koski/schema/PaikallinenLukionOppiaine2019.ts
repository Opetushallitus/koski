import { PaikallinenKoodi } from './PaikallinenKoodi'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.PaikallinenLukionOppiaine2019`
 */
export type PaikallinenLukionOppiaine2019 = {
  $class: 'fi.oph.koski.schema.PaikallinenLukionOppiaine2019'
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const PaikallinenLukionOppiaine2019 = (o: {
  tunniste: PaikallinenKoodi
  kuvaus: LocalizedString
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): PaikallinenLukionOppiaine2019 => ({
  $class: 'fi.oph.koski.schema.PaikallinenLukionOppiaine2019',
  ...o
})

export const isPaikallinenLukionOppiaine2019 = (
  a: any
): a is PaikallinenLukionOppiaine2019 =>
  a?.$class === 'PaikallinenLukionOppiaine2019'
