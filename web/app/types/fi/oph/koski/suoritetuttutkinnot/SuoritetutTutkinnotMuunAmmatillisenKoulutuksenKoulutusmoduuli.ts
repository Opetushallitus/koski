import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { SuoritetutTutkinnotLaajuus } from './SuoritetutTutkinnotLaajuus'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli`
 */
export type SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli = {
  $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli'
  tunniste: SuoritetutTutkinnotKoodistokoodiviite
  laajuus?: SuoritetutTutkinnotLaajuus
  kuvaus?: LocalizedString
}

export const SuoritetutTutkinnotMuunAmmatillisenKoulutuksenKoulutusmoduuli =
  (o: {
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
    laajuus?: SuoritetutTutkinnotLaajuus
    kuvaus?: LocalizedString
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
