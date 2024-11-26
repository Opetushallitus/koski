import { AktiivisetJaPäättyneetOpinnotPaikallinenKoodi } from './AktiivisetJaPaattyneetOpinnotPaikallinenKoodi'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso'
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
  nimi: LocalizedString
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
  nimi: LocalizedString
}): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso'
