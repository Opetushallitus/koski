import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli } from './SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { Vahvistus } from './Vahvistus'

/**
 * SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus`
 */
export type SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus'
  tyyppi: Koodistokoodiviite<string, 'muuammatillinenkoulutus'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'muuammatillinenkoulutus'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
  toimipiste?: Toimipiste
  vahvistus?: Vahvistus
}): SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus => ({
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus',
  ...o
})

SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus' as const

export const isSuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus = (
  a: any
): a is SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus'
