import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot 2019
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2019`
 */
export type LukionMuuValtakunnallinenOppiaine2019 = {
  $class: 'fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2019'
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'BI'
    | 'ET'
    | 'FI'
    | 'FY'
    | 'GE'
    | 'HI'
    | 'KE'
    | 'KU'
    | 'LI'
    | 'MU'
    | 'OP'
    | 'PS'
    | 'TE'
    | 'YH'
  >
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}

export const LukionMuuValtakunnallinenOppiaine2019 = (o: {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'BI'
    | 'ET'
    | 'FI'
    | 'FY'
    | 'GE'
    | 'HI'
    | 'KE'
    | 'KU'
    | 'LI'
    | 'MU'
    | 'OP'
    | 'PS'
    | 'TE'
    | 'YH'
  >
  pakollinen: boolean
  laajuus?: LaajuusOpintopisteissä
}): LukionMuuValtakunnallinenOppiaine2019 => ({
  $class: 'fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2019',
  ...o
})

export const isLukionMuuValtakunnallinenOppiaine2019 = (
  a: any
): a is LukionMuuValtakunnallinenOppiaine2019 =>
  a?.$class === 'fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2019'
