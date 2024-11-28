import { AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso } from './AktiivisetJaPaattyneetOpinnotOpiskeluoikeusjakso'

/**
 * AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila`
 */
export type AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila'
  opiskeluoikeusjaksot: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso>
}

export const AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila = (
  o: {
    opiskeluoikeusjaksot?: Array<AktiivisetJaPäättyneetOpinnotOpiskeluoikeusjakso>
  } = {}
): AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila',
  opiskeluoikeusjaksot: [],
  ...o
})

AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila' as const

export const isAktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotOpiskeluoikeudenTila'
