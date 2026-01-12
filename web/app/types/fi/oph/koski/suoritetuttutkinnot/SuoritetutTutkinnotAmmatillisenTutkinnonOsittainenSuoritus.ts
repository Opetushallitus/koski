import { SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli } from './SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotOsaamisalajakso } from './SuoritetutTutkinnotOsaamisalajakso'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus`
 */
export type SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus'
  toinenTutkintonimike?: boolean
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  toinenOsaamisala?: boolean
  korotettuOpiskeluoikeusOid?: string
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = (o: {
  toinenTutkintonimike?: boolean
  koulutusmoduuli: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  toimipiste?: Toimipiste
  tutkintonimike?: Array<SuoritetutTutkinnotKoodistokoodiviite>
  tyyppi: Koodistokoodiviite<string, 'ammatillinentutkintoosittainen'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  toinenOsaamisala?: boolean
  korotettuOpiskeluoikeusOid?: string
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  osaamisala?: Array<SuoritetutTutkinnotOsaamisalajakso>
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus => ({
  $class:
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus',
  ...o
})

SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus' as const

export const isSuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus = (
  a: any
): a is SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotAmmatillisenTutkinnonOsittainenSuoritus'
