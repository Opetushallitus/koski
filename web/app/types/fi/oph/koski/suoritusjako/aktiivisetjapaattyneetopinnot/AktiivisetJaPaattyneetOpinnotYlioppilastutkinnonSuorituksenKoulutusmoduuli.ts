import { AktiivisetJaPäättyneetOpinnotKoodistokoodiviite } from './AktiivisetJaPaattyneetOpinnotKoodistokoodiviite'

/**
 * AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli`
 */
export type AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }

export const AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (o: {
    tunniste: AktiivisetJaPäättyneetOpinnotKoodistokoodiviite
  }): AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli',
    ...o
  })

AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli' as const

export const isAktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (
    a: any
  ): a is AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.aktiivisetjapaattyneetopinnot.AktiivisetJaPäättyneetOpinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
