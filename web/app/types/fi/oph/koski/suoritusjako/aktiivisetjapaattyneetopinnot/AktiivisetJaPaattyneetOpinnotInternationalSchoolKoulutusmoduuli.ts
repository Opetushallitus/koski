import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  diplomaType?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    diplomaType?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli',
    ...o
  })

AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationalSchoolKoulutusmoduuli'
