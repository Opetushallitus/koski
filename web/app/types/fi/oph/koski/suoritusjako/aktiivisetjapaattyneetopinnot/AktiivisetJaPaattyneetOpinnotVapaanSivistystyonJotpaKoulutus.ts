import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    opintokokonaisuus: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus',
    ...o
  })

AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistystyönJotpaKoulutus'
