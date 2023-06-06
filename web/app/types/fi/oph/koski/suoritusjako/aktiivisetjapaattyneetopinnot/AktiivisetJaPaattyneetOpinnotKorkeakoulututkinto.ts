import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto`
 */
export type AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  virtaNimi?: LocalizedString
}

export const AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  virtaNimi?: LocalizedString
}): AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto',
  ...o
})

AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto' as const

export const isAktiivisetJaPäättyneetOpinnotKorkeakoulututkinto = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotKorkeakoulututkinto'
