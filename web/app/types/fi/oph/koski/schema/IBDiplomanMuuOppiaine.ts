import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.IBDiplomanMuuOppiaine`
 */
export type IBDiplomanMuuOppiaine = {
  $class: 'fi.oph.koski.schema.IBDiplomanMuuOppiaine'
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
    | 'WS'
  >
  taso: Koodistokoodiviite<'oppiaineentasoib', string>
  iboOppiainekoodi?: string
}

export const IBDiplomanMuuOppiaine = (o: {
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
    | 'WS'
  >
  taso: Koodistokoodiviite<'oppiaineentasoib', string>
  iboOppiainekoodi?: string
}): IBDiplomanMuuOppiaine => ({
  $class: 'fi.oph.koski.schema.IBDiplomanMuuOppiaine',
  ...o
})

IBDiplomanMuuOppiaine.className =
  'fi.oph.koski.schema.IBDiplomanMuuOppiaine' as const

export const isIBDiplomanMuuOppiaine = (a: any): a is IBDiplomanMuuOppiaine =>
  a?.$class === 'fi.oph.koski.schema.IBDiplomanMuuOppiaine'
