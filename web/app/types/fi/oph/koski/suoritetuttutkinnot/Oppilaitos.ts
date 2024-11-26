import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * Oppilaitos
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos'
  oid: string
  oppilaitosnumero?: SuoritetutTutkinnotKoodistokoodiviite
  nimi?: LocalizedString
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}

export const Oppilaitos = (o: {
  oid: string
  oppilaitosnumero?: SuoritetutTutkinnotKoodistokoodiviite
  nimi?: LocalizedString
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
}): Oppilaitos => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos',
  ...o
})

Oppilaitos.className = 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos'
