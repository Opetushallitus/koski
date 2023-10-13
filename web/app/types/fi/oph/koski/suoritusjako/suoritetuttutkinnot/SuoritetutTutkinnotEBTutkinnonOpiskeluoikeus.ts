import { Koodistokoodiviite } from '../../schema/Koodistokoodiviite'
import { LocalizedString } from '../../schema/LocalizedString'
import { Koulutustoimija } from './Koulutustoimija'
import { SuoritetutTutkinnotEBTutkinnonSuoritus } from './SuoritetutTutkinnotEBTutkinnonSuoritus'
import { Oppilaitos } from './Oppilaitos'

/**
 * SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus
 *
 * @see `fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus`
 */
export type SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus = {
  $class: 'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus'
  tyyppi: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
  oid?: string
  koulutustoimija?: Koulutustoimija
  versionumero?: number
  suoritukset: Array<SuoritetutTutkinnotEBTutkinnonSuoritus>
  oppilaitos?: Oppilaitos
}

export const SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus = (
  o: {
    tyyppi?: Koodistokoodiviite<'opiskeluoikeudentyyppi', 'ebtutkinto'>
    oid?: string
    koulutustoimija?: Koulutustoimija
    versionumero?: number
    suoritukset?: Array<SuoritetutTutkinnotEBTutkinnonSuoritus>
    oppilaitos?: Oppilaitos
  } = {}
): SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus => ({
  tyyppi: Koodistokoodiviite({
    koodiarvo: 'ebtutkinto',
    koodistoUri: 'opiskeluoikeudentyyppi'
  }),
  suoritukset: [],
  $class:
    'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus',
  ...o
})

SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus.className =
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus' as const

export const isSuoritetutTutkinnotEBTutkinnonOpiskeluoikeus = (
  a: any
): a is SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus =>
  a?.$class ===
  'fi.oph.koski.suoritusjako.suoritetuttutkinnot.SuoritetutTutkinnotEBTutkinnonOpiskeluoikeus'
