import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusVuosiviikkotunneissa } from './LaajuusVuosiviikkotunneissa'

/**
 * DIA-oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.DIAOppiaineMuu`
 */
export type DIAOppiaineMuu = {
  $class: 'fi.oph.koski.schema.DIAOppiaineMuu'
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'KU'
    | 'MU'
    | 'MA'
    | 'FY'
    | 'BI'
    | 'KE'
    | 'TI'
    | 'TK'
    | 'HI'
    | 'MAA'
    | 'TA'
    | 'US'
    | 'FI'
    | 'ET'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
  osaAlue: Koodistokoodiviite<'diaosaalue', string>
  pakollinen: boolean
}

export const DIAOppiaineMuu = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetdia',
    | 'KU'
    | 'MU'
    | 'MA'
    | 'FY'
    | 'BI'
    | 'KE'
    | 'TI'
    | 'TK'
    | 'HI'
    | 'MAA'
    | 'TA'
    | 'US'
    | 'FI'
    | 'ET'
  >
  laajuus?: LaajuusVuosiviikkotunneissa
  osaAlue: Koodistokoodiviite<'diaosaalue', string>
  pakollinen: boolean
}): DIAOppiaineMuu => ({ $class: 'fi.oph.koski.schema.DIAOppiaineMuu', ...o })

export const isDIAOppiaineMuu = (a: any): a is DIAOppiaineMuu =>
  a?.$class === 'DIAOppiaineMuu'
