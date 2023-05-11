import { Oppilaitos } from './Oppilaitos'
import { Koulutustoimija } from './Koulutustoimija'

/**
 * OrganisaatioHistoria
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.OrganisaatioHistoria`
 */
export type OrganisaatioHistoria = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OrganisaatioHistoria'
  muutospäivä: string
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
}

export const OrganisaatioHistoria = (o: {
  muutospäivä: string
  oppilaitos?: Oppilaitos
  koulutustoimija?: Koulutustoimija
}): OrganisaatioHistoria => ({
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OrganisaatioHistoria',
  ...o
})

OrganisaatioHistoria.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OrganisaatioHistoria' as const

export const isOrganisaatioHistoria = (a: any): a is OrganisaatioHistoria =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.OrganisaatioHistoria'
