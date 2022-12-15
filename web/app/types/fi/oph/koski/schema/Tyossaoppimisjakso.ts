import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'
import { LaajuusOsaamispisteissä } from './LaajuusOsaamispisteissa'

/**
 * Työssäoppimisjakso
 *
 * @see `fi.oph.koski.schema.Työssäoppimisjakso`
 */
export type Työssäoppimisjakso = {
  $class: 'fi.oph.koski.schema.Työssäoppimisjakso'
  työssäoppimispaikka?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}

export const Työssäoppimisjakso = (o: {
  työssäoppimispaikka?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
  laajuus: LaajuusOsaamispisteissä
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
}): Työssäoppimisjakso => ({
  $class: 'fi.oph.koski.schema.Työssäoppimisjakso',
  ...o
})

export const isTyössäoppimisjakso = (a: any): a is Työssäoppimisjakso =>
  a?.$class === 'Työssäoppimisjakso'
