import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  }

export const AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  }): AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli',
    ...o
  })

AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli.className =
  'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
