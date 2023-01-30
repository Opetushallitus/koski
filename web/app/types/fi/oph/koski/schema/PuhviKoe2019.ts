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

PuhviKoe2019.className = 'fi.oph.koski.schema.PuhviKoe2019' as const

export const isPuhviKoe2019 = (a: any): a is PuhviKoe2019 =>
  a?.$class === 'fi.oph.koski.schema.PuhviKoe2019'
