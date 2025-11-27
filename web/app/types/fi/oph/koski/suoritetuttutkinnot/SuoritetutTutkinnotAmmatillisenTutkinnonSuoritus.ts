import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli } from './SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotOsaamisalajakso } from './SuoritetutTutkinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus`
 */
export type SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus'
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = (o: {
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkinto'>
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus',
  ...o
})

SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus' as const

export const isSuoritetutTutkinnotAmmatillisenTutkinnonSuoritus = (
  a: any
): a is SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonSuoritus'
