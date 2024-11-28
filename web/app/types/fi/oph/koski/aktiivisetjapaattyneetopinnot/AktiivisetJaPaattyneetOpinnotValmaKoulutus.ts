import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotValmaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotValmaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotValmaKoulutus = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotValmaKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotValmaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotValmaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotValmaKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotValmaKoulutus'
