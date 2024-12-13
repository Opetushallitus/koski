import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus'
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus =
  (o: {
    opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus'
