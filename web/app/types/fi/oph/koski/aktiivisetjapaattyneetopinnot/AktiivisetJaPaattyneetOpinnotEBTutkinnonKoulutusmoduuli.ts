import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli',
  ...o
})

AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli'
