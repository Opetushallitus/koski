import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * Oppilaitos
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Oppilaitos`
 */
export type Oppilaitos = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Oppilaitos'
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
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Oppilaitos',
  ...o
})

Oppilaitos.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Oppilaitos' as const

export const isOppilaitos = (a: any): a is Oppilaitos =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.Oppilaitos'
