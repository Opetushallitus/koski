import { SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli } from './SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'

/**
 * SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus'
  koulutusmoduuli: SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite
}

export const SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = (o: {
  koulutusmoduuli: SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite
}): SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus',
  ...o
})

SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus' as const

export const isSuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = (
  a: any
): a is SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus'
