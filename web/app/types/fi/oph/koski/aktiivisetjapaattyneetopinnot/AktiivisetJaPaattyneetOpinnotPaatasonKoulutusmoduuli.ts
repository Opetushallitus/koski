import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}): AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli',
  ...o
})

AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli'
