import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönMaahanmuuttajienKotoutumiskoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
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
