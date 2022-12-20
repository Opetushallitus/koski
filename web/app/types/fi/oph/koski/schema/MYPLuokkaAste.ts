import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * MYPLuokkaAste
 *
 * @see `fi.oph.koski.schema.MYPLuokkaAste`
 */
export type MYPLuokkaAste = {
  $class: 'fi.oph.koski.schema.MYPLuokkaAste'
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    '6' | '7' | '8' | '9' | '10'
  >
}

export const MYPLuokkaAste = (o: {
  tunniste: Koodistokoodiviite<
    'internationalschoolluokkaaste',
    '6' | '7' | '8' | '9' | '10'
  >
}): MYPLuokkaAste => ({ $class: 'fi.oph.koski.schema.MYPLuokkaAste', ...o })

export const isMYPLuokkaAste = (a: any): a is MYPLuokkaAste =>
  a?.$class === 'fi.oph.koski.schema.MYPLuokkaAste'
