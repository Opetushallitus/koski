import { LocalizedString } from './LocalizedString'
import { Koodistokoodiviite } from './Koodistokoodiviite'

/**
 * Koulutussopimusjakso
 *
 * @see `fi.oph.koski.schema.Koulutussopimusjakso`
 */
export type Koulutussopimusjakso = {
  $class: 'fi.oph.koski.schema.Koulutussopimusjakso'
  työssäoppimispaikka?: LocalizedString
  työssäoppimispaikanYTunnus?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
}

export const Koulutussopimusjakso = (o: {
  työssäoppimispaikka?: LocalizedString
  työssäoppimispaikanYTunnus?: string
  maa: Koodistokoodiviite<'maatjavaltiot2', string>
  alku: string
  työtehtävät?: LocalizedString
  paikkakunta: Koodistokoodiviite<'kunta', string>
  loppu?: string
}): Koulutussopimusjakso => ({
  $class: 'fi.oph.koski.schema.Koulutussopimusjakso',
  ...o
})

Koulutussopimusjakso.className =
  'fi.oph.koski.schema.Koulutussopimusjakso' as const

export const isKoulutussopimusjakso = (a: any): a is Koulutussopimusjakso =>
  a?.$class === 'fi.oph.koski.schema.Koulutussopimusjakso'
