import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * Koulutustoimija
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutustoimija`
 */
export type Koulutustoimija = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutustoimija'
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
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutustoimija',
  ...o
})

Koulutustoimija.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutustoimija' as const

export const isKoulutustoimija = (a: any): a is Koulutustoimija =>
  a?.$class === 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.Koulutustoimija'
