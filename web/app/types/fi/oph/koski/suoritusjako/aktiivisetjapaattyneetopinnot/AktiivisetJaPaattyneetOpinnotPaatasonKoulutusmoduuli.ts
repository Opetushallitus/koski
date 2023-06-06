import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { AktiivisetJaPäättyneetOpinnotLaajuus } from './AktiivisetJaPaattyneetOpinnotLaajuus'

/**
 * AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli'
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
}

export const AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = (o: {
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  perusteenDiaarinumero?: string
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  laajuus?: AktiivisetJaPäättyneetOpinnotLaajuus
}): AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli => ({
  $class:
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli',
  ...o
})

AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli = (
  a: any
): a is AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotPäätasonKoulutusmoduuli'
