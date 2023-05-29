import { SuoritetutTutkinnotEBTutkinto } from './SuoritetutTutkinnotEBTutkinto'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'
import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * SuoritetutTutkinnotEBTutkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonSuoritus`
 */
export type SuoritetutTutkinnotEBTutkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonSuoritus'
  koulutusmoduuli: SuoritetutTutkinnotEBTutkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite<string, 'ebtutkinto'>
}

export const SuoritetutTutkinnotEBTutkinnonSuoritus = (o: {
  koulutusmoduuli: SuoritetutTutkinnotEBTutkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
  tyyppi: Koodistokoodiviite<string, 'ebtutkinto'>
}): SuoritetutTutkinnotEBTutkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonSuoritus',
  ...o
})

SuoritetutTutkinnotEBTutkinnonSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonSuoritus' as const

export const isSuoritetutTutkinnotEBTutkinnonSuoritus = (
  a: any
): a is SuoritetutTutkinnotEBTutkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonSuoritus'
