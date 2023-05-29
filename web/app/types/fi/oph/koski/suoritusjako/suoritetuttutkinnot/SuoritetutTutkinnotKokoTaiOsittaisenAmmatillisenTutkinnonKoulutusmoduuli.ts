import { SuoritetutTutkinnotKoodistokoodiviite } from './SuoritetutTutkinnotKoodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'

/**
 * SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli`
 */
export type SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  {
    $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
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
      'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli',
    ...o
  })

SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli' as const

export const isSuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =
  (
    a: any
  ): a is SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli =>
    a?.$class ===
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotKokoTaiOsittaisenAmmatillisenTutkinnonKoulutusmoduuli'
