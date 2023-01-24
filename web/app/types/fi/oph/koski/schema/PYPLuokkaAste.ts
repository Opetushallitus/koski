import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PYPLuokkaAste
 *
 * @see `fi.oph.koski.schema.PYPLuokkaAste`
 */
export type PYPLuokkaAste = {
  $class: 'fi.oph.koski.schema.PYPLuokkaAste'
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    'explorer' | '1' | '2' | '3' | '4' | '5'
  >
}

export const PYPLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    'explorer' | '1' | '2' | '3' | '4' | '5'
  >
}): PYPLuokkaAste => ({ $class: 'fi.oph.koski.schema.PYPLuokkaAste', ...o })

PYPLuokkaAste.className = 'fi.oph.koski.schema.PYPLuokkaAste' as const

export const isPYPLuokkaAste = (a: any): a is PYPLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.PYPLuokkaAste'
