import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * Toimipiste
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.Toimipiste`
 */
export type Toimipiste = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Toimipiste'
  oid: string
  nimi?: LocalizedString
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const Toimipiste = (o: {
  oid: string
  nimi?: LocalizedString
  kotipaikka?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): Toimipiste => ({
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Toimipiste',
  ...o
})

Toimipiste.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.Toimipiste' as const

export const isToimipiste = (a: any): a is Toimipiste =>
  a?.$class === 'fi.oph.koski.aktiivisetjapaattyneetopinnot.Toimipiste'
