import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
}): AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto',
  ...o
})

AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto' as const

export const isAktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
