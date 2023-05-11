import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (o: {
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
    koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  }): SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli',
    ...o
  })

SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli' as const

export const isSuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (
    a: any
  ): a is SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
