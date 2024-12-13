import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (o: {
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
  }): SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli',
    ...o
  })

SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli' as const

export const isSuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (
    a: any
  ): a is SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
