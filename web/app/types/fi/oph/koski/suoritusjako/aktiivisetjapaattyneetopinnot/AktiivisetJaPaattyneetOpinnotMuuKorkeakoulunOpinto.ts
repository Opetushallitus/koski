import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
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
