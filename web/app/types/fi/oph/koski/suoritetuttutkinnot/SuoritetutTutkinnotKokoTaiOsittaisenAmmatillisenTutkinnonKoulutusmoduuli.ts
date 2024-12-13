import { Koodistokoodiviite } from '../schema/Koodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'
import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'

/**
 * SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli`
 */
export type SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    perusteenNimi?: LocalizedString
    perusteenDiaarinumero?: string
    koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
  }

export const SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  (o: {
    kansallinenTutkintojenViitekehysNQF?: Koodistokoodiviite<'nqf', string>
    eurooppalainenTutkintojenViitekehysEQF?: Koodistokoodiviite<'eqf', string>
    perusteenNimi?: LocalizedString
    perusteenDiaarinumero?: string
    koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
  }): SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli => ({
    $class:
      'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli',
    ...o
  })

SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli.className =
  'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli' as const

export const isSuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  (
    a: any
  ): a is SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
