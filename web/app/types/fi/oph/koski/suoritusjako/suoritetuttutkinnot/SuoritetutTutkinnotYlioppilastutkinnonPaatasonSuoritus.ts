import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli } from './SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus`
 */
export type SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus'
  tyyppi: Koodistokoodiviite
  alkamispäivä?: string
  koulutusmoduuli: SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = (o: {
  tyyppi: Koodistokoodiviite
  alkamispäivä?: string
  koulutusmoduuli: SuoritetutTutkinnotYlioppilastutkinnonSuorituksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus',
  ...o
})

SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus' as const

export const isSuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus = (
  a: any
): a is SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotYlioppilastutkinnonPäätasonSuoritus'
