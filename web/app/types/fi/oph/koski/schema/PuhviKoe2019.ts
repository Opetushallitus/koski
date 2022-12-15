import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LocalizedString } from './LocalizedString'

/**
 * PuhviKoe2019
 *
 * @see `fi.oph.koski.schema.PuhviKoe2019`
 */
export type PuhviKoe2019 = {
  $class: 'fi.oph.koski.schema.PuhviKoe2019'
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}

export const PuhviKoe2019 = (o: {
  arvosana: Koodistokoodiviite<
    'arviointiasteikkoyleissivistava',
    '4' | '5' | '6' | '7' | '8' | '9' | '10' | 'S' | 'H'
  >
  kuvaus?: LocalizedString
  päivä: string
  hyväksytty?: boolean
}): PuhviKoe2019 => ({ $class: 'fi.oph.koski.schema.PuhviKoe2019', ...o })

export const isPuhviKoe2019 = (a: any): a is PuhviKoe2019 =>
  a?.$class === 'PuhviKoe2019'
