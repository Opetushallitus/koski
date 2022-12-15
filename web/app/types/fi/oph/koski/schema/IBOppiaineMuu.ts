import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusTunneissa } from './LaajuusTunneissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBOppiaineMuu`
 */
export type IBOppiaineMuu = {
  $class: 'fi.oph.koski.schema.IBOppiaineMuu'
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'BU'
    | 'CHE'
    | 'DAN'
    | 'ECO'
    | 'FIL'
    | 'GEO'
    | 'HIS'
    | 'MAT'
    | 'MATFT'
    | 'MATST'
    | 'MUS'
    | 'PHI'
    | 'PHY'
    | 'POL'
    | 'PSY'
    | 'REL'
    | 'SOC'
    | 'ESS'
    | 'THE'
    | 'VA'
    | 'CS'
  >
}

export const IBOppiaineMuu = (o: {
  pakollinen: boolean
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  laajuus?: LaajuusTunneissa
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'BU'
    | 'CHE'
    | 'DAN'
    | 'ECO'
    | 'FIL'
    | 'GEO'
    | 'HIS'
    | 'MAT'
    | 'MATFT'
    | 'MATST'
    | 'MUS'
    | 'PHI'
    | 'PHY'
    | 'POL'
    | 'PSY'
    | 'REL'
    | 'SOC'
    | 'ESS'
    | 'THE'
    | 'VA'
    | 'CS'
  >
}): IBOppiaineMuu => ({ $class: 'fi.oph.koski.schema.IBOppiaineMuu', ...o })

export const isIBOppiaineMuu = (a: any): a is IBOppiaineMuu =>
  a?.$class === 'IBOppiaineMuu'
