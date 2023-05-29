import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * Toimipiste
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Toimipiste`
 */
export type Toimipiste = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Toimipiste'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}

export const Toimipiste = (o: {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}): Toimipiste => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Toimipiste',
  ...o
})

Toimipiste.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Toimipiste' as const

export const isToimipiste = (a: any): a is Toimipiste =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Toimipiste'
