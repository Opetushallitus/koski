import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso`
 */
export type AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso'
  alku: string
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintojenRahoitus?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso = (o: {
  alku: string
  tila: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintojenRahoitus?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso' as const

export const isAktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso'
