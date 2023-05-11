import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * Järjestämismuoto
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Järjestämismuoto`
 */
export type Järjestämismuoto = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Järjestämismuoto'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const Järjestämismuoto = (o: {
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}): Järjestämismuoto => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Järjestämismuoto',
  ...o
})

Järjestämismuoto.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Järjestämismuoto' as const

export const isJärjestämismuoto = (a: any): a is Järjestämismuoto =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Järjestämismuoto'
