import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * Oppilaitos
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos'
  oid: string
  oppilaitosnumero?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi?: LocalizedString
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const Oppilaitos = (o: {
  oid: string
  oppilaitosnumero?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi?: LocalizedString
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): Oppilaitos => ({
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos',
  ...o
})

Oppilaitos.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class === 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Oppilaitos'
