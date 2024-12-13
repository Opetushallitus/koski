import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli = {
  $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
}

export const AktiivisetJaPäättyneetOpinnotEBTutkinnonKoulutusmoduuli = (o: {
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  koulutustyyppi?: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  curriculum: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
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
