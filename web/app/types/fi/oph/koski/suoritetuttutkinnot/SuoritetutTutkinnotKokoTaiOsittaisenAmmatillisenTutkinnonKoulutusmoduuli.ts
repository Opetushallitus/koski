import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli`
 */
export type SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    perusteenNimi?: LocalizedString
    koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
  }

export const SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  (o: {
    tunniste: SuoritetutTutkinnotKoodistokoodiviite
    perusteenDiaarinumero?: string
    perusteenNimi?: LocalizedString
    koulutustyyppi?: SuoritetutTutkinnotKoodistokoodiviite
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
