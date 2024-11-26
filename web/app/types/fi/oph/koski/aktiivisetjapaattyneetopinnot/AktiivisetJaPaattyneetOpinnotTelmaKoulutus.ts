import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotTelmaKoulutus
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus`
 */
export type AktiivisetJaPäättyneetOpinnotTelmaKoulutus = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotTelmaKoulutus = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotTelmaKoulutus => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus',
  ...o
})

AktiivisetJaPäättyneetOpinnotTelmaKoulutus.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus' as const

export const isAktiivisetJaPäättyneetOpinnotTelmaKoulutus = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotTelmaKoulutus =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotTelmaKoulutus'
