import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  diplomaType?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    diplomaType?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli',
    ...o
  })

AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotInternationlSchoolKoulutusmoduuli'
