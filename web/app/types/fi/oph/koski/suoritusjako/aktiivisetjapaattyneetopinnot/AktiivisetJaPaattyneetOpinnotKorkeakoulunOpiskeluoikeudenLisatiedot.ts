import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen } from './AktiivisetJaPaattyneetOpinnotLukukausiIlmoittautuminen'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
    virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
  }

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    o: {
      virtaOpiskeluoikeudenTyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
      lukukausiIlmoittautuminen?: AktiivisetJaPäättyneetOpinnotLukukausi_Ilmoittautuminen
    } = {}
  ): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot',
    ...o
  })

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpiskeluoikeudenLisätiedot'
