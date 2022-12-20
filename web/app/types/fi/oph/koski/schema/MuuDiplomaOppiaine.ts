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
  >
  taso?: Koodistokoodiviite<'oppiaineentasoib', string>
}): MuuDiplomaOppiaine => ({
  $class: 'fi.oph.koski.schema.MuuDiplomaOppiaine',
  ...o
})

export const isMuuDiplomaOppiaine = (a: any): a is MuuDiplomaOppiaine =>
  a?.$class === 'fi.oph.koski.schema.MuuDiplomaOppiaine'
