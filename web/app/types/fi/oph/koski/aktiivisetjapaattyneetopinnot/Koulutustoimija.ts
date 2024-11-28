import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * Koulutustoimija
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.Koulutustoimija`
 */
export type Koulutustoimija = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Koulutustoimija'
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const Koulutustoimija = (o: {
  oid: string
  nimi?: LocalizedString
  yTunnus?: string
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): Koulutustoimija => ({
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Koulutustoimija',
  ...o
})

Koulutustoimija.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.Koulutustoimija' as const

export const isKoulutustoimija = (a: any): a is Koulutustoimija =>
  a?.$class === 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Koulutustoimija'
