import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotLaajuus } from './SuoritetutTutkinnotLaajuus'

/**
 * Työssäoppimisjakso
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Työssäoppimisjakso`
 */
export type Työssäoppimisjakso = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Työssäoppimisjakso'
  työssäoppimispaikka?: LocalizedString
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite
  loppu?: string
  laajuus: SuoritetutTutkinnotLaajuus
  maa: SuoritetutTutkinnotKoodistokoodiviite
  alku: string
}

export const Työssäoppimisjakso = (o: {
  työssäoppimispaikka?: LocalizedString
  paikkakunta: SuoritetutTutkinnotKoodistokoodiviite
  loppu?: string
  laajuus: SuoritetutTutkinnotLaajuus
  maa: SuoritetutTutkinnotKoodistokoodiviite
  alku: string
}): Työssäoppimisjakso => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Työssäoppimisjakso',
  ...o
})

Työssäoppimisjakso.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Työssäoppimisjakso' as const

export const isTyössäoppimisjakso = (a: any): a is Työssäoppimisjakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Työssäoppimisjakso'
