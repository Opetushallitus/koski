import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusKursseissa } from './LaajuusKursseissa'

/**
 * Lukion/IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2015`
 */
export type LukionMuuValtakunnallinenOppiaine2015 = {
  $class: 'fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2015'
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'OP'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
}

export const LukionMuuValtakunnallinenOppiaine2015 = (o: {
  tunniste: Koodistokoodiviite<
    'koskioppiaineetyleissivistava',
    | 'HI'
    | 'MU'
    | 'BI'
    | 'PS'
    | 'ET'
    | 'KO'
    | 'FI'
    | 'KE'
    | 'YH'
    | 'TE'
    | 'KS'
    | 'FY'
    | 'GE'
    | 'LI'
    | 'KU'
    | 'OP'
  >
  pakollinen: boolean
  laajuus?: LaajuusKursseissa
  perusteenDiaarinumero?: string
}): LukionMuuValtakunnallinenOppiaine2015 => ({
  $class: 'fi.oph.koski.schema.LukionMuuValtakunnallinenOppiaine2015',
  ...o
})

export const isLukionMuuValtakunnallinenOppiaine2015 = (
  a: any
): a is LukionMuuValtakunnallinenOppiaine2015 =>
  a?.$class === 'LukionMuuValtakunnallinenOppiaine2015'
