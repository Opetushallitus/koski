import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
}): AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto',
  ...o
})

AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto' as const

export const isAktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
