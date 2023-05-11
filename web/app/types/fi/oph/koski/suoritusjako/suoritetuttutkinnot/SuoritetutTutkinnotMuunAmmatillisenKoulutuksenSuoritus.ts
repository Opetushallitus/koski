import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli } from './SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
import { Koulutussopimusjakso } from './Koulutussopimusjakso'
import { SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli } from './SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli'
import { Toimipiste } from './Toimipiste'
import { OsaamisenHankkimistapajakso } from './OsaamisenHankkimistapajakso'
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
  täydentääTutkintoa?: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
  vahvistus?: Vahvistus
}

export const SuoritetutTutkinnotMuunAmmatillisenKoulutuksenSuoritus = (o: {
  tyyppi: Koodistokoodiviite<string, 'muuammatillinenkoulutus'>
  suorituskieli?: SuoritetutTutkinnotKoodistokoodiviite
  suoritustapa?: SuoritetutTutkinnotKoodistokoodiviite
  täydentääTutkintoa?: SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
  koulutussopimukset?: Array<Koulutussopimusjakso>
  koulutusmoduuli: SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
  toimipiste?: Toimipiste
  osaamisenHankkimistavat?: Array<OsaamisenHankkimistapajakso>
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
