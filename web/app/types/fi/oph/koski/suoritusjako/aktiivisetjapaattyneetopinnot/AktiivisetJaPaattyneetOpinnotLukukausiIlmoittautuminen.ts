import { AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso } from './AktiivisetJaPaattyneetOpinnotLukukausiIlmoittautumisjakso'

/**
 * AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen`
 */
export type AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen'
  ilmoittautumisjaksot: Array<AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso>
}

export const AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen = (
  o: {
    ilmoittautumisjaksot?: Array<AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautumisjakso>
  } = {}
): AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen',
  ilmoittautumisjaksot: [],
  ...o
})

AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen' as const

export const isAktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen'
