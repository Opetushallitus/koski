import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
}

export const SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli =
  (o: {
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
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
