import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * Koulutussopimusjakso
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutussopimusjakso`
 */
export type Koulutussopimusjakso = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutussopimusjakso'
  työssäoppimispaikka?: LocalizedString
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite
  loppu?: string
  maa: SuoritetutTutkinnotKoodistokoodiviite
  alku: string
}

export const Koulutussopimusjakso = (o: {
  työssäoppimispaikka?: LocalizedString
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite
  loppu?: string
  maa: SuoritetutTutkinnotKoodistokoodiviite
  alku: string
}): Koulutussopimusjakso => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutussopimusjakso',
  ...o
})

Koulutussopimusjakso.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutussopimusjakso' as const

export const isKoulutussopimusjakso = (a: any): a is Koulutussopimusjakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutussopimusjakso'
