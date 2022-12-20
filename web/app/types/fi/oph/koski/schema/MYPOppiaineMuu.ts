import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * MYPOppiaineMuu
 *
 * @see `fi.oph.koski.schema.MYPOppiaineMuu`
 */
export type MYPOppiaineMuu = {
  $class: 'fi.oph.koski.schema.MYPOppiaineMuu'
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'AD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'ILS'
    | 'IS'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PHE'
    | 'PP'
    | 'SCI'
    | 'SMA'
    | 'VA'
    | 'INS'
    | 'MF'
  >
}

export const MYPOppiaineMuu = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'AD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'ILS'
    | 'IS'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PHE'
    | 'PP'
    | 'SCI'
    | 'SMA'
    | 'VA'
    | 'INS'
    | 'MF'
  >
}): MYPOppiaineMuu => ({ $class: 'fi.oph.koski.schema.MYPOppiaineMuu', ...o })

export const isMYPOppiaineMuu = (a: any): a is MYPOppiaineMuu =>
  a?.$class === 'fi.oph.koski.schema.MYPOppiaineMuu'
