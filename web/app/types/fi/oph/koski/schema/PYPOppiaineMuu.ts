import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PYPOppiaineMuu
 *
 * @see `fi.oph.koski.schema.PYPOppiaineMuu`
 */
export type PYPOppiaineMuu = {
  $class: 'fi.oph.koski.schema.PYPOppiaineMuu'
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'DD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'FR'
    | 'FMT'
    | 'ICT'
    | 'ILS'
    | 'IS'
    | 'LA'
    | 'LIB'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PE'
    | 'PHE'
    | 'SCI'
    | 'SS'
    | 'VA'
    | 'ART'
    | 'FFL'
  >
}

export const PYPOppiaineMuu = (o: {
  tunniste: Koodistokoodiviite<
    'oppiaineetinternationalschool',
    | 'DD'
    | 'DE'
    | 'DR'
    | 'EAL'
    | 'EMA'
    | 'FR'
    | 'FMT'
    | 'ICT'
    | 'ILS'
    | 'IS'
    | 'LA'
    | 'LIB'
    | 'MA'
    | 'ME'
    | 'MU'
    | 'PE'
    | 'PHE'
    | 'SCI'
    | 'SS'
    | 'VA'
    | 'ART'
    | 'FFL'
  >
}): PYPOppiaineMuu => ({ $class: 'fi.oph.koski.schema.PYPOppiaineMuu', ...o })

export const isPYPOppiaineMuu = (a: any): a is PYPOppiaineMuu =>
  a?.$class === 'fi.oph.koski.schema.PYPOppiaineMuu'
