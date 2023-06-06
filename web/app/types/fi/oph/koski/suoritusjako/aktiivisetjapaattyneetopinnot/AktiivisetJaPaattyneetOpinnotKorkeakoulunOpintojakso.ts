import { AktiivisetJaPäättyneetOpinnotPaikallinenKoodi } from './AktiivisetJaPaattyneetOpinnotPaikallinenKoodi'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso'
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
  nimi: LocalizedString
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotPaikallinenKoodi
  nimi: LocalizedString
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
}): AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso',
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulunOpintojakso'
