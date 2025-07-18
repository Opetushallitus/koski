import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'
import { LaajuusOpintopisteissä } from './LaajuusOpintopisteissa'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDPCoreOppiaineMuu`
 */
export type IBDPCoreOppiaineMuu = {
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineMuu'
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'BU'
    | 'CHE'
    | 'DAN'
    | 'DIS'
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
    | 'LIT'
    | 'INF'
    | 'DES'
    | 'SPO'
    | 'MATAA'
    | 'MATAI'
  >
  laajuus?: LaajuusOpintopisteissä
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
}

export const IBDPCoreOppiaineMuu = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'BU'
    | 'CHE'
    | 'DAN'
    | 'DIS'
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
    | 'LIT'
    | 'INF'
    | 'DES'
    | 'SPO'
    | 'MATAA'
    | 'MATAI'
  >
  laajuus?: LaajuusOpintopisteissä
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
  ryhmä: Koodistokoodiviite<'aineryhmaib', string>
}): IBDPCoreOppiaineMuu => ({
  $class: 'fi.oph.koski.schema.IBDPCoreOppiaineMuu',
  ...o
})

IBDPCoreOppiaineMuu.className =
  'fi.oph.koski.schema.IBDPCoreOppiaineMuu' as const

export const isIBDPCoreOppiaineMuu = (a: any): a is IBDPCoreOppiaineMuu =>
  a?.$class === 'fi.oph.koski.schema.IBDPCoreOppiaineMuu'
