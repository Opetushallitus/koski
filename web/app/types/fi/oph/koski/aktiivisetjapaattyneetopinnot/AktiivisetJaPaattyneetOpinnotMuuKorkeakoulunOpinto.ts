import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto`
 */
export type AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const AktiivisetJaPäättyneetOpinnotMuuKorkeakoulunOpinto = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  nimi: LocalizedString
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
