import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus'
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus =
  (o: {
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
