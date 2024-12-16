import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotLaajuus } from './SuoritetutTutkinnotLaajuus'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli`
 */
export type SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli'
  kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
  eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
  laajuus?: SuoritetutTutkinnotLaajuus
  kuvaus?: LocalizedString
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
}

export const SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli =
  (o: {
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    laajuus?: SuoritetutTutkinnotLaajuus
    kuvaus?: LocalizedString
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
  }): SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli',
    ...o
  })

SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli' as const

export const isSuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli = (
  a: any
): a is SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli =>
  a?.$class ===
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli'
