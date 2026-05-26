import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * Oppilaitos
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos'
  oppilaitosnumero?: SuoritetutTutkinnotKoodistokoodiviite
  oppilaitostyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
  oid: string
  nimi?: LocalizedString
}

export const Oppilaitos = (o: {
  oppilaitosnumero?: SuoritetutTutkinnotKoodistokoodiviite
  oppilaitostyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  kotipaikka?: SuoritetutTutkinnotKoodistokoodiviite
  oid: string
  nimi?: LocalizedString
}): Oppilaitos => ({
  $class: 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos',
  ...o
})

Oppilaitos.className = 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.suoritetuttutkinnot.Oppilaitos'
