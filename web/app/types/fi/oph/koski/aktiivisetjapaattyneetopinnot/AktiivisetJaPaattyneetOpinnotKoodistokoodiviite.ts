import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodistokoodiviite`
 */
export type AktiivisetJaPäättyneetOpinnotKoodistokoodiviite = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodistokoodiviite'
  koodistoVersio?: number
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri?: string
}

export const AktiivisetJaPäättyneetOpinnotKoodistokoodiviite = (o: {
  koodistoVersio?: number
  koodiarvo: string
  nimi?: LocalizedString
  lyhytNimi?: LocalizedString
  koodistoUri?: string
}): AktiivisetJaPäättyneetOpinnotKoodistokoodiviite => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodistokoodiviite',
  ...o
})

AktiivisetJaPäättyneetOpinnotKoodistokoodiviite.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodistokoodiviite' as const

export const isAktiivisetJaPäättyneetOpinnotKoodistokoodiviite = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKoodistokoodiviite =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKoodistokoodiviite'
