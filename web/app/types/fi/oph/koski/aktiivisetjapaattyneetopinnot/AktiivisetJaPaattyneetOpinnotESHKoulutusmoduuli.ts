import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli',
  ...o
})

AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotESHKoulutusmoduuli'
