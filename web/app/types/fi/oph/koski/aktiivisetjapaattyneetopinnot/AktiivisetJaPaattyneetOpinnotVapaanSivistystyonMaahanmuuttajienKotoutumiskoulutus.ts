import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =
  (o: {
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
