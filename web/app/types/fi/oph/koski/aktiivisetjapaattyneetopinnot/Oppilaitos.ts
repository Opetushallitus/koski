import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * Oppilaitos
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos'
  oppilaitosnumero?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  oppilaitostyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  oid: string
  nimi?: LocalizedString
}

export const Oppilaitos = (o: {
  oppilaitosnumero?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  oppilaitostyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  oid: string
  nimi?: LocalizedString
}): Oppilaitos => ({
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos',
  ...o
})

Oppilaitos.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos'
