import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotDIATutkinto } from './SuoritetutTutkinnotDIATutkinto'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotDIATutkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinnonSuoritus`
 */
export type SuoritetutTutkinnotDIATutkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinnonSuoritus'
  tyyppi: Koodistokoodiviite<string, 'diatutkintovaihe'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotDIATutkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotDIATutkinnonSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'diatutkintovaihe'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotDIATutkinto
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotDIATutkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinnonSuoritus',
  ...o
})

SuoritetutTutkinnotDIATutkinnonSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinnonSuoritus' as const

export const isSuoritetutTutkinnotDIATutkinnonSuoritus = (
  a: any
): a is SuoritetutTutkinnotDIATutkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotDIATutkinnonSuoritus'
