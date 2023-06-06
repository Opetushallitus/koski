import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'

/**
 * AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus'
  opintokokonaisuus?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus = (o: {
  opintokokonaisuus?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotVapaanSivistyönKoulutus'
