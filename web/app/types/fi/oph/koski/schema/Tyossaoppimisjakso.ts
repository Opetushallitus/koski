import { LocalizedString } from './LocalizedString'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Työssäoppimisjakso
 *
 * @see `fi.oph.koski.schema.Työssäoppimisjakso`
 */
export type Työssäoppimisjakso = {
  $class: 'fi.oph.koski.schema.Työssäoppimisjakso'
  työssäoppimispaikka?: LocalizedString
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
}

export const Työssäoppimisjakso = (o: {
  työssäoppimispaikka?: LocalizedString
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
}): Työssäoppimisjakso => ({
  $class: 'fi.oph.koski.schema.Työssäoppimisjakso',
  ...o
})

Työssäoppimisjakso.className = 'fi.oph.koski.schema.Työssäoppimisjakso' as const

export const isTyössäoppimisjakso = (a: any): a is Työssäoppimisjakso =>
  a?.$class === 'fi.oph.koski.schema.Työssäoppimisjakso'
