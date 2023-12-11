import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * IB-lukion oppiaineen tunnistetiedot
 *
 * @see `fi.oph.koski.schema.MuuDiplomaOppiaine`
 */
export type MuuDiplomaOppiaine = {
  $class: 'fi.oph.koski.schema.MuuDiplomaOppiaine'
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'CHE'
    | 'ECO'
    | 'ESS'
    | 'HIS'
    | 'MAT'
    | 'MATST'
    | 'PHY'
    | 'PSY'
    | 'VA'
    | 'LIT'
    | 'INF'
    | 'DES'
    | 'SPO'
    | 'MATAA'
    | 'MATAI'
    | 'DIS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}

export const MuuDiplomaOppiaine = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetib',
    | 'BIO'
    | 'CHE'
    | 'ECO'
    | 'ESS'
    | 'HIS'
    | 'MAT'
    | 'MATST'
    | 'PHY'
    | 'PSY'
    | 'VA'
    | 'LIT'
    | 'INF'
    | 'DES'
    | 'SPO'
    | 'MATAA'
    | 'MATAI'
    | 'DIS'
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}): MuuDiplomaOppiaine => ({
  $class: 'fi.oph.koski.schema.MuuDiplomaOppiaine',
  ...o
})

MuuDiplomaOppiaine.className = 'fi.oph.koski.schema.MuuDiplomaOppiaine' as const

export const isMuuDiplomaOppiaine = (a: any): a is MuuDiplomaOppiaine =>
  a?.$class === 'fi.oph.koski.schema.MuuDiplomaOppiaine'
