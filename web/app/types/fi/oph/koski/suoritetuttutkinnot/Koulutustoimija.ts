import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * Koulutustoimija
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.Koulutustoimija`
 */
export type Koulutustoimija = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.Koulutustoimija'
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}

export const Koulutustoimija = (o: {
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}): Koulutustoimija => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.Koulutustoimija',
  ...o
})

Koulutustoimija.className =
  'fi.oph.koski.suoritetuttutkinnot.Koulutustoimija' as const

export const isKoulutustoimija = (a: any): a is Koulutustoimija =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.Koulutustoimija'
